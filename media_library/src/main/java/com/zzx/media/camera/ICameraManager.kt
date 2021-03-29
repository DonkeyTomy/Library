package com.zzx.media.camera
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.camera2.CameraDevice
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import com.zzx.media.recorder.IRecorder
import timber.log.Timber

/**
 * Camera对象的管理类接口.
 * [surface]代表的是设置的Surface类.
 * @see Surface
 * @see SurfaceTexture
 *
 * [camera]代表的是Camera类.
 * @see CameraDevice
 * @see Camera
 *
 * @author Tomy
 * Created by Tomy on 2018/4/4.
 */
interface ICameraManager<in surface, camera> {

    fun openFrontCamera()

    fun openBackCamera()

    fun openExternalCamera()

    fun isCameraOpening(): Boolean

    fun openSpecialCamera(cameraId: Int = 0)

    /**
     * 只设置了预览 Surface ,但是不会调用 [startPreview].
     * 此方法跟[startPreview]共同使用由自身决定何时启动预览.
     * */
    fun setPreviewSurface(surface: surface)

    fun setPreviewSurfaceTexture(surfaceTexture: SurfaceTexture)

    /**
     * 此方法调用之前必须先调用[setPreviewSurface],自行决定决定何时启动预览.
     * */
    fun startPreview()

    fun setPreviewDataCallback(previewDataCallback: PreviewDataCallback?)

    /**
     * 等价于[setPreviewSurface]+[startPreview].
     * 设置完预览界面后即可启动预览.
     * */
    fun startPreview(surface: surface)

    fun startPreview(surfaceTexture: SurfaceTexture)

    fun stopPreview()

    /**
     * 开始录像
     * */
    fun startRecordPreview(surface: Surface?)

    fun startRecord()

    fun setIRecorder(recorder: IRecorder)

    /**
     * 开始自动对焦
     */
    fun startAutoFocus(focusCallback: AutoFocusCallback? = null)

    /**
     * 设置自动对焦回调,若已设置,则[startAutoFocus]可以不再传入
     * @param focusCallback AutoFocusCallback?
     */
    fun setAutoFocusCallback(focusCallback: AutoFocusCallback?)

    /**
     * 停止自动对焦
     */
    fun cancelAutoFocus()

    /**
     * @param focusRect 设置定点对焦的区域
     */
    fun focusOnRect(focusRect: Rect, focusCallback: AutoFocusCallback? = null)

    /**
     * @param x 在Camera窗口X相对坐标
     * @param y 在Camera窗口Y相对坐标
     * @param screenWidth Camera窗口的宽度
     * @param screenHeight Camera窗口的高度
     * @param horWidth 区域相对中心的左右扩展(-horWidth, horWidth)默认为100
     * @param verHeight 区域相对中心的上下扩展.(-verHeight, verHeight)默认为100
     */
    fun focusOnPoint(x: Int, y: Int, screenWidth: Int, screenHeight: Int, horWidth: Int = 100, verHeight: Int = 100, focusCallback: ICameraManager.AutoFocusCallback? = null)

    /**
     * @return 获得可定点对焦的区域数.
     */
    fun getMaxNumFocusAreas(): Int

    /**
     * @return 获得定点对焦的区域.
     */
    fun getFocusRect(): List<Rect>

    fun getSupportFocusMode(): List<String>

    fun setFocusMode(focusMode: String)

    fun isManualFocusSupported(): Boolean

    fun isVideoAutoFocusSupported(): Boolean

    fun isPictureAutoFocusSupported(): Boolean

    fun getCameraCore(): CameraCore<camera>

    /**
     * 停止录像
     * */
    fun stopRecord()

    fun closeCamera()

    fun releaseCamera()

    fun getCameraCount(): Int

    fun getSupportPreviewSizeList(): Array<Size>

    fun getSupportPreviewFormatList(): Array<Int>

    fun setPreviewParams(width: Int, height: Int, format: Int)

    fun getSupportCaptureSizeList(): Array<Size>

    fun getSupportCaptureFormatList(): Array<Int>

    fun setCaptureParams(width: Int, height: Int, format: Int)

    fun getSupportRecordSizeList(): Array<Size>

    fun getSensorOrientation(): Int

    fun takePicture(callback: PictureCallback? = null)

    fun takePictureBurst(count: Int, callback: PictureCallback? = null)

    fun startContinuousShot(count: Int, callback: PictureCallback? = null)

    fun cancelContinuousShot()

    /**
     * @param speed Int : the speed set for continuous shot(xx fps)
     */
    fun setContinuousShotSpeed(speed: Int)

    fun setPictureCallback(callback: PictureCallback?)

    fun setRecordPreviewCallback(callback: RecordPreviewReady?)

    fun takePicture()

    fun takePictureBurst(count: Int)

    /**
     * @param rotation Int 预览界面的旋转角度
     */
    fun setDisplayOrientation(rotation: Int)

