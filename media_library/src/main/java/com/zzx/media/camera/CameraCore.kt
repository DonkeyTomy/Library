package com.zzx.media.camera

import android.hardware.Camera
import timber.log.Timber

/**@author Tomy
 * Created by Tomy on 2019/11/26.
 * 保存Camera的状态参数及对象.
 */
class CameraCore<camera> {

    private var mCamera: camera? = null

    private var mStatus = Status.RELEASE

    private var mLoopStatus = 0

    private var mRecordStatus = 0

    private var mParameter: Camera.Parameters? = null

    private var mCameraID = 0

    /** Preview **/
    private var mPreviewSupportList: List<Camera.Size>? = null
    private var mPreviewSize: Camera.Size? = null
    private var mPreviewFormat: Int = 0

    /** Photo **/
    private var mPhotoSupportSize: List<Camera.Size>? = null
    private var mPhotoSize: Camera.Size? = null
    private var mPhotoFormat: Int = 0

    /** Video **/
    private var mVideoSupportSize: List<Camera.Size>? = null
    private var mVideoSize: Camera.Size? = null
    private var mVideoFormat: Int = 0

    /** Focus **/
    private var mFocusSupportModeList: List<String>? = null
    private var mFocusMode: String? = null

    fun getCameraID() = mCameraID
    fun setCameraID(cameraID: Int) {
        mCameraID = cameraID
    }

    fun setParameters(parameters: Camera.Parameters?) {
        mParameter = parameters
    }

    fun setVideoSize(videoSize: Camera.Size) {
        mVideoSize = videoSize
    }
    fun getVideoSize() = mVideoSize


    fun setVideoFormat(videoFormat: Int) {
        mVideoFormat = videoFormat
    }
    fun getVideoFormat() = mVideoFormat


    fun setPhotoSize(photoSize: Camera.Size) {
        mPhotoSize = photoSize
    }
    fun getPhotoSize() = mPhotoSize


    fun setPhotoFormat(photoFormat: Int) {
        mPhotoFormat = photoFormat
    }
    fun getPhotoFormat() = mPhotoFormat


    fun setFocusMode(focusMode: String) {
        mFocusMode = focusMode
    }
    fun getFocusMode() = mFocusMode


    fun setCamera(camera: camera?) {
        mCamera = camera
    }
    fun getCamera(): camera? {
        return mCamera
    }

    fun setStatus(status: Status) {
        mStatus = status
    }

    fun getStatus(): Status {
        return mStatus
    }

    fun isPreview(): Boolean {
        return mStatus == Status.PREVIEW || isRecording()
    }

    fun canPreview(): Boolean {
        return mStatus == Status.OPENED
    }

    fun isOpened(): Boolean {
        return mStatus == Status.OPENED
    }

    fun isOpening(): Boolean {
        return mStatus == Status.OPENING
    }

    fun isClosing(): Boolean {
        return mStatus == Status.CLOSING
    }

    fun canOpen(): Boolean {
        return mStatus == Status.RELEASE
    }

    fun canClose(): Boolean {
        return !isLoopRecord() && (mStatus == Status.PREVIEW || mStatus == Status.OPENED || mStatus == Status.RELEASE)
    }

    fun isCapturing(): Boolean {
        return mStatus == Status.CAPTURING || mStatus == Status.RECORDING_CAPTURING
    }

    fun canCapture(): Boolean {
        return mStatus == Status.PREVIEW || mStatus == Status.RECORDING
    }

    fun isIDLE(): Boolean {
        return isLoopRecord() && (mStatus != Status.RECORDING && mStatus != Status.RECORDING_CAPTURING && mStatus != Status.CAPTURING)
    }

    fun isBusy(): Boolean {
        return mStatus == Status.RECORDING || mStatus == Status.RECORDING_CAPTURING || mStatus == Status.CAPTURING || mStatus == Status.CLOSING
    }

    fun isUserBusy(): Boolean {
        return isNormalLoop() || mStatus == Status.RECORDING_CAPTURING || mStatus == Status.CAPTURING || mStatus == Status.CLOSING
    }

    fun isRecording(): Boolean {
        return mStatus == Status.RECORDING || mStatus == Status.RECORDING_CAPTURING
    }

    fun setLoopRecord(start: Boolean, autoDelete: Boolean = false) {
        Timber.d("setLoopRecord  start[$start]; autoDelete[$autoDelete]")
        mLoopStatus = if (start) {
            if (autoDelete) {
                LOOP_RECORD.or(LOOP_AUTO_DEL)
            } else {
                LOOP_RECORD
            }
        } else {
            0
        }
    }

    fun setRecordImp(imp: Boolean) {
        mRecordStatus = if (imp) {
            mRecordStatus.or(RECORD_IMP)
        } else {
            mRecordStatus.and(RECORD_IMP.inv())
        }
    }

    fun getRecordStatue() = mRecordStatus

    fun getRecordImp() = mRecordStatus.and(RECORD_IMP)

