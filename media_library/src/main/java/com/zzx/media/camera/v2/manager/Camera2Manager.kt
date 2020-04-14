package com.zzx.media.camera.v2.manager

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.ImageReader
import android.media.MediaCodec
import android.media.MediaRecorder
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface
import com.zzx.media.camera.CameraCore
import com.zzx.media.camera.ICameraManager
import com.zzx.media.recorder.IRecorder
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean


/**@author Tomy
 * Created by Tomy on 2018/4/3.
 */
class Camera2Manager(var context: Context): ICameraManager<SurfaceTexture, CameraDevice>, CameraDevice.StateCallback() {


    override fun getSensorOrientation(): Int {
        return getBackCameraConfiguration()!!.get(CameraCharacteristics.SENSOR_ORIENTATION)!!
    }

    private var mCamera: CameraDevice? = null

    private var mCameraClosed = AtomicBoolean(false)

    private val mObject = Object()

    private val mSurfaceList = ArrayList<Surface>()

    private var mPreviewSurface: Surface? = null

    private var mPreviewSurfaceTexture: SurfaceTexture? = null

    private var mPreviewSession: CameraCaptureSession? = null

    private var mVideoRecorder: IRecorder? = null

    private var mPreviewBuilder: CaptureRequest.Builder? = null
    private var mRecordBuilder: CaptureRequest.Builder? = null

