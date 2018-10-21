package com.zzx.media.camera.v1.manager

import android.hardware.Camera
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface
import android.view.SurfaceHolder
import com.zzx.media.camera.ICameraManager
import com.zzx.media.recorder.IRecorder
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

    private var mRecording = false

    private var mCameraClosed = AtomicBoolean(false)

    private val mObject = Object()

    private var mPreviewSurface: SurfaceHolder? = null


    private var mVideoRecorder: IRecorder? = null

    private var mPictureDataCallback: ICameraManager.PictureDataCallback? = null

    private var mRecordPreviewReady: ICameraManager.RecordPreviewReady? = null

    private var mHandlerThread: HandlerThread = HandlerThread(Camera1Manager::class.simpleName)
    private var mHandler: Handler

    init {
        mHandlerThread.start()
        mHandler = Handler(mHandlerThread.looper)
    }


    override fun openFrontCamera() {
        for (i in 0 until getCameraCount()) {
            val info = Camera.CameraInfo()
            Camera.getCameraInfo(i, info)
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mCameraId = i
                openSpecialCamera(mCameraId)
                return
            }
        }
    }

    override fun openBackCamera() {
        for (i in 0 until getCameraCount()) {
            val info = Camera.CameraInfo()
            Camera.getCameraInfo(i, info)
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mCameraId = i
                openSpecialCamera(mCameraId)
                return
            }
        }
    }

    private fun openSpecialCamera(cameraId: Int) {
        mCamera = Camera.open(cameraId)
        mParameters = mCamera!!.parameters
        mStateCallback?.onCameraOpenSuccess(mCamera)
    }

    override fun openExternalCamera() {
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
        setPreviewSurface(surface)
        startPreview()
    }

    override fun stopPreview() {
        mCamera?.stopPreview()
        mCamera?.setPreviewDisplay(null)
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
        mRecording = true
    }

    override fun setIRecorder(recorder: IRecorder) {
    }

    /**
     * 停止录像
     * */
    override fun stopRecord() {
        mRecording = false
    }

    override fun closeCamera() {
        mCamera?.release()
    }

    override fun releaseCamera() {
        stopPreview()
        mCamera?.release()
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
        return 0
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
        setPictureNormalMode()
        startTakePicture()
    }

    override fun takePictureBurst(count: Int) {
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

    private fun startTakePicture() {
        mPictureCount = 0
        mCamera?.takePicture(null, null, mPictureCallback)
    }

    private val mPictureCallback =
        Camera.PictureCallback { data, _ ->
            mPictureDataCallback?.onCaptureFinished(data)
            Timber.e("mPictureCount = $mPictureCount; mBurstMode = $mBurstMode")
            if (mBurstMode) {
                if (++mPictureCount >= mContinuousShotCount && !mRecording) {
                    startPreview()
                }
            } else if (!mRecording) {
                startPreview()
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
        mCamera?.setDisplayOrientation(rotation)
    }

    /**
     * @param rotation Int 图片的旋转角度
     */
    override fun setPictureRotation(rotation: Int) {
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
            mParameters.apply {
                set(CAP_MODE, CAP_MODE_CONTINUOUS)
                set(BURST_NUM, pictureCount)
                set(MTK_CAM_MODE, CAMERA_MODE_MTK_PRV)
                mCamera?.parameters = this
            }
            restartPreview()
            mBurstMode = true
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