    /**
     * @param rotation Int 图片的旋转角度
     */
    fun setPictureRotation(rotation: Int)

    /**
     * @param enable Boolean 是否打开拍照声音
     */
    fun enableShutter(enable: Boolean)

    /**
     * 在当前的缩放下放大镜头的Level
     * @param level Int +level
     */
    fun zoomUp(level: Int = 1)

    /**
     * 在当前的缩放倍数下缩小镜头的Level
     * @param level Int -Level
     */
    fun zoomDown(level: Int = 1)

    /***
     * @return Int 获得可放大的最大倍数.
     */
    fun getZoomMax(): Int

    /**
     * @param level Int 设置的缩放倍数.不得超过最大放大倍数,可通过[getZoomMax]获得最大放大倍数.
     */
    fun setZoomLevel(level: Int)

    /**
     * @param stateCallback CameraStateCallback<camera> 设置相机状态回调
     */
    fun setStateCallback(stateCallback: CameraStateCallback<camera>)

    fun restartPreview()

    /**
     * 获得摄像头设备.
     * @see CameraDevice
     * @see Camera
     * */
    fun getCameraDevice(): camera?

    interface CameraStateCallback<C> {

        fun onCameraOpening()

        fun onCameraOpenSuccess(camera: C, id: Int)

        fun onCameraOpenFailed(errorCode: Int)

        fun onCameraClosing()

        fun onCameraClosed()

        fun onCameraErrorClose(errorCode: Int)

        fun onCameraPreviewSuccess()

        fun onCameraPreviewStop()
    }

    interface PictureCallback {

        fun onCaptureStart()

        fun onCaptureError(errorCode: Int)

        fun onCaptureResult(buffer: ByteArray)

        fun onCaptureDone()

        companion object {
            const val ERROR_CODE_NOT_SUPPORT_VIDEO_CAPTURE = -101
            const val ERROR_CODE_CAPTURING          = -102
            const val ERROR_CODE_START_STOP_RECORD  = -103
        }

    }

    interface PreviewDataCallback {
        fun onPreviewDataCallback(buffer: ByteArray, previewFormat: Int)
    }

    interface RecordPreviewReady {
        fun onRecordPreviewReady()
    }

    interface AutoFocusCallback {
        fun onAutoFocusCallbackSuccess(success: Boolean)
    }

    companion object {

        fun getPreviewSize(previewWidth: Int, previewHeight: Int, sizeList: Array<Size>, dec: Boolean): Size? {
            val widthArray  = getSameValueIndex(previewWidth, sizeList, true, dec)
            val heightArray = getSameValueIndex(previewHeight, sizeList, false, dec)

            val index: Int
            if (dec) {
                /**
                 * 递减情况下:
                 * 1.  先比较两边最后一个即最小值.
                 * 2.  再将较小值那方取出最大值,然后再取两边的最大值.
                 * */
                val minWidthIndex   = widthArray.last()
                val minHeightIndex  = heightArray.last()
                return when {
                    minWidthIndex == minHeightIndex -> sizeList[minWidthIndex]
                    minWidthIndex > minHeightIndex -> {
                        index = Math.max(minHeightIndex, widthArray.first())
                        sizeList[index]
                    }
                    else -> {
                        index = Math.max(minWidthIndex, heightArray.first())
                        sizeList[index]
                    }
                }
            } else {
                /**
                 * 递增情况下:
                 * 1.  先比较两边第一个即最小值.
                 * 2.  再将较大值那方取出最小值,然后再取两边的最小值.
                 * */
                val minWidthIndex   = widthArray.first()
                val minHeightIndex  = heightArray.first()
                return when {
                    minWidthIndex == minHeightIndex -> sizeList[minWidthIndex]
                    minWidthIndex > minHeightIndex -> {
                        index = Math.min(minWidthIndex, heightArray.last())
                        sizeList[index]
                    }
                    else -> {
                        index = Math.min(minHeightIndex, widthArray.last())
                        sizeList[index]
                    }
                }
            }
        }

        /**
         * 获得队列中跟提供值一样的索引.
         * @param dec 判断何时停止搜索.
         * */
        fun getSameValueIndex(value: Int, list: Array<Size>, width: Boolean, dec: Boolean): ArrayList<Int> {
            Timber.e("getValue.value = [$value]")
            val indexList = ArrayList<Int>()
            var valueIndex  = 0
            var preIndex    = 0
            for (index in 0 until list.size) {
                val size = list[index]
                Timber.e("${size.width}x${size.height}")
                val tmp = if (width) size.width else size.height
                if (tmp == value) {
                    valueIndex++
                    indexList.add(index)
                    Timber.e("getSameValueIndex = ${list[index].width}x${list[index].height}")
                } else if (dec) {
                    if (value < tmp) {
                        preIndex = index
                    } else {
                        preIndex = index
                        break
                    }
                } else {
                    if (value > tmp) {
                        preIndex = index
                    } else {
                        break
                    }
                }
            }
            if (valueIndex == 0) {
                indexList.add(preIndex)
                Timber.e("getSameValueIndex = ${list[preIndex].width}x${list[preIndex].height}")
            }
            return indexList
        }

        const val SENSOR_FRONT_CAMERA = 0
        const val SENSOR_BACK_CAMERA = 0

        val ORIENTATIONS = SparseIntArray()
        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }

