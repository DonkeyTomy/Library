package com.zzx.media.recorder.video

import android.hardware.Camera
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.os.Build
import android.util.SparseIntArray
import android.view.Surface
import com.zzx.media.parameters.AudioProperty
import com.zzx.media.parameters.VideoProperty
import com.zzx.media.recorder.IRecorder
import com.zzx.media.recorder.IRecorder.State
import com.zzx.utils.file.FileUtil
import com.zzx.utils.rxjava.FlowableUtil
import io.reactivex.functions.Consumer
import timber.log.Timber
import java.io.File

/**@author Tomy
 * Created by Tomy on 2018/6/8.
 */
class VideoRecorder(var isUseCamera2: Boolean = true): IRecorder {

    private lateinit var mMediaRecorder: MediaRecorder

    private var mState: State = State.RELEASE

    private var mRecorderCallback: IRecorder.IRecordCallback? = null

    @IRecorder.FLAG
    private var mFlag: Int = 0

    private lateinit var mVideoProperty: VideoProperty

    private lateinit var mAudioProperty: AudioProperty

    private var mFile: File? = null

    private var mDegrees    = 0

    private var mRotation   = 0

    private var mCamera: Camera? = null


    override fun setFlag(@IRecorder.FLAG flag: Int) {
        mFlag = flag
    }

    override fun setCamera(camera: Camera) {
        mCamera = camera
    }

    fun setSensorOrientationHint(degrees: Int) {
        mDegrees = degrees
    }

    override fun setRotation(rotation: Int) {
        mRotation = rotation
    }

    /**
     * 初始化至[State.IDLE]
     * @see prepare
     * */
    override fun init() {
        mMediaRecorder = MediaRecorder()
        mMediaRecorder.setOnErrorListener { _, _, _ ->
            Timber.e("$TAG_RECORDER onRecordError")
            FlowableUtil.setBackgroundThread(Consumer {
                mRecorderCallback?.onRecordError()
                release()
                init()
            })
        }
    }

    override fun setRecordCallback(callback: IRecorder.IRecordCallback?) {
        mRecorderCallback = callback
    }

    override fun setProperty(quality: Int) {
        val profile = CamcorderProfile.get(quality)
        mAudioProperty = AudioProperty(profile.audioSampleRate,
                profile.audioChannels,
                8,
                profile.audioBitRate)
        mVideoProperty = VideoProperty(profile.videoFrameWidth,
                profile.videoFrameHeight,
                profile.videoFrameRate,
                profile.videoBitRate / 2, null).apply {

            audioProperty = mAudioProperty
        }
        prepare()
        Timber.e("$TAG_RECORDER $mVideoProperty")
    }

    /**
     * 设置视频参数.
     * @see VideoProperty
     * */
    override fun setVideoProperty(videoProperty: VideoProperty) {
        mVideoProperty = videoProperty
    }

    /**
     * 设置音频参数.
     * @see AudioProperty
     * */
    override fun setAudioProperty(audioProperty: AudioProperty) {
        mAudioProperty = audioProperty
    }

    fun setProfile(profile: CamcorderProfile) {
        mMediaRecorder.setProfile(profile)
    }

    /**
     * @see setOutputFile[File]
     * @param fullPath 完整的文件路径.
     * */
    override fun setOutputFilePath(fullPath: String) {
        mFile = File(fullPath)
    }

    /**
     *
     * @param dirPath 目录完整路径.
     * @param fileName 文件名.
     * */
    override fun setOutputFilePath(dirPath: String, fileName: String) {
        setOutputFile(File(dirPath, fileName))
    }

    /**
     * @param fullFile 完整的文件.
     * */
    override fun setOutputFile(fullFile: File) {
        mFile = fullFile
        Timber.e("$TAG_RECORDER mkdirs ${mFile!!.parent} success ? ${FileUtil.checkDirExist(mFile!!.parentFile, true)}")
    }

    /**
     * @param dir   目录文件.
     * @param file  文件.
     * */
    override fun setOutputFile(dir: File, file: File) {
        setOutputFile(File(file, file.name))
    }

    override fun getOutputFile(): String {
        return mFile?.absolutePath ?: ""
    }

    private fun getVideoSource(): Int {

        return if (isUseCamera2) {
            MediaRecorder.VideoSource.SURFACE
        } else {
            mCamera!!.unlock()
            mMediaRecorder.setCamera(mCamera)
            MediaRecorder.VideoSource.CAMERA
        }
    }

    /**
     * 设置参数至[State.PREPARED]状态
     * @see init
     * */
    override fun prepare() {
        Timber.e("$TAG_RECORDER prepare. mState = [$mState]")
        when(mFlag) {
            IRecorder.AUDIO ->
                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            IRecorder.VIDEO_MUTE ->
                mMediaRecorder.setVideoSource(getVideoSource())
            IRecorder.VIDEO -> {
                mMediaRecorder.setVideoSource(getVideoSource())
                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            }
        }

        when(mFlag) {
            IRecorder.AUDIO ->
                mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            IRecorder.VIDEO_MUTE,
            IRecorder.VIDEO ->
                mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)

        }