    private var mPictureRequest = lazy {
        mCamera!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
            addTarget(mCaptureReader!!.surface)
            set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            set(CaptureRequest.JPEG_ORIENTATION, 0)
            setTag(RequestTag.CAPTURE)
        }.build()
    }

    private lateinit var mCameraId: String

    private var mHandler: Handler

    private var mHandlerThread: HandlerThread = HandlerThread(Camera2Manager::class.simpleName)

    private var mCaptureReader: ImageReader? = null

    private var mCaptureWidth   = 0
    private var mCaptureHeight  = 0
    private var mCaptureFormat  = 0

    private var mPictureCount: Int = 0

    private var mYuvReader: ImageReader? = null

    private var mCameraStateCallback: ICameraManager.CameraStateCallback<CameraDevice>? = null

    private var mPictureCallback: ICameraManager.PictureCallback? = null

    private var mRecordPreviewReady: ICameraManager.RecordPreviewReady? = null

    private var mCameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    private val mCameraCore = CameraCore<CameraDevice>()


    init {
        mHandlerThread.start()
        mHandler = Handler(mHandlerThread.looper)
    }

    /**
     * @see startPreview
     * */
    override fun closeCamera() {
//        stopPreview()
        synchronized(mObject) {
            Timber.e("closeCamera: mCameraClosed:${mCameraClosed.get()}")
            mCameraClosed.set(true)
            mCamera!!.close()
            mCamera = null
            Timber.e("closeCamera: mCameraClosed:${mCameraClosed.get()}")
        }
        mCaptureReader!!.close()
        mCaptureReader = null
    }

    private var mDimension: Size? = null

    override fun getSupportPreviewSizeList(): Array<Size> {
        val map = getStreamConfigurationMap(mCameraId)
        val dimension = map.getOutputSizes(SurfaceTexture::class.java)
        for (size in dimension) {
            Timber.e("getSupportPreviewSizeList = [${size.width}]x[${size.height}]")
        }
        return dimension
    }

    override fun getSupportPreviewFormatList(): Array<Int> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * @see getCameraProperty
     * */
    private fun getStreamConfigurationMap(cameraId: String): StreamConfigurationMap {
        return getCameraProperty(cameraId).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
    }

    override fun getSupportCaptureSizeList(): Array<Size> {
        val map = getStreamConfigurationMap(mCameraId)
        val sizeList = map.getOutputSizes(ImageFormat.JPEG)
        for (size in sizeList) {
            Timber.e("JPEGSupportCaptureSizeList = [${size.width}]x[${size.height}]")
        }

        val list = map.getOutputSizes(ImageFormat.YUV_420_888)
        for (size in list) {
            Timber.e("YUV_420_888CaptureSizeList = [${size.width}]x[${size.height}]")
        }

        val listReader = map.getOutputSizes(ImageReader::class.java)
        for (size in listReader) {
            Timber.e("ImageReaderCaptureSizeList = [${size.width}]x[${size.height}]")
        }
        Timber.e("supportLevel = ${getCameraProperty(mCameraId).get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)}")
        return sizeList
    }

    override fun getSupportCaptureFormatList(): Array<Int> {
        val map = getStreamConfigurationMap(mCameraId)
        val formatArray = map.outputFormats
        for (outputFormat in map.outputFormats) {
            Timber.e("outputFormat = $outputFormat")
            if (map.isOutputSupportedFor(outputFormat)) {
                Timber.e("------> Support <------")
            } else {
                Timber.e(" Not Support ")
            }
        }

        for (highSpeedVideoFpsRanges in map.highSpeedVideoFpsRanges) {
            Timber.e("highSpeedVideoFpsRanges = $highSpeedVideoFpsRanges")
        }

        for (highSpeedVideoSizes in map.highSpeedVideoSizes) {
            Timber.e("highSpeedVideoSizes = ${highSpeedVideoSizes.width}x${highSpeedVideoSizes.height}")
        }

        return formatArray.toTypedArray()
    }

    override fun getSupportRecordSizeList(): Array<Size> {
        val map = getStreamConfigurationMap(mCameraId)
        val sizeList = map.getOutputSizes(MediaRecorder::class.java)
        for (size in sizeList) {
            Timber.e("recordSize = ${size.width}x${size.height}")
        }
        val list = map.getOutputSizes(MediaCodec::class.java)
        for (size in list) {
            Timber.e("recordSize = ${size.width}x${size.height}")
        }
        return sizeList
    }

    /**
     * @see takePictureBurst
     * @see setupCaptureReader
     * */
    override fun takePicture(callback: ICameraManager.PictureCallback?) {
        Timber.e("sensorOrientation = ${getSensorOrientation()}")
        mPictureCallback = callback
        takePicture()
    }

    /**
     * @see startPreview
     * @see takePictureBurst
     * @see setupCaptureReader
     * */
    override fun takePictureBurst(count: Int, callback: ICameraManager.PictureCallback?) {
        mPictureCallback = callback
        takePictureBurst(count)
    }

    override fun startContinuousShot(count: Int, callback: ICameraManager.PictureCallback?) {
        takePictureBurst(count, callback)
    }

    override fun cancelContinuousShot() {
    }

    override fun setContinuousShotSpeed(speed: Int) {

    }

    override fun setPictureCallback(callback: ICameraManager.PictureCallback?) {
        mPictureCallback = callback
    }


    /**
     * @see startRecordPreview
     * */
    override fun takePicture() {
        mPictureCount = 1

        /*mPictureRequest = mCamera!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
            addTarget(mCaptureReader!!.surface)
            set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            set(CaptureRequest.JPEG_ORIENTATION, 0)
            setTag(RequestTag.CAPTURE)
        }.build()*/

        mPreviewSession!!.capture(mPictureRequest.value, null, mHandler)
    }

    override fun takePictureBurst(count: Int) {
        mPictureCount = count
        val list = ArrayList<CaptureRequest>()
        for (i in 0 until count) {
            val builder = mCamera!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            builder.addTarget(mCaptureReader!!.surface)
            builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            builder.setTag(RequestTag.BURST_CAPTURE)
//            builder.set(CaptureRequest.JPEG_ORIENTATION, 270)
            list.add(builder.build())
        }
        mPreviewSession!!.captureBurst(list, CaptureCallback(), mHandler)
    }

    /**
     * 在当前的缩放下放大镜头的Level
     * @param level Int +level
     */
    override fun zoomUp(level: Int) {
    }

    /**
     * 在当前的缩放倍数下缩小镜头的Level
     * @param level Int -Level
     */
    override fun zoomDown(level: Int) {
    }

    /***
     * @return Int 获得可放大的最大倍数.
     */
    override fun getZoomMax(): Int {
        return 0
    }

    /**
     * @param level Int 设置的缩放倍数.不得超过最大放大倍数,可通过[getZoomMax]获得最大放大倍数.
     */
    override fun setZoomLevel(level: Int) {
    }

    private fun getCameraProperty(id: String): CameraCharacteristics {
        return mCameraManager.getCameraCharacteristics(id)
    }

    /**
     * @see setupCaptureReader
     * */
    override fun setCaptureParams(width: Int, height: Int, format: Int) {
        mCaptureWidth   = width
        mCaptureHeight  = height
        mCaptureFormat  = format
        setupCaptureReader()
    }

    /**
     * @see startPreview
     * */
    override fun setPreviewParams(width: Int, height: Int, format: Int) {
        val list = getSupportPreviewSizeList()
        val listIncrease = Array(list.size) {
            i ->
            Size(i, i)
        }
        var index = 0
        for (i in list.size - 1 downTo 0) {
            listIncrease[index++] = list[i]
        }
//        mDimension = ICameraManager.getPreviewSize(width, height, listIncrease, false)
        mDimension = Size(width, height)
        Timber.e("preSize = ${mDimension?.width}x${mDimension?.height}")
    }

    /**
     * @see setCaptureParams
     * @see takePicture
     * @see takePictureBurst
     * */
    private fun setupCaptureReader() {
        Timber.e("setupCaptureReader = ${mCaptureWidth}x$mCaptureHeight, format = $mCaptureFormat")
        mCaptureReader = ImageReader.newInstance(mCaptureWidth, mCaptureHeight, mCaptureFormat, 2).apply {
            setOnImageAvailableListener({
                reader: ImageReader? ->
                val image = reader!!.acquireNextImage()
//            mPictureCallback?.onCaptureFinished(ImageUtils.NV21toJPEG(ImageUtils.YUV420toNV21(image), 800, 600, 100))
                val array = ByteArray(image.planes[0].buffer.remaining())
                image.planes[0].buffer.get(array)
                mPictureCallback?.onCaptureResult(array)
                Timber.e("image = ${image.width}x${image.height}")
                image.close()
            }, mHandler)
        }

        Timber.e("CaptureReader = ${mCaptureReader!!.width}x${mCaptureReader!!.height}, format = ${mCaptureReader!!.imageFormat}")
    }

    private fun setupPreviewReader() {

    }

    override fun setStateCallback(stateCallback: ICameraManager.CameraStateCallback<CameraDevice>) {
        mCameraStateCallback = stateCallback
    }

    /**
     * @param rotation Int 预览界面的旋转角度
     */
    override fun setDisplayOrientation(rotation: Int) {

    }

    /**
     * @param rotation Int 图片的旋转角度
     */
    override fun setPictureRotation(rotation: Int) {
    }

    /**
     * @param enable Boolean 是否打开拍照声音
     */
    override fun enableShutter(enable: Boolean) {
    }

    override fun openBackCamera() {
        mCameraId = getSpecialCameraId(CameraCharacteristics.LENS_FACING_BACK)
        openSpecialCamera(mCameraId)
    }

    override fun openFrontCamera() {
        mCameraId = getSpecialCameraId(CameraCharacteristics.LENS_FACING_FRONT)
        openSpecialCamera(mCameraId)
    }

    override fun openExternalCamera() {
        mCameraId = getSpecialCameraId(CameraCharacteristics.LENS_FACING_EXTERNAL)
        openSpecialCamera(mCameraId)
    }

    override fun isCameraOpening(): Boolean {
        return false
    }

    @SuppressLint("MissingPermission")
    fun openSpecialCamera(id: String) {
        if (id == "") {
            return
        }

        try {
            mCameraManager.openCamera(id, this, mHandler)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun openSpecialCamera(cameraId: Int) {
    }

    /**
     * @see setPreviewParams
     * */
    override fun setPreviewSurface(surface: SurfaceTexture) {
        mPreviewSurfaceTexture = surface
    }

    override fun setPreviewSurfaceTexture(surfaceTexture: SurfaceTexture) {
        setPreviewSurface(surfaceTexture)
    }

    private fun initSurface() {
        Timber.e("width x height = [${mDimension!!.width}x${mDimension!!.height}]")
        mPreviewSurfaceTexture!!.setDefaultBufferSize(mDimension!!.width, mDimension!!.height)
        mPreviewSurface = Surface(mPreviewSurfaceTexture)
    }

    override fun setPreviewDataCallback(previewDataCallback: ICameraManager.PreviewDataCallback?) {
    }

    /**
     * @see takePictureBurst
     * @see stopPreview
     * */
    override fun startPreview(surface: SurfaceTexture) {
        setPreviewSurface(surface)
        startPreview()
    }

    /**
     * @see startRecordPreview
     * @see stopPreview
     * */
    override fun startPreview() {
        Timber.e("startPreview: mCameraClosed:${mCameraClosed.get()}")
        synchronized(mObject) {
            if (mCameraClosed.get()) {
                return
            }
            stopPreview()
            initSurface()

            mSurfaceList.apply {
                clear()
                add(mCaptureReader!!.surface)
                add(mPreviewSurface!!)
            }
            mCamera?.createCaptureSession(mSurfaceList, object : CameraCaptureSession.StateCallback() {

                override fun onConfigureFailed(session: CameraCaptureSession?) {
                }

                override fun onConfigured(session: CameraCaptureSession?) {
                    mPreviewSession = session
                    sendPreviewRequest()
                }
            }, mHandler)
        }
    }

    /**
     * @see takePicture
     * @see takePictureBurst
     * @see sendRecordPreviewRequest
     * */
    private fun sendPreviewRequest() {
        synchronized(mObject) {
            Timber.e("sendPreviewRequest: ${mCameraClosed.get()}")
            if (mCameraClosed.get()) {
                return
            }
            if (mPreviewBuilder == null) {
                mPreviewBuilder = mCamera!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                    addTarget(mPreviewSurface!!)
                    set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
                    setTag(RequestTag.PREVIEW)
                }
            }
            mPreviewSession?.setRepeatingRequest(mPreviewBuilder?.build(), null, mHandler)
        }
    }

    /**
     * @see startPreview
     * @see closeCamera
     * */
    override fun stopPreview() {
        Timber.e("stopPreview: mCameraClosed:${mCameraClosed.get()}")
        if (!mCameraClosed.get()) {
            mPreviewSession?.abortCaptures()
            mPreviewSession?.close()
            mPreviewSession = null
        }
    }

    override fun restartPreview() {
    }

    override fun setIRecorder(recorder: IRecorder) {
        mVideoRecorder = recorder
    }

    override fun setRecordPreviewCallback(callback: ICameraManager.RecordPreviewReady?) {
        mRecordPreviewReady = callback
    }

    /**
     * 开始录像
     * */
    override fun startRecordPreview(surface: Surface?) {
        if (mCamera == null || mCameraClosed.get()) {
            return
        }

        stopPreview()
        initSurface()
        mSurfaceList.apply {
            clear()
            add(mCaptureReader!!.surface)
            add(mPreviewSurface!!)
            add(surface!!)
        }

        mCamera?.createCaptureSession(mSurfaceList, object : CameraCaptureSession.StateCallback() {
            /**
             * This method is called if the session cannot be configured as requested.
             * @param session the session returned by [CameraDevice.createCaptureSession]
             */
            override fun onConfigureFailed(session: CameraCaptureSession?) {

            }

            /**
             *
             * @param session the session returned by [CameraDevice.createCaptureSession]
             */
            override fun onConfigured(session: CameraCaptureSession?) {
                mPreviewSession = session
                sendRecordPreviewRequest(surface!!)
                mRecordPreviewReady?.onRecordPreviewReady()
            }

        }, mHandler)
    }

    inner class SessionStateCallback: CameraCaptureSession.StateCallback() {

        override fun onConfigureFailed(session: CameraCaptureSession?) {

        }

        override fun onConfigured(session: CameraCaptureSession?) {

        }

        override fun onReady(session: CameraCaptureSession?) {
            super.onReady(session)
        }

        override fun onClosed(session: CameraCaptureSession?) {
            super.onClosed(session)

        }

    }

    /**
     * 停止录像
     * */
    override fun stopRecord() {
    }

    override fun startRecord() {
    }

    override fun startAutoFocus(focusCallback: ICameraManager.AutoFocusCallback?) {
    }

    override fun setAutoFocusCallback(focusCallback: ICameraManager.AutoFocusCallback?) {
    }

    override fun focusOnRect(focusRect: Rect, focusCallback: ICameraManager.AutoFocusCallback?) {
    }

    override fun focusOnPoint(x: Int, y: Int, screenWidth: Int, screenHeight: Int, horWidth: Int, verHeight: Int, focusCallback: ICameraManager.AutoFocusCallback?) {
    }

    override fun cancelAutoFocus() {
    }

    override fun getSupportFocusMode(): List<String> {
        return emptyList()
    }

    override fun setFocusMode(focusMode: String) {
    }

    override fun isManualFocusSupported(): Boolean {
        return false
    }

    override fun isVideoAutoFocusSupported(): Boolean {
        return false
    }

    override fun isPictureAutoFocusSupported(): Boolean {
        return false
    }

    override fun getFocusRect(): List<Rect> {
        return emptyList()
    }

    override fun getMaxNumFocusAreas(): Int {
        return 0
    }

    override fun getCameraCore(): CameraCore<CameraDevice> {
        return mCameraCore
    }

    /**
     * @see sendPreviewRequest
     * */
    private fun sendRecordPreviewRequest(recordSurface: Surface) {
        mRecordBuilder = mCamera!!.createCaptureRequest(CameraDevice.TEMPLATE_RECORD).apply {
            addTarget(recordSurface)
            addTarget(mPreviewSurface)
            setTag(RequestTag.RECORD)
        }
        mPreviewSession?.setRepeatingRequest(mRecordBuilder?.build(), null, mHandler)
    }


    override fun releaseCamera() {

    }

    override fun getCameraDevice(): CameraDevice? {
        return mCamera
    }


    enum class RequestTag {
        PREVIEW,
        CAPTURE,
        BURST_CAPTURE,
        RECORD
    }

    /**
     * @see CameraDevice.StateCallback
     * */
    override fun onOpened(camera: CameraDevice?) {
        mCamera = camera
        mCameraStateCallback?.onCameraOpenSuccess(camera!!, 0)
    }

    override fun onDisconnected(camera: CameraDevice?) {
        camera?.close()
        mCamera = null
    }

    override fun onError(camera: CameraDevice?, error: Int) {
        mCameraStateCallback?.onCameraErrorClose(error)
        camera?.close()
        mCamera = null
    }

    override fun onClosed(camera: CameraDevice?) {
        mCameraStateCallback?.onCameraClosed()
    }


    override fun getCameraCount(): Int {
        return mCameraManager.cameraIdList.size
    }

    fun getCameraIdList(): Array<out String>? {
        return mCameraManager.cameraIdList
    }

    fun getCameraConfiguration(id: String): CameraCharacteristics? {
        return mCameraManager.getCameraCharacteristics(id)
    }

    fun getBackCameraConfiguration(): CameraCharacteristics? {
        return getCameraConfiguration(getSpecialCameraId(CameraCharacteristics.LENS_FACING_BACK))
    }

    /**
     * 获得特定的摄像头.后置,前置及外置.
     * @see CameraCharacteristics.LENS_FACING_FRONT
     * @see CameraCharacteristics.LENS_FACING_BACK
     * @see CameraCharacteristics.LENS_FACING_EXTERNAL
     * @see getCameraProperty
     * */
    fun getSpecialCameraId(lensFacing: Int): String {
        for (id in getCameraIdList()!!) {
            val characteristics = mCameraManager.getCameraCharacteristics(id)
            if (lensFacing == characteristics.get(CameraCharacteristics.LENS_FACING)) {
                return id
            }
        }
        return ""
    }

    inner class CaptureCallback: CameraCaptureSession.CaptureCallback() {
        var mPictureCounter = 0

        override fun onCaptureStarted(session: CameraCaptureSession?, request: CaptureRequest?, timestamp: Long, frameNumber: Long) {
            super.onCaptureStarted(session, request, timestamp, frameNumber)
            Timber.e("onCaptureStarted.mPictureCount ====== $mPictureCounter")
        }

        /**
         * @see takePictureBurst
         * @see sendPreviewRequest
         * @see setupCaptureReader
         * */
        override fun onCaptureCompleted(session: CameraCaptureSession?, request: CaptureRequest?, result: TotalCaptureResult?) {
            super.onCaptureCompleted(session, request, result)
            Timber.e("onCaptureCompleted.mPictureCount ====== $mPictureCounter")
            when(request!!.tag) {
                RequestTag.CAPTURE  -> {
//                    sendPreviewRequest()
                }
                RequestTag.RECORD   -> {}
                RequestTag.PREVIEW  -> {}
                RequestTag.BURST_CAPTURE -> {
                    if (++mPictureCounter >= mPictureCount) {
//                        sendPreviewRequest()
                    }
                }
            }
        }

        override fun onCaptureProgressed(session: CameraCaptureSession?, request: CaptureRequest?, partialResult: CaptureResult?) {
            super.onCaptureProgressed(session, request, partialResult)
            Timber.e("onCaptureProgressed.mPictureCount ====== $mPictureCounter")
        }

        override fun onCaptureFailed(session: CameraCaptureSession?, request: CaptureRequest?, failure: CaptureFailure?) {
            super.onCaptureFailed(session, request, failure)
            Timber.e("onCaptureFailed.mPictureCount ====== $mPictureCounter")
        }

        override fun onCaptureSequenceCompleted(session: CameraCaptureSession?, sequenceId: Int, frameNumber: Long) {
            super.onCaptureSequenceCompleted(session, sequenceId, frameNumber)
            Timber.e("onCaptureSequenceCompleted.mPictureCount ====== $mPictureCounter")
        }
    }

    companion object {
        val PICTURE_BURST_MAX = 20
    }

}