        fun getOrientation(rotation: Int): Int {
            return (ORIENTATIONS.get(rotation))
        }

        /*/**
         * 获得与设置的预览分辨率最接近最小的支持的分辨率.
         * 1.  将宽跟高分别放入到两个队列中.
         * 2.  在各自的队列中分别找到跟设置的值相同的或者略低于该值的索引位置.
         * 3.  找到两个索引值相同的或者取最相近的最小索引.然后该值就是最接近的分辨率.
         *
         * @param previewWidth
         * @param previewHeight
         * @param sizeList 顺序排列的分辨率队列.
         * @param dec [sizeList]是否递减排列.寻找队列中最接近的值会按照从上往下搜索,而[dec]就用来判断何时停止.
         * */
        fun getPreviewSize(previewWidth: Int, previewHeight: Int, sizeList: Array<Size>, dec: Boolean, wrong: Boolean): Size? {
            val mPreviewIndex: Int

            //获得跟指定值相同或者最近的索引值.
            val widthArray  = getSameValueIndex(previewWidth, sizeList, true, dec)
            val heightArray = getSameValueIndex(previewHeight, sizeList, false, dec)

            var preWidthIndex   = 0
            var preHeightIndex  = 0
            for (widthIndex in 0 until widthArray.size) {
                val width = widthArray[widthIndex]
                Timber.e("width = $width")
                if (width < 0) {
                    break
                }
                for (heightIndex in 0 until heightArray.size) {
                    val height = heightArray[heightIndex]
                    Timber.e("height = $height")
                    if (height < 0) {
                        break
                    }
                    //若两者相等则取该值.
                    if (width == height) {
                        return sizeList[width]
                    }
                    *//**
         * 某一边的索引值低于另一边的,会将低的那一边不断往上递加,直到相等或者超过.若超过则取上一个未超过的值.
         * 所以某一边低于另一边的时候要判断另一边是否是递加上来的,如果是则取上一个值.如果不是则表明高的那边已经不用在递加,递加另一边的即可.
         * 如果另一边也没有则表明已找到最接近的值,两者取最小即可.
         * *//*
                    if (width < height) {
                        Timber.e("preHeightIndex = $preHeightIndex")
                        *//**
         * 高的索引高,因此要判断高的索引是否有递加操作,即height > preHeightIndex,而前提还得判断preHeightIndex是否有递加即 > 0.
         * 如果都满足,则表示已是高的递加操作超过了宽.因此直接设置值然后返回.
         * 反之则表示 height 原先就大于 width,需要 width 递加,直到 width > height 为止.
         * *//*
                        if (preHeightIndex != 0 && height > preHeightIndex) {
                            Timber.e("width = $width, preHeightIndex = $preHeightIndex")
                            mPreviewIndex = Math.min(width, preHeightIndex)
                            return sizeList[mPreviewIndex]
                        }
                        preHeightIndex  = height
                        preWidthIndex   = width
                        break
                    } else {
                        *//**
         * {width}的索引高,因此要判断{width}的索引是否有递加操作,width > preWidthIndex,而前提还得判断preWidthIndex是否有递加 即 > 0.
         * 如果都满足,则表示已是{width}的递加操作超过了{height}.因此直接设置值然后返回.
         * 反之则表示 width 原先就大于 height,需要 height 递加,直到 height > width为止.
         * *//*
                        Timber.e("preWidthIndex = $preWidthIndex")
                        if (preWidthIndex != 0 && width > preWidthIndex) {
                            Timber.e("preWidthIndex = $preWidthIndex, height = $height")
                            mPreviewIndex = Math.min(preWidthIndex, height)
                            return sizeList[mPreviewIndex]
                        }
                        preWidthIndex   = width
                        preHeightIndex = height
                        break
                    }

                }
            }
            Timber.e("preWidthIndex = $preWidthIndex, preHeightIndex = $preHeightIndex")
            return null
        }*/

        const val CAMERA_OPEN_ERROR_OPEN_FAILED     = -11
        const val CAMERA_OPEN_ERROR_NO_CAMERA       = -12
        const val CAMERA_OPEN_ERROR_NOT_RELEASE     = -13
        const val CAMERA_OPEN_ERROR_GET_INFO_FAILED = -14
        const val CAMERA_OPEN_ERROR_PREVIEW_FAILED  = -15

        const val FOCUS_MODE_MANUAL = "manual"
    }

}