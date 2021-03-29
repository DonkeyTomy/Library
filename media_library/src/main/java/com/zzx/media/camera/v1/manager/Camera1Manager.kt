package com.zzx.media.camera.v1.manager

import android.annotation.SuppressLint
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.Camera.Parameters
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface
import android.view.SurfaceHolder
import com.tencent.bugly.crashreport.CrashReport
import com.zzx.media.bean.Const
import com.zzx.media.camera.CameraCore
import com.zzx.media.camera.CameraCore.Status
import com.zzx.media.camera.ICameraManager
import com.zzx.media.camera.ICameraManager.Companion.CAMERA_OPEN_ERROR_GET_INFO_FAILED
import com.zzx.media.camera.ICameraManager.Companion.CAMERA_OPEN_ERROR_NOT_RELEASE
import com.zzx.media.camera.ICameraManager.Companion.CAMERA_OPEN_ERROR_NO_CAMERA
import com.zzx.media.camera.ICameraManager.Companion.CAMERA_OPEN_ERROR_OPEN_FAILED
import com.zzx.media.camera.ICameraManager.Companion.CAMERA_OPEN_ERROR_PREVIEW_FAILED
import com.zzx.media.camera.ICameraManager.Companion.FOCUS_MODE_MANUAL
import com.zzx.media.camera.ICameraManager.Companion.SENSOR_BACK_CAMERA
import com.zzx.media.camera.ICameraManager.Companion.SENSOR_FRONT_CAMERA
import com.zzx.media.recorder.IRecorder
import com.zzx.utils.ExceptionHandler
import com.zzx.utils.rxjava.singleThread
import timber.log.Timber
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

/**@author Tomy
 * Created by Tomy on 2018/4/5.
 */
@SuppressLint("PrivateApi")
class Camera1Manager: ICameraManager<SurfaceHolder, Camera> {

    private var mCamera: Camera? = null


    private var mParameters: Parameters? = null

    private var mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK

    private var mPictureCount = 0

    private var mBurstMode = false

//    private val mIsRecording = AtomicBoolean(false)

    private val mRecordStarting = AtomicBoolean(false)

    private val mRecordStopping = AtomicBoolean(false)

//    private var mPreviewed = AtomicBoolean(false)

    private var mCameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK

    private var mPreviewSurface: SurfaceHolder? = null

    private var mPreviewTexture: SurfaceTexture? = null


    private var mPictureCallback: ICameraManager.PictureCallback? = null

    private var mRecordPreviewReady: ICameraManager.RecordPreviewReady? = null

    private var mPreviewDataCallback: ICameraManager.PreviewDataCallback? = null

    private var mHandlerThread: HandlerThread = HandlerThread(Camera1Manager::class.simpleName)
    private var mHandler: Handler? = null

    private var mFocusCallback: ICameraManager.AutoFocusCallback? = null

    private var mIsManualFocusSupported           = false
    private var mIsPictureAutoFocusSupported    = false
    private var mIsVideoAutoFocusSupported      = false

    private val mCameraCore = CameraCore<Camera>()

    private var mPreWidth   = 0
    private var mWidth      = 0

    private var mPreHeight  = 0
    private var mHeight     = 0

    private var mPrePreviewFormat = 0
    private var mPreviewFormat = 0

    private var mAllocateBufferSize = 0

    private var mAllocateBuffer: ByteArray? = null

//    private val mCameraOpening = AtomicBoolean(false)

    private val mMtkSetContinuousSpeedMethod by lazy {
        Camera::class.java.getDeclaredMethod("setContinuousShotSpeed", Integer::class.java)
    }

    private val mMtkCancelContinuousMethod by lazy {
        Camera::class.java.getDeclaredMethod("cancelContinuousShot")
    }

    init {
        /*mHandlerThread.start()
        mHandler = Handler(mHandlerThread.looper)*/
    }


