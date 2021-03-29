package com.zzx.media.recorder

import android.hardware.Camera
import android.view.Surface
import androidx.annotation.IntDef
import com.zzx.media.parameters.AudioProperty
import com.zzx.media.parameters.VideoProperty
import java.io.File

/**@author Tomy
 * Created by Tomy on 2018/6/11.
 */
interface IRecorder {

    /**
     * 初始化至[State.IDLE]
     * */
    fun init()

    /**
     * 设置参数至[State.PREPARED]状态
     * */
    fun prepare()

    /**
     * Camera2录制视频使用
     * */
    fun getSurface(): Surface?

    /**
     * Camera1时录制视频使用
     * */
    fun setCamera(camera: Camera?)

    /**
     * @see setOutputFile[File]
     * @param fullPath 完整的文件路径.
     * */
    fun setOutputFilePath(fullPath: String)

    /**
     *
     * @param dirPath 目录完整路径.
     * @param fileName 文件名.
     * */
    fun setOutputFilePath(dirPath: String, fileName: String)

    /**
     * @param fullFile 完整的文件.
     * */
    fun setOutputFile(fullFile: File)

    /**
     * @param dir   目录文件.
     * @param file  文件.
     * */
    fun setOutputFile(dir: File, file: File)

    fun getOutputFile(): String

    /**
     * 设置视频参数.
     * @see VideoProperty
     * */
    fun setVideoProperty(videoProperty: VideoProperty)

    /**
     * 设置音频参数.
     * @see AudioProperty
     * */
    fun setAudioProperty(audioProperty: AudioProperty)

    fun setRecordCallback(callback: IRecordCallback?)

    /**
     * @param degrees Int 视频录像的旋转角度
     */
    fun setSensorRotationHint(degrees: Int)

    fun setProperty(quality: Int, highQuality: Boolean = true)

    /**
     * 开始录像.
     * */
    fun startRecord()

    /**
     * 停止录像.
     * */
    fun stopRecord()

//    fun isRecordStartingOrStopping(): Boolean

    fun pauseRecord()

    fun resumeRecord()

    /**
     * 重置[IRecorder]到[State.IDLE]状态,从而重新设置参数.
     * 若在录像会先停止录像.
     * */
    fun reset()

    /**
     * 释放[IRecorder]到[State.RELEASE]状态.必须重新[init].
     * */
    fun release()

    /**
     * 返回当前状态.
     * @see State
     * */
    fun getState(): State

    /**
     * 设置当前状态.
     * @see State
     * */
    fun setState(state: State)

    enum class State {
        RELEASE, IDLE, PREPARED, RECORDING, ERROR, PAUSE
    }

    companion object {
        const val VIDEO = 0x01
        const val AUDIO = 0x02
        const val VIDEO_MUTE    = VIDEO.or(AUDIO)

        const val RECORD_STATE = "RecordState"
    }

    @IntDef(VIDEO, AUDIO, VIDEO_MUTE)
    @Retention(AnnotationRetention.SOURCE)
    annotation class FLAG

    fun setFlag(@FLAG flag: Int)

    interface IRecordCallback {

        fun onRecorderPrepared()

        fun onRecordStarting()

        fun onRecordStart()

        fun onRecordStopping()

        fun onRecordStop(stopCode: Int = -1)

//        fun onRecorderConfigureFailed()

        fun onRecordError(errorCode: Int, errorType: Int = -1)

        fun onRecorderFinished(file: File?)

        fun onRecordPause()

        fun onRecordResume()

        companion object {
            const val RECORD_STOP_EXTERNAL_STORAGE_NOT_MOUNTED  = -101
            const val RECORD_STOP_EXTERNAL_STORAGE_NOT_ENOUGH   = -102
            /**
             * 调用停止录像但是未执行录像
             */
            const val RECORD_STOP_NOT_RECORDING                 = -103
            /**
             * 录像停止时发生异常错误
             */
            const val RECORD_STOP_ERROR            = -104


            const val RECORD_ERROR_TOO_SHORT = -1007
            const val RECORD_ERROR_CONFIGURE_FAILED = -105
            const val CAMERA_RELEASED = -106
            const val CAMERA_IS_NULL = -107
            const val RECORDER_NOT_IDLE = -108

            /**
             * 无法写入文件,可能是TF卡损坏或者路径错误
             */
            const val ERROR_CODE_FILE_WRITE_DENIED  = 0x1111

            /**
             * 配置Camera出错,可能是相机已释放或者无法锁定
             */
            const val ERROR_CODE_CAMERA_SET_FAILED  = 0x1112
        }

    }

}