        when(mFlag) {
            IRecorder.VIDEO,
            IRecorder.VIDEO_MUTE -> {
                mMediaRecorder.apply {
                    setVideoFrameRate(mVideoProperty.frameRate)
                    setVideoSize(mVideoProperty.width, mVideoProperty.height)
                    setVideoEncodingBitRate(mVideoProperty.bitRate)
                    setVideoEncoder(mVideoProperty.encoder)
                }
            }
        }

        when(mFlag) {
            IRecorder.AUDIO,
            IRecorder.VIDEO -> {
                mMediaRecorder.apply {
                    setAudioEncodingBitRate(mAudioProperty.bitRate)
                    setAudioChannels(mAudioProperty.channels)
                    setAudioSamplingRate(mAudioProperty.sampleRate)
                    setAudioEncoder(mAudioProperty.encoder)
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mMediaRecorder.setOutputFile(mFile)
        }
        setOrientation()
        Timber.e("mFlag = $mFlag[1: Video. 2: Audio. 3:MuteVideo]")
        Timber.e("mFile = ${mFile!!.absolutePath}.")
        try {
            mMediaRecorder.prepare()
            setState(State.PREPARED)
            mRecorderCallback?.onRecorderPrepared()
            Timber.e("$TAG_RECORDER onRecorderPrepared")
        } catch (e: Exception) {
            e.printStackTrace()
            unlockCamera()
            setState(State.ERROR)
            mRecorderCallback?.onRecorderConfigureFailed()
            Timber.e("$TAG_RECORDER onRecorderConfigureFailed")
            /*FlowableUtil.setMainThread(Consumer {
                TTSToast.showToast(R.string.not_support_format)
            })*/
        }
    }

    private fun unlockCamera() {
        if (!isUseCamera2)
            mCamera?.unlock()
    }

    private fun setOrientation() {
        when(mDegrees) {
            SENSOR_ORIENTATION_DEFAULT_DEGREES  -> mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(mRotation))
            SENSOR_ORIENTATION_INVERSE_DEGREES  -> mMediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(mRotation))
        }
    }

    /**
     * 开始录像.
     * */
    override fun startRecord() {
        Timber.e("$TAG_RECORDER startRecord. mState = [$mState]")
        if (checkState() == State.PREPARED) {
            mMediaRecorder.start()
            setState(State.RECORDING)
        } else {
            throw object : IllegalStateException("Current state is {$mState}.Not Prepared!") {}
        }
    }

    /**
     * 停止录像.
     * */
    override fun stopRecord() {
        Timber.e("$TAG_RECORDER stopRecord. mState = [$mState]")
        if (checkState() == State.RECORDING) {
            mMediaRecorder.stop()
            setState(State.IDLE)
            mRecorderCallback?.onRecorderFinished(mFile)
        }
    }

    /**
     * 重置[IRecorder]到[State.IDLE]状态,从而重新设置参数.
     * 若在录像会先停止录像.
     * */
    override fun reset() {
        Timber.e("$TAG_RECORDER reset. mState = [$mState]")
        if (checkState() != State.IDLE && checkState() != State.RELEASE) {
            stopRecord()
            mMediaRecorder.reset()
            setState(State.IDLE)
        }
    }

    /**
     * 释放[IRecorder]到[State.RELEASE]状态.必须重新[init].
     * */
    override fun release() {
        Timber.e("$TAG_RECORDER release. mState = [$mState]")
        if (checkState() != State.RELEASE) {
            stopRecord()
            mMediaRecorder.release()
            setState(State.RELEASE)
        }
    }

    /**
     * 返回当前状态.
     * @see State
     * */
    override fun checkState(): IRecorder.State {
        return mState
    }

    /**
     * 设置当前状态.
     * @see State
     * */
    override fun setState(state: IRecorder.State) {
        mState = state
    }

    override fun getSurface(): Surface {
        return mMediaRecorder.surface
    }


    companion object {
        private const val TAG_RECORDER = "[VideoRecorder] "

        private const val SENSOR_ORIENTATION_DEFAULT_DEGREES = 90
        private const val SENSOR_ORIENTATION_INVERSE_DEGREES = 270
        val DEFAULT_ORIENTATIONS  = SparseIntArray().apply {
            append(Surface.ROTATION_0, 90)
            append(Surface.ROTATION_90, 0)
            append(Surface.ROTATION_180, 270)
            append(Surface.ROTATION_270, 180)
        }

        val INVERSE_ORIENTATIONS = SparseIntArray().apply {
            append(Surface.ROTATION_0, 270)
            append(Surface.ROTATION_90, 180)
            append(Surface.ROTATION_180, 90)
            append(Surface.ROTATION_270, 0)
        }
    }

}