    override fun openFrontCamera() = singleThread {
        try {
            if (!mCameraCore.canOpen()) {
                Timber.e("${Const.TAG}openExternalCamera() Failed")
                mStateCallback?.onCameraOpenFailed(mCameraCore.getStatus().ordinal)
                return@singleThread
            }
            mCameraCore.setStatus(Status.OPENING)
            mStateCallback?.onCameraOpening()
//            mCameraOpening.set(true)
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
                    mCameraFacing = info.facing
                    openSpecialCamera(i)
                    return@singleThread
                }
            }
        } catch (e: Exception) {
            ExceptionHandler.getInstance().saveException2File(e)
            e.printStackTrace()
            mCameraCore.setStatus(Status.RELEASE)
            mStateCallback?.onCameraOpenFailed(CAMERA_OPEN_ERROR_GET_INFO_FAILED)
        }
    }

    override fun openBackCamera() = singleThread {
        try {
            if (!mCameraCore.canOpen()) {
                Timber.e("${Const.TAG}openBackCamera() Failed")
                mStateCallback?.onCameraOpenFailed(mCameraCore.getStatus().ordinal)
                return@singleThread
            }
            mCameraCore.setStatus(Status.OPENING)
            mStateCallback?.onCameraOpening()
//            mCameraOpening.set(true)
            for (i in 0 until getCameraCount()) {
                val info = Camera.CameraInfo()
                Timber.d("${Const.TAG}getCameraInfo.id = $i")
                Camera.getCameraInfo(i, info)
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    Timber.e("${Const.TAG}openBackCamera()")
                    mCameraFacing = info.facing
                    openSpecialCamera(i)
                    return@singleThread
                }
            }
        } catch (e: Exception) {
            ExceptionHandler.getInstance().saveException2File(e)
            e.printStackTrace()
            mCameraCore.setStatus(Status.RELEASE)
            mStateCallback?.onCameraOpenFailed(CAMERA_OPEN_ERROR_GET_INFO_FAILED)
        }
    }

    override fun openSpecialCamera(cameraId: Int) {
        Timber.w("openSpecialCamera.cameraId = $cameraId. mCamera = $mCamera")
        /*if (mCameraOpening.get()) {
            return
        }
        mCameraOpening.set(true)*/
        if (mCamera != null) {
            mStateCallback?.onCameraOpenFailed(CAMERA_OPEN_ERROR_NOT_RELEASE)
//            mCameraOpening.set(false)
            return
        }
        mCameraId = cameraId
        if (getCameraCount() <= 0) {
            mStateCallback?.onCameraOpenFailed(CAMERA_OPEN_ERROR_NO_CAMERA)
//            mCameraOpening.set(false)
            mCameraCore.setStatus(Status.RELEASE)
            return
        }
        val id = if (getCameraCount() <= cameraId) {
            getCameraCount() - 1
        } else {
            cameraId
        }
        mIsVideoAutoFocusSupported = false
        mIsPictureAutoFocusSupported = false
        mIsManualFocusSupported = false
        mBurstMode = false
        Timber.i("${Const.TAG}cameraId = $cameraId; getCameraCount = ${getCameraCount()}")
        var openSuccess: Boolean
        try {
            mCamera = Camera.open(id)
            mCamera?.apply {
                mParameters = parameters
                mParameters?.apply {
                    supportedFocusModes.forEach {
                        Timber.d("focusMode = $it")
                        when (it) {
                            FOCUS_MODE_MANUAL   -> {
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
                    whiteBalance = Parameters.WHITE_BALANCE_AUTO
                    setParameter()
                }
                setErrorCallback {
                    error, _ ->
                    releaseCamera()
//                    mPreviewed.set(false)
                    mCamera = null
                    mStateCallback?.onCameraErrorClose(error)
                }
            }

            mCameraCore.setCamera(mCamera)
            mCameraCore.setParameters(mParameters)
            mBurstMode = false
            stopRecord()
            setFocusMode(Parameters.FOCUS_MODE_AUTO)
            openSuccess = true
            setDisplayOrientation(0)
            setPictureRotation(0)
//            mCameraOpening.set(false)
        } catch (e: Exception) {
            e.printStackTrace()
            openSuccess = false
            ExceptionHandler.getInstance(null, "Camera")
//            mCameraOpening.set(false)
            mCameraCore.setStatus(Status.RELEASE)
            mStateCallback?.onCameraOpenFailed(CAMERA_OPEN_ERROR_OPEN_FAILED)
        }
        if (openSuccess) {
            mCameraCore.setStatus(Status.OPENED)
            mStateCallback?.onCameraOpenSuccess(mCamera!!, mCameraId)
        }
    }

    override fun openExternalCamera() = singleThread {
        Timber.w("${Const.TAG}openExternalCamera(): mCamera = $mCamera")
        if (!mCameraCore.canOpen()) {
            Timber.e("${Const.TAG}openExternalCamera() Failed")
            mStateCallback?.onCameraOpenFailed(mCameraCore.getStatus().ordinal)
            return@singleThread
        }
//        mCameraOpening.set(true)
        mCameraCore.setStatus(Status.OPENING)
        mStateCallback?.onCameraOpening()
        openSpecialCamera(1)
    }

    override fun isCameraOpening(): Boolean {
        return mCameraCore.getStatus() == Status.OPENING
    }

    override fun getCameraCore(): CameraCore<Camera> {
        return mCameraCore
    }

    /**
     * 只设置了预览 Surface ,但是不会调用 [startPreview].
     * 此方法跟[startPreview]共同使用由自身决定何时启动预览.
     * */
    override fun setPreviewSurface(surface: SurfaceHolder) {
        Timber.i("startPreviewSurface(). mCamera = $mCamera")
        mPreviewTexture = null
        mPreviewSurface = surface
        mCamera?.setPreviewDisplay(mPreviewSurface)
    }

    override fun setPreviewSurfaceTexture(surfaceTexture: SurfaceTexture) {
        mPreviewSurface = null
        mPreviewTexture = surfaceTexture
        mCamera?.setPreviewTexture(mPreviewTexture)
    }

    private fun setPreviewCallback() {
        if (mWidth != mPreWidth || mHeight != mPreHeight || mPreviewFormat != mPrePreviewFormat) {
            mAllocateBuffer = null
            mAllocateBufferSize = (mWidth * mHeight * ImageFormat.getBitsPerPixel(mPreviewFormat)) / 8
            mAllocateBuffer = ByteBuffer.allocateDirect(mAllocateBufferSize).array()
            mPreWidth   = mWidth
            mPreHeight  = mHeight
            mPrePreviewFormat   = mPreviewFormat
        }
        mCamera?.addCallbackBuffer(mAllocateBuffer)
        mCamera?.setPreviewCallbackWithBuffer { data, _ ->
            data?.apply {
                mCamera?.addCallbackBuffer(mAllocateBuffer)
                Timber.v("onPreviewCallback.data.size ===== ${data.size}")
                mPreviewDataCallback?.onPreviewDataCallback(data, mPreviewFormat)
            }
        }
    }

    /**
     * 此方法调用之前必须先调用[setPreviewSurface],自行决定决定何时启动预览.
     * */
    override fun startPreview() {
        Timber.i("startPreview(). mCamera = $mCamera\nwidth x height = [${mWidth}x$mHeight]")
        if (mCameraCore.canPreview()) {
            try {
//                mPreviewDataCallback?.apply {
                mParameters?.apply {
                    previewFormat = mPreviewFormat
                    setPreviewSize(mWidth, mHeight)
                    setParameter()
                }
                setPreviewCallback()
//                }
                mCamera!!.apply {
                    startPreview()
                    mStateCallback?.onCameraPreviewSuccess()
                }
                mCameraCore.setStatus(Status.PREVIEW)
//                mPreviewed.set(mCamera != null)
            } catch (e: Exception) {
                e.printStackTrace()
                releaseCamera()
                mStateCallback?.onCameraOpenFailed(CAMERA_OPEN_ERROR_PREVIEW_FAILED)
            }
        }
    }

    override fun setPreviewDataCallback(previewDataCallback: ICameraManager.PreviewDataCallback?) {
        mPreviewDataCallback = previewDataCallback
    }

    /**
     * 等价于[setPreviewSurface]+[startPreview].
     * 设置完预览界面后即可启动预览.
     * */
    override fun startPreview(surface: SurfaceHolder) {
        synchronized(this) {
            Timber.i("startPreview.mStatus = ${mCameraCore.getStatus()}")
            if (mCameraCore.canPreview()) {
                setPreviewSurface(surface)
                startPreview()
                startAutoFocus()
            }
        }
    }

    override fun startPreview(surfaceTexture: SurfaceTexture) {
        synchronized(this) {
            Timber.i("startPreview.mStatus = ${mCameraCore.getStatus()}")
            if (mCameraCore.canPreview()) {
                setPreviewSurfaceTexture(surfaceTexture)
                startPreview()
                startAutoFocus()
            }
        }
    }

    override fun stopPreview() {
        try {
            if (mCameraCore.isPreview()) {
//                mPreviewDataCallback?.apply {
                    mCamera?.setPreviewCallbackWithBuffer(null)
//                }
                mCamera?.stopPreview()
                /*mPreviewSurface?.apply {
                    mCamera?.setPreviewDisplay(null)
                }
                mPreviewTexture?.apply {
                    mCamera?.setPreviewTexture(null)
                }*/
                mCameraCore.setStatus(Status.OPENED)
                mStateCallback?.onCameraPreviewStop()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun restartPreview() {
        stopPreview()
        mPreviewSurface?.apply {
            startPreview(this)
        }
        mPreviewTexture?.apply {
            startPreview(this)
        }
    }

    /**
     * 开始录像
     * */
    override fun startRecordPreview(surface: Surface?) {
    }

    /**
     * @see startPreview
     * @see stopRecord
     */
    override fun startRecord() {
        Timber.i("startRecord()")
        mCameraCore.setStatus(Status.RECORDING)
        setPreviewCallback()
//        mIsRecording.set(true)
    }

    override fun setIRecorder(recorder: IRecorder) {
    }

    override fun startAutoFocus(focusCallback: ICameraManager.AutoFocusCallback?) {
        if (mBurstMode || !mCameraCore.isPreview()) {
            return
        }
        try {
            Timber.e("startAutoFocus")
            cancelAutoFocus()
            focusCallback?.apply {
                mFocusCallback = this
            }
            mCamera?.autoFocus { success, _ ->
                Timber.e("autoFocus.success = $success")
//            cancelAutoFocus()
                if (mIsPictureAutoFocusSupported)
                    setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
                mFocusCallback?.onAutoFocusCallbackSuccess(success)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            CrashReport.postCatchedException(e)
        }
    }

    override fun setAutoFocusCallback(focusCallback: ICameraManager.AutoFocusCallback?) {
        mFocusCallback = focusCallback
    }

    override fun cancelAutoFocus() {
        try {
            mCamera?.cancelAutoFocus()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun focusOnRect(focusRect: Rect, focusCallback: ICameraManager.AutoFocusCallback?) {
        if (getMaxNumFocusAreas() > 0) {
            mParameters?.apply {
                ArrayList<Camera.Area>().let {
                    it.add(Camera.Area(focusRect, 1000))
                    meteringAreas = it
                    focusAreas  = it
                }
                focusMode = Parameters.FOCUS_MODE_AUTO
                mCamera?.parameters = this
            }
        }
        startAutoFocus(focusCallback)
    }

    override fun focusOnPoint(x: Int, y: Int, screenWidth: Int, screenHeight: Int, horWidth: Int, verHeight: Int, focusCallback: ICameraManager.AutoFocusCallback?) {
        if (!isManualFocusSupported() || (screenWidth == 0 || screenHeight == 0)) {
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
        return mParameters?.supportedFocusModes ?: ArrayList()
    }

    override fun setFocusMode(focusMode: String) {
        try {
            mParameters?.apply {
                this.focusMode = focusMode
            }
            setParameter()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun isManualFocusSupported(): Boolean {
        Timber.i("isManualFocusSupported = $mIsManualFocusSupported")
        return mIsManualFocusSupported
    }

    override fun isPictureAutoFocusSupported(): Boolean {
        return mIsPictureAutoFocusSupported
    }

    override fun isVideoAutoFocusSupported(): Boolean {
        return mIsVideoAutoFocusSupported
    }

    override fun getMaxNumFocusAreas(): Int {
        return mParameters?.maxNumFocusAreas ?: 0
    }

    /**
     * @see startRecord
     * 停止录像
     * */
    override fun stopRecord() {
        Timber.i("stopRecord().status = ${mCameraCore.getStatus()}")
        if (mCameraCore.isRecording()) {
            mCameraCore.setStatus(Status.OPENED)
        }
//        mIsRecording.set(false)
    }

    override fun closeCamera() {
        synchronized(this) {
            mIsVideoAutoFocusSupported = false
            mIsPictureAutoFocusSupported = false
            mIsManualFocusSupported = false
            mBurstMode = false
            mCameraCore.setStatus(Status.CLOSING)
            mStateCallback?.onCameraClosing()
            stopPreview()
//            stopRecord()
            mCamera?.release()
            mCamera = null
            mParameters = null
            mCameraCore.setStatus(Status.RELEASE)
            Timber.i("closeCamera. mCamera = $mCamera")
            mStateCallback?.onCameraClosed()
        }
    }

    override fun releaseCamera() {
        Timber.e("releaseCamera()")
        closeCamera()
    }

    override fun getCameraCount(): Int {
        return Camera.getNumberOfCameras()
    }

    override fun getSupportPreviewSizeList(): Array<Size> {
        val list = mParameters?.supportedPreviewSizes
        return if (list != null) {
            Array(list.size) {
                Size(list[it].width, list[it].height)
            }
        } else {
            Array(0) {
                Size(0, 0)
            }
        }
    }

    override fun getSupportPreviewFormatList(): Array<Int> {
        val list = mParameters?.supportedPreviewFormats
        return if (list != null) {
            Array(list.size) {
                list[it]
            }
        } else {
            Array(0) {
                0
            }
        }

    }

    override fun setPreviewParams(width: Int, height: Int, format: Int) {
        mWidth  = width
        mHeight = height
        mPreviewFormat  = format
    }

    override fun getSupportCaptureSizeList(): Array<Size> {
        val list = mParameters?.supportedPictureSizes
        return if (list != null) {
            Array(list.size) {
                Size(list[it].width, list[it].height)
            }
        } else {
            Array(0) {
                Size(0, 0)
            }
        }

    }

    override fun getSupportCaptureFormatList(): Array<Int> {
        val list = mParameters?.supportedPictureFormats
        return if (list != null) {
            Array(list.size) {
                list[it]
            }
        } else {
            Array(0) {
                0
            }
        }
    }

    override fun setCaptureParams(width: Int, height: Int, format: Int) {
        try {
            mParameters?.apply {
                if (mCameraCore.isRecording()) {
                    Timber.e("setCaptureParams Camera is Recording; isVssSupported = $isVideoSnapshotSupported")
                    if (!isVideoSnapshotSupported) {
                        return
                    }
                }
                pictureFormat = format
                setPictureSize(width, height)
                mCamera?.parameters = this
            }
        } catch (e: Exception) {
            e.printStackTrace()
            CrashReport.postCatchedException(e)
        }

    }

    override fun getSupportRecordSizeList(): Array<Size> {
        val list = mParameters?.supportedVideoSizes
        return if (list != null) {
            Array(list.size) {
                Size(list[it].width, list[it].height)
            }
        } else {
            Array(0) {
                Size(0, 0)
            }
        }
    }

    override fun getSensorOrientation(): Int {
        return if (mCameraFacing != Camera.CameraInfo.CAMERA_FACING_BACK)
            SENSOR_FRONT_CAMERA
        else
            SENSOR_BACK_CAMERA
    }

    override fun takePicture(callback: ICameraManager.PictureCallback?) {
        setPictureCallback(callback)
        takePicture()
    }

    override fun takePictureBurst(count: Int, callback: ICameraManager.PictureCallback?) {
        mPictureCallback = callback
        takePictureBurst(count)
    }

    override fun startContinuousShot(count: Int, callback: ICameraManager.PictureCallback?) {
        takePictureBurst(count, callback)
    }

    override fun cancelContinuousShot() {
        try {
            mMtkCancelContinuousMethod.invoke(mCamera)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * @param speed Int : the speed set for continuous shot(xx fps)
     */
    override fun setContinuousShotSpeed(speed: Int) {
        try {
            mMtkSetContinuousSpeedMethod.invoke(mCamera, speed)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun setPictureCallback(callback: ICameraManager.PictureCallback?) {
        mPictureCallback = callback
    }

    override fun setRecordPreviewCallback(callback: ICameraManager.RecordPreviewReady?) {
        mRecordPreviewReady = callback
    }

    override fun takePicture() {
        /*if (!mCameraCore.isRecording())
            setPictureNormalMode()*/
        mContinuousShotCount = 0
        startTakePicture()
    }

    override fun takePictureBurst(count: Int) {
        /*if (!mCameraCore.isRecording())
            setPictureContinuousMode(count)*/
        mContinuousShotCount = count
        startTakePicture()
    }

    /**
     * 在当前的缩放下放大镜头的Level
     * @param level Int +level
     */
    override fun zoomUp(level: Int) {
        try {
            mParameters?.apply {
                if (!isZoomSupported || zoom == getZoomMax()) {
                    return
                }
                val zoomLevel = zoom + level
                zoom = if (zoomLevel <= getZoomMax()) zoomLevel else getZoomMax()
            }
            setParameter()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun setParameter() {
        try {
            mParameters?.apply {
                mCamera?.parameters = this
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 在当前的缩放倍数下缩小镜头的Level
     * @param level Int -Level
     */
    override fun zoomDown(level: Int) {
        mParameters?.apply {
            if (!isZoomSupported || zoom == 0) {
                return
            }
            val zoomLevel = zoom - level
            zoom = if (zoomLevel >= 0) zoomLevel else 0
        }
        setParameter()
    }

    /***
     * @return Int 获得可放大的最大倍数.
     */
    override fun getZoomMax(): Int {
        return mParameters?.maxZoom ?: 0
    }

    /**
     * @param level Int 设置的缩放倍数.level不得小于零以及不得超过最大放大倍数,可通过[getZoomMax]获得最大放大倍数.否则无效.
     */
    override fun setZoomLevel(level: Int) {
        if (level < 0 || level > getZoomMax())
            return
        mParameters?.apply {
            zoom = level
        }
        setParameter()
    }

    private fun startTakePicture() = singleThread {
        mPictureCount = 0
        if (mCameraCore.isCapturing()) {
            mContinuousShotCount = 0
            mPictureCallback?.onCaptureError(ICameraManager.PictureCallback.ERROR_CODE_CAPTURING)
            return@singleThread
        }
        if (mCameraCore.isRecording()) {
            val vssSupported = mParameters?.isVideoSnapshotSupported ?: false
            Timber.e("startTakePicture Camera is Recording; isVssSupported = $vssSupported")
            if (!vssSupported) {
                mPictureCallback?.onCaptureError(ICameraManager.PictureCallback.ERROR_CODE_NOT_SUPPORT_VIDEO_CAPTURE)
                return@singleThread
            } else {
                mContinuousShotCount = 0
            }
        }
        mPictureCallback?.onCaptureStart()
        if (mContinuousShotCount > 1) {
            setPictureContinuousMode(mContinuousShotCount)
        } else {
            mContinuousShotCount = 0
            setPictureNormalMode()
        }
        mCameraCore.setStatus(if (mCameraCore.isRecording()) Status.RECORDING_CAPTURING else Status.CAPTURING)
        try {
            Timber.e("takePicture start")
            mCamera!!.takePicture(null, null, mPictureDataCallback)
        } catch (e: Exception) {
            if (!mCameraCore.isRecording()) {
                mCameraCore.setStatus(Status.PREVIEW)
            } else {
                mCameraCore.setStatus(Status.RECORDING)
            }
            mPictureCallback?.onCaptureDone()
        }
        /*if (mCamera == null) {
            mPictureDataCallback?.onCaptureDone()
        } else {
            mCameraCore.setStatus(Status.CAPTURING)
            mCamera?.takePicture(null, null, mPictureCallback)
        }*/

    }

    private val mPictureDataCallback =
        Camera.PictureCallback { data, _ ->
            mPictureCallback?.onCaptureResult(data)
            Timber.e("mPictureCount = $mPictureCount; mBurstMode = $mBurstMode; mContinuousShotCount = $mContinuousShotCount; mIsRecording = ${mCameraCore.isRecording()}")
            if (mBurstMode && !mCameraCore.isRecording()) {
                if (++mPictureCount >= mContinuousShotCount) {
                    if (!mCameraCore.isRecording()) {
//                        mPreviewed.set(false)
                        mCameraCore.setStatus(Status.OPENED)
                        startPreview()
                    } else {
                        mCameraCore.setStatus(Status.RECORDING)
                    }
                    mPictureCallback?.onCaptureDone()
                    mContinuousShotCount = 0

                }
            } else {
                if (!mCameraCore.isRecording()) {
//                    mPreviewed.set(false)
                    mCameraCore.setStatus(Status.OPENED)
                    startPreview()
                } else {
                    mCameraCore.setStatus(Status.RECORDING)
                }
                mPictureCallback?.onCaptureDone()
            }
        }
//    }


    /**
     * @param rotation Int 预览界面的旋转角度
     */
    override fun setDisplayOrientation(rotation: Int) {
        Timber.e("setDisplayOrientation.rotation = $rotation")
        try {
            mCamera?.setDisplayOrientation(rotation)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * @param rotation Int 图片的旋转角度
     */
    override fun setPictureRotation(rotation: Int) {
        Timber.e("setPictureRotation.rotation = $rotation. mParameters = $mParameters")
        mParameters?.apply {
            setRotation(rotation)
        }
        setParameter()
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
            mParameters?.apply {
                set(CAP_MODE, CAP_MODE_NORMAL)
                set(BURST_NUM, 1)
                set(MTK_CAM_MODE, CAMERA_MODE_NORMAL)
            }
            setParameter()
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
        Timber.w("setPictureContinuousMode. pictureCount = $pictureCount; mBurstMode = $mBurstMode")
        mContinuousShotCount = pictureCount
        if (!mBurstMode) {
            mBurstMode = true
            mParameters?.apply {
                set(CAP_MODE, CAP_MODE_CONTINUOUS)
                set(BURST_NUM, pictureCount)
                set(MTK_CAM_MODE, CAMERA_MODE_MTK_PRV)
            }
            setParameter()
            restartPreview()
        } else {
            mParameters?.apply {
                set(BURST_NUM, pictureCount)
            }
            setParameter()
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