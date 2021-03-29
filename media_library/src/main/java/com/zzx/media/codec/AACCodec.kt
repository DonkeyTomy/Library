package com.zzx.media.codec

import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import timber.log.Timber
import java.nio.ByteBuffer
import kotlin.math.min

/**@author Tomy
 * Created by Tomy on 2017/11/6.
 */
class AACCodec(private var mMuxerWrapper: MuxerWrapper? = null): ICodec {

    private var mCodec: MediaCodec? = null

    private var mInputBuffer: ByteBuffer? = null
    private var mOutBuffer: ByteBuffer? = null

    private var mCallback: ICodec.OutCallback? = null

    private val mBufferInfo = MediaCodec.BufferInfo()

    @Volatile
    private var mKeepAlive = true

    fun setMuxerWrapper(wrapper: MuxerWrapper?) {
        mMuxerWrapper = wrapper
    }

    fun W(msg: String) {
        Timber.w("========= $msg ==========")
    }

    fun E(msg: String) {
        Timber.e("========= $msg ==========")
    }

    fun D(msg: String) {
        Timber.d("========= $msg ==========")
    }

    override fun initCodec(codecName: String, encoder: Boolean, sampleRate: Int, channelCount: Int, bitPerByte: Int): Boolean {
        mCodec = if (encoder) {
            MediaCodec.createEncoderByType(codecName)
        } else {
            MediaCodec.createDecoderByType(codecName)
        }
        val audioFormat = MediaFormat.createAudioFormat(codecName, sampleRate, channelCount)
        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, sampleRate * channelCount * bitPerByte)
        // optional stuff
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_MONO)
        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
//        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectHE)
        try {
            mCodec?.configure(audioFormat, null, null, if (encoder) MediaCodec.CONFIGURE_FLAG_ENCODE else 0)
        } catch (e: Exception) {
            return false
        }
        return true
    }

    override fun startCodec() {
        mCodec?.start()
        mKeepAlive = true
    }

    override fun stopCodec() {
        mCodec?.stop()
        mKeepAlive = false
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
        while (mKeepAlive && index < inputData.size) {
            val bufferIndex = mCodec!!.dequeueInputBuffer(0)
            if (bufferIndex < 0) {
                E("dequeueInputBuffer failed")
                return
            }
            var tmpSize = 0

            mInputBuffer = mCodec!!.getInputBuffer(bufferIndex)
            mInputBuffer?.apply {
                clear()
                tmpSize = min(remaining(), inputData.size)
                W("tmpSize = $tmpSize, index = $index")
                put(inputData, index, tmpSize)
            }

            index += tmpSize

            mCodec!!.queueInputBuffer(bufferIndex, 0, tmpSize, 0, 0)

            var outIndex = mCodec!!.dequeueOutputBuffer(mBufferInfo, 50)
            if (outIndex < 0) {
                E("outIndex = $outIndex")
            }
            while (outIndex >= 0) {
                W("outIndex = $outIndex")
                mOutBuffer = mCodec!!.getOutputBuffer(outIndex)
                val outData = ByteArray(mBufferInfo.size)
                mOutBuffer?.apply {
                    position(mBufferInfo.offset)
                    limit(mBufferInfo.offset + mBufferInfo.size)
                    get(outData)
                    mCallback?.onOut(outData)
                }

                mCodec!!.releaseOutputBuffer(outIndex, false)
                outIndex = mCodec!!.dequeueOutputBuffer(mBufferInfo, 50)
            }
        }
    }

}