package com.zzx.media.camera.v1.manager

import android.graphics.Rect
import android.hardware.Camera
import android.hardware.Camera.Parameters
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface
import android.view.SurfaceHolder
import com.zzx.media.bean.Const
import com.zzx.media.camera.ICameraManager
import com.zzx.media.camera.ICameraManager.Companion.SENSOR_BACK_CAMERA
import com.zzx.media.camera.ICameraManager.Companion.SENSOR_FRONT_CAMERA
import com.zzx.media.recorder.IRecorder
import com.zzx.utils.rxjava.singleThread
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

/**@author Tomy
 * Created by Tomy on 2018/4/5.
 */
class Camera1Manager: ICameraManager<SurfaceHolder, Camera> {

    private var mCamera: Camera? = null

    private lateinit var mParameters: Camera.Parameters

    private var mCameraId: Int = 0

    private var mPictureCount = 0

    private var mBurstMode = false

    private var mIsRecording = false

    private var mPreviewed = AtomicBoolean(false)

    private var mCameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK

    private val mObject = Object()

    private var mPreviewSurface: SurfaceHolder? = null


    private var mVideoRecorder: IRecorder? = null

    private var mPictureDataCallback: ICameraManager.PictureDataCallback? = null

    private var mRecordPreviewReady: ICameraManager.RecordPreviewReady? = null

    private var mHandlerThread: HandlerThread = HandlerThread(Camera1Manager::class.simpleName)
    private var mHandler: Handler

    private var mFocusCallback: ICameraManager.AutoFocusCallback? = null

    private var mIsManualFocusSupported           = false
    private var mIsPictureAutoFocusSupported    = false
    private var mIsVideoAutoFocusSupported      = false

    init {
        mHandlerThread.start()
        mHandler = Handler(mHandlerThread.looper)
    }


