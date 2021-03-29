package com.zzx.media.codec

/**@author Tomy
 * Created by Tomy on 2017/11/6.
 */
interface ICodec {

    fun initCodec(codecName: String, encoder: Boolean, sampleRate: Int, channelCount: Int, bitPerByte: Int): Boolean

    fun startCodec()

    fun stopCodec()

    fun encodeData(inputData: ByteArray)

    fun decodeData(inputData: ByteArray)

    fun releaseCodec()

    companion object {
        const val AAC   = "audio/mp4a-latm"
//        const val THREE_GPP  = "audio/3gpp"
//        const val AMR_WB  = "audio/amr-wb"
        const val H264  = "video/avc"
    }

    interface OutCallback {
        fun onOut(data: ByteArray)
    }

}