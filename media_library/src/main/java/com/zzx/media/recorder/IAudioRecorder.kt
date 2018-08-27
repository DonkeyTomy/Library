package com.zzx.media.recorder

/**@author Tomy
 * Created by Tomy on 2018/1/15.
 */
interface IAudioRecorder {

    fun init()

    fun setReadCallback(callback: AudioReadCallback)

    fun startRecord()

    interface AudioReadCallback {
        fun onAudioRead(data: ByteArray)
    }

}