    override fun openFrontCamera() {
        for (i in 0 until getCameraCount()) {
            val info = Camera.CameraInfo()
            Camera.getCameraInfo(i, info)
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                Timber.e("${Const.TAG}openFrontCamera()")
                if (getCameraCount() > 2) {
                    if (info.orientation != 90) {
                        continue
                    }
                }
                mCameraId = i
                mCameraFacing = info.facing
                openSpecialCamera(mCameraId)
                setDisplayOrientation(0)
                setPictureRotation(0)
                return
            }
        }
    }

    override fun openBackCamera() {
        for (i in 0 until getCameraCount()) {
            val info = Camera.CameraInfo()
            Camera.getCameraInfo(i, info)
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                Timber.e("${Const.TAG}openBackCamera()")
                mCameraId = i
                mCameraFacing = info.facing
                openSpecialCamera(mCameraId)
                setDisplayOrientation(0)
                setPictureRotation(0)
                return
            }
        }
    }

    override fun openSpecialCamera(cameraId: Int) {
        if (mCamera != null) {
            return
        }
        val id = if (getCameraCount() <= cameraId) {
            getCameraCount() - 1
        } else {
            cameraId
        }
        Timber.e("${Const.TAG}cameraId = $cameraId; getCameraCount = ${getCameraCount()}")
        mCamera = Camera.open(id)
        mParameters = mCamera!!.parameters.apply {
            supportedFocusModes.forEach {
                Timber.e("focusMode = $it")
                when (it) {
                    "manual"   -> {
                        mIsManualFocusSupported = true
                    }
                    Parameters.FOCUS_MODE_CONTINUOUS_PICTURE    -> {
                        mIsPictureAutoFocusSupported = true
                    }
                    Parameters.FOCUS_MODE_CONTINUOUS_VIDEO  -> {
                        mIsVideoAutoFocusSupported  = true
                    }
                }
            }
        }
        setFocusMode(Parameters.FOCUS_MODE_AUTO)
        mStateCallback?.onCameraOpenSuccess(mCamera)
    }

    override fun openExternalCamera() {
        Timber.e("${Const.TAG}openBackCamera(): mCamera = $mCamera")
        openSpecialCamera(1)
    }

    /**
     * 只设置了预览 Surface ,但是不会调用 [startPreview].
     * 此方法跟[startPreview]共同使用由自身决定何时启动预览.
     * */
    override fun setPreviewSurface(surface: SurfaceHolder) {
        mPreviewSurface = surface
        mCamera?.setPreviewDisplay(mPreviewSurface)
    }

    /**
     * 此方法调用之前必须先调用[setPreviewSurface],自行决定决定何时启动预览.
     * */
    override fun startPreview() {
        mCamera?.startPreview()
    }

    /**
     * 等价于[setPreviewSurface]+[startPreview].
     * 设置完预览界面后即可启动预览.
     * */
    override fun startPreview(surface: SurfaceHolder) {
        if (!mPreviewed.get()) {
            setPreviewSurface(surface)
            startPreview()
            mPreviewed.set(true)
            startAutoFocus()
        }
    }

    override fun stopPreview() {
        if (mPreviewed.get()) {
            mCamera?.stopPreview()
            mCamera?.setPreviewDisplay(null)
            mPreviewed.set(false)
        }
    }

    override fun restartPreview() {
        stopPreview()
        startPreview(mPreviewSurface!!)
    }

    /**
     * 开始录像
     * */
    override fun startRecordPreview(surface: Surface?) {
    }

    override fun startRecord() {
        mIsRecording = true
    }

    override fun setIRecorder(recorder: IRecorder) {
    }

    override fun startAutoFocus(focusCallback: ICameraManager.AutoFocusCallback?) {
        Timber.e("startAutoFocus")
        cancelAutoFocus()
        focusCallback?.apply {
            mFocusCallback = this
        }
        mCamera?.autoFocus { success, _ ->
            Timber.e("autoFocus.success = $success")
//            cancelAutoFocus()
            setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
            mFocusCallback?.onAutoFocusCallbackSuccess(success)
        }
    }

    override fun setAutoFocusCallback(focusCallback: ICameraManager.AutoFocusCallback?) {
        mFocusCallback = focusCallback
    }

    override fun cancelAutoFocus() {
        mCamera?.cancelAutoFocus()
    }

    override fun focusOnRect(focusRect: Rect, focusCallback: ICameraManager.AutoFocusCallback?) {
        if (getMaxNumFocusAreas() > 0) {
            mParameters.apply {
                ArrayList<Camera.Area>().let {
                    it.add(Camera.Area(focusRect, 1000))
                    meteringAreas = it
                    focusAreas  = it
                }
                focusMode = Camera.Parameters.FOCUS_MODE_AUTO
                mCamera?.parameters = this
            }
        }
        startAutoFocus(focusCallback)
    }

    override fun focusOnPoint(x: Int, y: Int, screenWidth: Int, screenHeight: Int, horWidth: Int, verHeight: Int, focusCallback: ICameraManager.AutoFocusCallback?) {
        if (screenWidth == 0 || screenHeight == 0) {
            return
        }
        val pointX = x * 2000 / screenWidth - 1000
        val pointY = y * 2000 / screenHeight - 1000
        Rect().apply {
            left    = Math.max(pointX - horWidth, -1000)
            right   = Math.min(pointX + horWidth, 1000)
            top     = Math.max(pointY - verHeight, -1000)
            bottom  = Math.min(pointY + verHeight, 1000)
            Timber.e("\n***** \n[$x:$y];\n screen = [$screenWidth x $screenHeight];\n point = [$pointX:$pointY];\n rect = $this\n*****")
            focusOnRect(this)
        }
    }

    override fun getFocusRect(): List<Rect> {
        return emptyList()
    }

    override fun getSupportFocusMode(): List<String> {
        return mParameters.supportedFocusModes
    }

    override fun setFocusMode(focusMode: String) {
        mParameters.apply {
            this.focusMode = focusMode
            mCamera?.parameters = this
        }
    }

    override fun isAutoFocusSupported(): Boolean {
        return mIsManualFocusSupported
    }

    override fun isPictureAutoFocusSupported(): Boolean {
        return mIsPictureAutoFocusSupported
    }

    override fun isVideoAutoFocusSupported(): Boolean {
        return mIsVideoAutoFocusSupported
    }

    override fun getMaxNumFocusAreas(): Int {
        return mParameters.maxNumFocusAreas
    }

    /**
     * 停止录像
     * */
    override fun stopRecord() {
        mIsRecording = false
    }

    override fun closeCamera() {
        mIsVideoAutoFocusSupported = false
        mIsPictureAutoFocusSupported = false
        mIsManualFocusSupported = false
        mCamera?.release()
        mCamera = null
    }

    override fun releaseCamera() {
        stopPreview()
        closeCamera()
        mStateCallback?.onCameraClosed()
    }

    override fun getCameraCount(): Int {
        return Camera.getNumberOfCameras()
    }

    override fun getSupportPreviewSizeList(): Array<Size> {
        val list = mParameters.supportedPreviewSizes
        return Array(list.size) {
            Size(list[it].width, list[it].height)
        }
    }

    override fun getSupportPreviewFormatList(): Array<Int> {
        val list = mParameters.supportedPreviewFormats
        return Array(list.size) {
            list[it]
        }
    }

    override fun setPreviewParams(width: Int, height: Int, format: Int) {
        mParameters.apply {
            previewFormat = format
            setPreviewSize(width, height)
            mCamera!!.parameters = this
        }
    }

    override fun getSupportCaptureSizeList(): Array<Size> {
        val list = mParameters.supportedPictureSizes
        return Array(list.size) {
            Size(list[it].width, list[it].height)
        }
    }

    override fun getSupportCaptureFormatList(): Array<Int> {
        val list = mParameters.supportedPictureFormats
        return Array(list.size) {
            list[it]
        }
    }

    override fun setCaptureParams(width: Int, height: Int, format: Int) {
        mParameters.apply {
            if (mIsRecording) {
                Timber.e("mIsRecording = $mIsRecording; isVssSupported = $isVideoSnapshotSupported")
                if (!isVideoSnapshotSupported) {
                    return
                }
            }
            pictureFormat = format
            setPictureSize(width, height)
            mCamera!!.parameters = this
        }
    }

    override fun getSupportRecordSizeList(): Array<Size> {
        val list = mParameters.supportedVideoSizes
        return Array(list.size) {
            Size(list[it].width, list[it].height)
        }
    }

    override fun getSensorOrientation(): Int {
        return if (mCameraFacing != Camera.CameraInfo.CAMERA_FACING_BACK)
            SENSOR_FRONT_CAMERA
        else
            SENSOR_BACK_CAMERA
    }

    override fun takePicture(callback: ICameraManager.PictureDataCallback?) {
        setPictureCallback(callback)
        takePicture()
    }

    override fun takePictureBurst(count: Int, callback: ICameraManager.PictureDataCallback?) {
        mPictureDataCallback = callback
        takePictureBurst(count)
    }

    override fun setPictureCallback(callback: ICameraManager.PictureDataCallback?) {
        mPictureDataCallback = callback
    }

    override fun setRecordPreviewCallback(callback: ICameraManager.RecordPreviewReady?) {
        mRecordPreviewReady = callback
    }

    override fun takePicture() {
        if (!mIsRecording)
            setPictureNormalMode()
        startTakePicture()
    }

    override fun takePictureBurst(count: Int) {
        if (!mIsRecording)
            setPictureContinuousMode(count)
        startTakePicture()
    }

    /**
     * 在当前的缩放下放大镜头的Level
     * @param level Int +level
     */
    override fun zoomUp(level: Int) {
        val zoom = mParameters.zoom + level
        mParameters.zoom = if (zoom <= getZoomMax()) zoom else getZoomMax()
        mCamera?.parameters = mParameters
    }

    /**
     * 在当前的缩放倍数下缩小镜头的Level
     * @param level Int -Level
     */
    override fun zoomDown(level: Int) {
        val zoom = mParameters.zoom - level
        mParameters.zoom = if (zoom >= 0) zoom else 0
        mCamera?.parameters = mParameters
    }

    /***
     * @return Int 获得可放大的最大倍数.
     */
    override fun getZoomMax(): Int {
        return mParameters.maxZoom
    }

    /**
     * @param level Int 设置的缩放倍数.level不得小于零以及不得超过最大放大倍数,可通过[getZoomMax]获得最大放大倍数.否则无效.
     */
    override fun setZoomLevel(level: Int) {
        if (level < 0 || level > getZoomMax())
            return
        mParameters.zoom = level
        mCamera?.parameters = mParameters
    }

    private fun startTakePicture() = singleThread {
        mPictureCount = 0
        if (mIsRecording) {
            val vssSupported = mParameters.isVideoSnapshotSupported
            Timber.e("mIsRecording = $mIsRecording; isVssSupported = $vssSupported")
            if (!vssSupported) {
                return@singleThread
            }
        }
        mCamera?.takePicture(null, null, mPictureCallback)
    }

    private val mPictureCallback =
        Camera.PictureCallback { data, _ ->
            mPictureDataCallback?.onCaptureFinished(data)
            Timber.e("mPictureCount = $mPictureCount; mBurstMode = $mBurstMode; mContinuousShotCount = $mContinuousShotCount")
            if (mBurstMode) {
                if (++mPictureCount >= mContinuousShotCount) {
                    if (!mIsRecording) {
                        startPreview()
                    }
                    mPictureDataCallback?.onCaptureDone()
                }
            } else {
                if (!mIsRecording) {
                    startPreview()
                }
                mPictureDataCallback?.onCaptureDone()
            }
        }
