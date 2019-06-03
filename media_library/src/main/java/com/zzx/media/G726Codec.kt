package com.zzx.media

import timber.log.Timber

/**@author DonkeyTomy
 * Created by DonkeyTomy on 2017/4/19.
 */

class G726Codec {
    private var mIsEncoder = false
    private var mInitSuccess = false

    constructor(encoder: Boolean, sampleRate: Int, channelCount: Int, bitsPerRawSample: Int, bitsPerCodecSample: Int) {
        initCodec(encoder, sampleRate, channelCount, bitsPerRawSample, bitsPerCodecSample)
    }

    constructor()

    fun initCodec(encoder: Boolean, sampleRate: Int, channelCount: Int, bitsPerRawSample: Int, bitsPerCodecSample: Int): Boolean {
        if (mInitSuccess) {
            return true
        }
        mIsEncoder = encoder
        mInitSuccess = initG726Codec(encoder, sampleRate, channelCount, bitsPerRawSample, bitsPerCodecSample) >= 0
        return mInitSuccess
    }

    /**
     * @return Encoded Data's size. If return -1, means encode Failed.
     * [.mIsEncoder] && [.mInitSuccess] must be True. Or will do nothing and return -1.
     */
    fun encodeData(input: ByteArray, inputSize: Int, output: ByteArray, outputSize: Int): Int {
        return if (mIsEncoder && mInitSuccess) {
            encodeG726(input, inputSize, output, outputSize)
        } else -1
    }

    /**@return Decoded Data's size. If return -1, means Decode Failed.
     * [.mIsEncoder] must be false. And [.mInitSuccess] must be True. Or will do nothing and return -1.
     */
    fun decodeData(input: ByteArray, inputSize: Int, output: ByteArray, outputSize: Int): Int {
        return if (!mIsEncoder && mInitSuccess) {
            decodeG726(input, inputSize, output, outputSize)
        } else -1
    }

    fun release() {
        if (!mInitSuccess) {
            return
        }
        if (mIsEncoder) {
            releaseG726Encoder()
        } else {
            releaseG726Decoder()
        }
        mInitSuccess = false
    }

    /**
     *
     */
    private external fun initG726Codec(isEncoder: Boolean, sampleRate: Int, channelCount: Int, bitsPerRawSample: Int, bitsPerCodedSample: Int): Int
    //    private native int initG726Decoder(int sampleRate, int channelCount, int bitsPerRawSample, int bitsPerCodedSample);
    /**
     * @return Encoded Data's size. If return -1, means encode Failed.
     */
    private external fun encodeG726(input: ByteArray, count: Int, output: ByteArray, outputSize: Int): Int

    /**
     * @return Decoded Data's size. If return -1, means Decode Failed.
     */
    private external fun decodeG726(input: ByteArray, count: Int, output: ByteArray, outputSize: Int): Int

    private external fun releaseG726Decoder()
    private external fun releaseG726Encoder()

    companion object {

        init {
            Timber.e("loadLibrary [native-lib]")
            System.loadLibrary("native-lib")
        }
    }
}