    fun isRecordImp() = getRecordImp() == RECORD_IMP

    fun getLoopStatus(): Int  {
        Timber.d("mLoopStatus = $mLoopStatus")
        return mLoopStatus
    }

    /**
     * @return 是否正常循环录像
     */
    fun isNormalLoop(): Boolean {
        return getLoopStatus() == LOOP_RECORD
    }

    /**
     * @return 是否处于预录自动删除模式
     */
    fun isAutoDelLoop(): Boolean {
        return getLoopStatus() == LOOP_RECORD.or(LOOP_AUTO_DEL)
    }

    /**
     * @return 是否处于循环录像模式.包括预录与正常循环
     */
    fun isLoopRecord(): Boolean {
        return getLoopStatus().and(LOOP_RECORD) == LOOP_RECORD
    }

    fun isRecordingCapturing(): Boolean {
        return mStatus == Status.RECORDING_CAPTURING
    }

    fun canRecord(): Boolean {
        return mStatus == Status.PREVIEW
    }


    enum class Status {
        RELEASE,
        OPENING,
        OPENED,
        PREVIEW,
        CAPTURING,
        RECORDING,
        RECORDING_CAPTURING,
        CLOSING,
        CAPTURE_RESULT,
        CAPTURE_FINISH,
        ERROR
    }

    class CameraState {
        @Volatile
        private var mNewState = CAMERA_RELEASED

        private fun isBitSet(bit: Int): Boolean {
            return mNewState.and(bit) == bit
        }

        private fun isBitUnset(bit: Int): Boolean {
            return mNewState.and(bit) == 0
        }

        private fun setBit(bit: Int) {
            mNewState = mNewState.or(bit)
        }

        private fun setState(state: Int) {
            mNewState = state
        }

        private fun unsetBit(bit: Int) {
            mNewState = mNewState.and(bit.inv())
        }

        fun getStatus() = mNewState

        fun isClosing()
                = isBitSet(CAMERA_CLOSING)

        fun startClose() {
            setState(CAMERA_CLOSING)
        }

        fun canClose()
                = isBitUnset(CAMERA_CAPTURE.or(CAMERA_RECORD))

        fun closeCamera() {
            mNewState = CAMERA_RELEASED
        }

        fun startOpen() {
            setState(CAMERA_OPENING)
        }

        fun isOpening()
                = isBitSet(CAMERA_OPENING)

        fun openCamera() {
            setBit(CAMERA_OPENED)
        }

        fun isCameraOpened(): Boolean {
            return isBitSet(CAMERA_OPENED)
        }

        fun startPreview() {
            setBit(CAMERA_PREVIEWED)
        }

        fun stopPreview() {
            unsetBit(CAMERA_PREVIEWED)
        }

        fun isPreview(): Boolean {
            return isCameraOpened() && isBitSet(CAMERA_PREVIEWED)
        }

        fun startRecord() {
            setBit(CAMERA_RECORD)
        }

        fun stopRecord() {
            unsetBit(CAMERA_RECORD)
        }

        fun isRecording(): Boolean {
            return isCameraOpened() && isBitSet(CAMERA_RECORD)
        }

        fun startCapture() {
            setBit(CAMERA_CAPTURE)
        }

        fun stopCapture() {
            unsetBit(CAMERA_CAPTURE)
        }

        fun isCapturing() = isCameraOpened() && isBitSet(CAMERA_CAPTURE)

        fun canRecord(): Boolean {
            return isPreview() && !isRecording()
        }


        fun canCapture()
                = isCameraOpened() && mNewState.and(CAMERA_CAPTURE) == 0

        fun canOpen()
                = !isCameraOpened()

        fun canStartPreview()
                = isCameraOpened() && mNewState.and(CAMERA_PREVIEWED) == 0

        fun canStopPreview()
                = isPreview() && isBitUnset(CAMERA_CAPTURE.or(CAMERA_RECORD))

        fun isBusy()
                = isCameraOpened() && !isBitUnset(CAMERA_CAPTURE.or(CAMERA_RECORD))

    }

    companion object {
        const val ERROR_EXTRA_CODE_NOT_MOUNT    = -100
        const val ERROR_EXTRA_CODE_NOT_ENOUGH   = -101
        const val STATUS_CLOSING    = 0x4
        const val STATUS_CAPTURING  = 0x8

        const val LOOP_RECORD   = 0x10
        const val LOOP_AUTO_DEL = 0x20
        const val RECORD_IMP    = 0x40

        const val CAMERA_RELEASED   = 0
        const val CAMERA_OPENED     = 0x01
        const val CAMERA_OPENING    = CAMERA_OPENED shl 1
        const val CAMERA_CLOSING    = CAMERA_OPENED shl 2
        const val CAMERA_PREVIEWED  = CAMERA_OPENED shl 3
        const val CAMERA_CAPTURE    = CAMERA_OPENED shl 4
        const val CAMERA_RECORD     = CAMERA_OPENED shl 5
    }
}