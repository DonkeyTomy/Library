package com.zzx.media.recorder

import android.hardware.Camera
import android.support.annotation.IntDef
import android.view.Surface
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
    fun setCamera(camera: Camera)

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

    fun setProperty(quality: Int)

    /**
     * 开始录像.
     * */
    fun startRecord()

    /**
     * 停止录像.
     * */
    fun stopRecord()

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

        fun onRecordStart()

        fun onRecordStop()

        fun onRecorderConfigureFailed()

        fun onRecordError()

        fun onRecorderFinished(file: File?)

        fun onRecordPause()

        fun onRecordResume()
    }

}