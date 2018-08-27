package com.zzx.media.codec

import android.media.MediaCodec
import android.media.MediaFormat
import timber.log.Timber
import java.nio.ByteBuffer

/**@author Tomy
 * Created by Tomy on 2017/11/6.
 */
class AACCodec: ICodec {

    private var mCodec: MediaCodec? = null

    private lateinit var mInputBuffer: ByteBuffer
    private lateinit var mOutBuffer: ByteBuffer

    private var mCallback: ICodec.OutCallback? = null

    private val mBufferInfo = MediaCodec.BufferInfo()

    fun W(msg: String) {
        Timber.w("========= $msg ==========")
    }

    fun E(msg: String) {
        Timber.e("========= $msg ==========")
    }

    override fun initCodec(codecName: String, encoder: Boolean, sampleRate: Int, channelCount: Int, bytePerBit: Int, bitRate: Int): Boolean {
        mCodec = if (encoder) {
            MediaCodec.createEncoderByType(codecName)
        } else {
            MediaCodec.createDecoderByType(codecName)
        }
        val format = MediaFormat.createAudioFormat(codecName, sampleRate, channelCount)
        format.setInteger(MediaFormat.KEY_BIT_RATE, sampleRate * channelCount * 8)
//        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectHE)
        mCodec?.configure(format, null, null, if (encoder) MediaCodec.CONFIGURE_FLAG_ENCODE else 0)
        mCodec?.start()
        return false
    }

    fun setCallback(callback: ICodec.OutCallback) {
        mCallback = callback
    }

    override fun releaseCodec() {
        mCodec?.release()
    }

    override fun encodeData(inputData: ByteArray) {
        codec(inputData)
    }

    override fun decodeData(inputData: ByteArray) {
        codec(inputData)
    }

    private fun  codec(inputData: ByteArray) {
        var index = 0
        while (index < inputData.size) {
            val bufferIndex = mCodec!!.dequeueInputBuffer(0)
            if (bufferIndex < 0) {
                E("dequeueInputBuffer failed")
                return
            }

            mInputBuffer = mCodec!!.getInputBuffer(bufferIndex)
            mInputBuffer.clear()

            val tmpSize = Math.min(mInputBuffer.remaining(), inputData.size)
            W("tmpSize = $tmpSize, index = $index")
            mInputBuffer.put(inputData, index, tmpSize)
            index += tmpSize

            mCodec!!.queueInputBuffer(bufferIndex, 0, tmpSize, 0, 0)

            var outIndex = mCodec!!.dequeueOutputBuffer(mBufferInfo, 50)
            if (outIndex < 0) {
                E("outIndex = $outIndex")
            }
            while (outIndex >= 0) {
                W("outIndex = $outIndex")
                mOutBuffer = mCodec!!.getOutputBuffer(outIndex)
                mOutBuffer.position(mBufferInfo.offset)
                mOutBuffer.limit(mBufferInfo.offset + mBufferInfo.size)
                val outData = ByteArray(mBufferInfo.size)
                mOutBuffer.get(outData)
                mCallback?.onOut(outData)
                mCodec!!.releaseOutputBuffer(outIndex, false)
                outIndex = mCodec!!.dequeueOutputBuffer(mBufferInfo, 50)
            }
        }
    }

}