//    }

    private val callback = Camera.PictureCallback {
        _, _ ->
        Timber.e("mPictureCount = $mPictureCount")
    }

    /**
     * @param rotation Int 预览界面的旋转角度
     */
    override fun setDisplayOrientation(rotation: Int) {
        Timber.e("setDisplayOrientation.rotation = $rotation")
        mCamera?.setDisplayOrientation(rotation)
    }

    /**
     * @param rotation Int 图片的旋转角度
     */
    override fun setPictureRotation(rotation: Int) {
        Timber.e("setPictureRotation.rotation = $rotation")
        mParameters.setRotation(rotation)
        mCamera?.parameters = mParameters
    }

    /**
     * @param enable Boolean 是否打开拍照声音
     */
    override fun enableShutter(enable: Boolean) {
        mCamera?.enableShutterSound(enable)
    }

    /**
     * 获得摄像头设备.
     * @see Camera
     * */
    override fun getCameraDevice(): Camera? {
        return mCamera
    }

    private var mStateCallback: ICameraManager.CameraStateCallback<Camera>? = null

    override fun setStateCallback(stateCallback: ICameraManager.CameraStateCallback<Camera>) {
        mStateCallback = stateCallback
    }

    /**
     * @see setPictureContinuousMode
     */
    private fun setPictureNormalMode() {
        if (mBurstMode) {
            mParameters.apply {
                set(CAP_MODE, CAP_MODE_NORMAL)
                set(BURST_NUM, 1)
                set(MTK_CAM_MODE, CAMERA_MODE_NORMAL)
                mCamera?.parameters = this
            }
            mBurstMode = false
        }
    }

    /**
     * 高速连拍总数
     * */
    private var mContinuousShotCount = 0

    /**
     * 设置成高速连拍模式
     * */
    private fun setPictureContinuousMode(pictureCount: Int) {
        mContinuousShotCount = pictureCount
        if (!mBurstMode) {
            mBurstMode = true
            mParameters.apply {
                set(CAP_MODE, CAP_MODE_CONTINUOUS)
                set(BURST_NUM, pictureCount)
                set(MTK_CAM_MODE, CAMERA_MODE_MTK_PRV)
                mCamera?.parameters = this
            }
            restartPreview()
        } else {
            mParameters.apply {
                set(BURST_NUM, pictureCount)
                mCamera?.parameters = this
            }
        }
    }

    companion object {
        const val CAP_MODE  = "cap-mode"
        const val CAP_MODE_NORMAL   = "normal"
        const val CAP_MODE_CONTINUOUS = "continuousshot"
        const val BURST_NUM = "burst-num"
        const val MTK_CAM_MODE = "mtk-cam-mode"
        const val CAMERA_MODE_NORMAL    = 0
        const val CAMERA_MODE_MTK_PRV   = 1
        const val CAMERA_MODE_MTK_VDO   = 2
        const val CAMERA_MODE_MTK_VT    = 3
    }

}