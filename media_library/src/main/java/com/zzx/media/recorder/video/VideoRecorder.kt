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
import java.util.concurrent.atomic.AtomicBoolean

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

    private var mDegrees    = 90

    private var mCamera: Camera? = null

    private val mRecordStarting = AtomicBoolean(false)

    private val mRecordStopping = AtomicBoolean(false)


    override fun setFlag(@IRecorder.FLAG flag: Int) {
        mFlag = flag
    }

    override fun setCamera(camera: Camera) {
        mCamera = camera
    }

    override fun setSensorRotationHint(degrees: Int) {
        mDegrees = degrees
    }

    init {
        init()
    }

    /**
     * 初始化至[State.IDLE]
     * @see prepare
     * */
    override fun init() {
        mMediaRecorder = MediaRecorder().apply {
            reset()
            setState(State.IDLE)
        }
        mMediaRecorder.setOnErrorListener { _, what, extra ->
            Timber.e("$TAG_RECORDER onRecordError.what [$what] extraCode[$extra]")
            FlowableUtil.setBackgroundThread(Consumer {
                mFile?.delete()
                mRecorderCallback?.onRecordError(extra)
                reset()
//                init()
            })
        }
        mMediaRecorder.setOnInfoListener { _, what, _ ->
            when (what) {
                MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED, MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED -> {
                    stopRecord()
                }
            }
        }
    }

    override fun setRecordCallback(callback: IRecorder.IRecordCallback?) {
        mRecorderCallback = callback
    }

    override fun setProperty(quality: Int, highQuality: Boolean) {
        mRecordStarting.set(true)
        val profile = CamcorderProfile.get(quality)
        val min = if (highQuality) 1 else 2
        mAudioProperty = AudioProperty(profile.audioSampleRate,
                profile.audioChannels,
                8,
                profile.audioBitRate)
        mVideoProperty = VideoProperty(profile.videoFrameWidth,
                profile.videoFrameHeight,
                profile.videoFrameRate,
                profile.videoBitRate / min, null).apply {

            audioProperty = mAudioProperty
        }
        if (quality == CamcorderProfile.QUALITY_480P) {
            mVideoProperty.width = 864
        }
        Timber.tag(TAG_RECORDER).e("$mVideoProperty")
        prepare()
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

    private fun setProfile(profile: CamcorderProfile) {
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
        try {
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
                        setOrientation()
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
            Timber.e("mFlag = $mFlag [1: Video. 2: Audio. 3:MuteVideo]")
            Timber.e("mFile = ${mFile!!.absolutePath}.")
            mMediaRecorder.prepare()
            setState(State.PREPARED)
            mRecorderCallback?.onRecorderPrepared()
            Timber.e("$TAG_RECORDER onRecorderPrepared")
        } catch (e: Exception) {
            e.printStackTrace()
//            unlockCamera()
            setState(State.ERROR)
            mRecorderCallback?.onRecorderConfigureFailed()
            reset()
            Timber.e("$TAG_RECORDER onRecorderConfigureFailed")
        }
    }

    private fun unlockCamera() {
        if (!isUseCamera2)
            mCamera?.unlock()
    }

    private fun setOrientation() {
        mMediaRecorder.setOrientationHint(mDegrees)
        /*when(mDegrees) {
            SENSOR_FRONT_CAMERA  -> mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(mRotation))
            SENSOR_BACK_CAMERA  -> mMediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(mRotation))
        }*/
    }

    /**
     * 开始录像.
     * */
    override fun startRecord() {
        Timber.e("$TAG_RECORDER startRecord. mState = [$mState]")
        if (getState() == State.PREPARED) {
            mMediaRecorder.start()
            setState(State.RECORDING)
            mRecorderCallback?.onRecordStart()
        } else {
            throw object : IllegalStateException("Current state is {$mState}.Not Prepared!") {}
        }
        mRecordStarting.set(false)
    }

    /**
     * 停止录像.
     * */
    override fun stopRecord() {
        Timber.e("$TAG_RECORDER stopRecord. mState = [$mState]")
        mRecordStopping.set(true)
        val state = getState()
        if (state == State.RECORDING || state == State.PAUSE) {
            setState(State.IDLE)
            try {
                mMediaRecorder.resume()
                mMediaRecorder.stop()
                mRecorderCallback?.onRecorderFinished(mFile)
            } catch (e: Exception) {
                e.printStackTrace()
                mRecorderCallback?.onRecorderFinished(null)
            }
        }
        mRecordStopping.set(false)
    }

    override fun isRecordStartingOrStopping(): Boolean {
        return mRecordStarting.get() || mRecordStopping.get()
    }

    override fun pauseRecord() {
        Timber.e("$TAG_RECORDER pauseRecord. mState = [$mState]")
        if (getState() == State.RECORDING) {
            mMediaRecorder.pause()
            setState(State.PAUSE)

        }
    }

    override fun resumeRecord() {
        Timber.e("$TAG_RECORDER resumeRecord. mState = [$mState]")
        if (getState() == State.PAUSE) {
            mMediaRecorder.resume()
            setState(State.RECORDING)

        }
    }

    /**
     * 重置[IRecorder]到[State.IDLE]状态,从而重新设置参数.
     * 若在录像会先停止录像.
     * */
    override fun reset() {
        Timber.e("$TAG_RECORDER reset. mState = [$mState]")
        if (getState() != State.IDLE && getState() != State.RELEASE) {
            stopRecord()
            mMediaRecorder.reset()
            setState(State.IDLE)
        }
        mRecordStarting.set(false)
        mRecordStopping.set(false)
    }

    /**
     * 释放[IRecorder]到[State.RELEASE]状态.必须重新[init].
     * */
    override fun release() {
        Timber.e("$TAG_RECORDER release. mState = [$mState]")
        if (getState() != State.RELEASE) {
            stopRecord()
            mMediaRecorder.release()
            setState(State.RELEASE)
        }
        mRecordStarting.set(false)
        mRecordStopping.set(false)
    }

    /**
     * 返回当前状态.
     * @see State
     * */
    override fun getState(): IRecorder.State {
        Timber.e("$TAG_RECORDER getState() = $mState")
        return mState
    }

    /**
     * 设置当前状态.
     * @see State
     * */
    override fun setState(state: IRecorder.State) {
        mState = state
        Timber.e("$TAG_RECORDER setState. mState = [$mState]")
    }

    override fun getSurface(): Surface {
        return mMediaRecorder.surface
    }


    companion object {
        private const val TAG_RECORDER = "[VideoRecorder] "

        const val SENSOR_FRONT_CAMERA = 270
        const val SENSOR_BACK_CAMERA = 90
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