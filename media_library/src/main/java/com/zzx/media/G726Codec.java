package com.zzx.media;

/**@author DonkeyTomy
 * Created by DonkeyTomy on 2017/4/19.
 */

public class G726Codec {
    private boolean mIsEncoder = false;
    private boolean mInitSuccess = false;

    public G726Codec() {
        /*mIsEncoder = encoder;
        if (encoder) {
            initG726Codec(encoder, sampleRate, channelCount, bitsPerRawSample, bitsPerCodecSample);
        } else {
            initG726Decoder(sampleRate, channelCount, bitsPerRawSample, bitsPerCodecSample);
        }*/
    }

    public boolean initCodec(boolean encoder, int sampleRate, int channelCount, int bitsPerRawSample, int bitsPerCodecSample) {
        mIsEncoder = encoder;
        mInitSuccess = initG726Codec(encoder, sampleRate, channelCount, bitsPerRawSample, bitsPerCodecSample) >= 0;
        return mInitSuccess;
    }

    /**
     * @return Encoded Data's size. If return -1, means encode Failed.
     * {@link #mIsEncoder} && {@link #mInitSuccess} must be True. Or will do nothing and return -1.
     * */
    public int encodeData(byte[] input, int inputSize, byte[] output, int outputSize) {
        if (mIsEncoder && mInitSuccess) {
            return encodeG726(input, inputSize, output, outputSize);
        }
        return -1;
    }

    /**@return Decoded Data's size. If return -1, means Decode Failed.
     * {@link #mIsEncoder} must be false. And {@link #mInitSuccess} must be True. Or will do nothing and return -1.
     * */
    public int decodeData(byte[] input, int inputSize, byte[] output, int outputSize) {
        if ((!mIsEncoder) && mInitSuccess) {
            return decodeG726(input, inputSize, output, outputSize);
        }
        return -1;
    }

    public void release() {
        if (!mInitSuccess) {
            return;
        }
        if (mIsEncoder) {
            releaseG726Encoder();
        } else {
            releaseG726Decoder();
        }
    }

    static {
        System.loadLibrary("native-lib");
    }

    /**
     *
     * */
    private native int initG726Codec(boolean isEncoder, int sampleRate, int channelCount, int bitsPerRawSample, int bitsPerCodedSample);
//    private native int initG726Decoder(int sampleRate, int channelCount, int bitsPerRawSample, int bitsPerCodedSample);
    /**
     * @return Encoded Data's size. If return -1, means encode Failed.
     * */
    private native int encodeG726(byte[] input, int count, byte[] output, int outputSize);
    /**
     * @return Decoded Data's size. If return -1, means Decode Failed.
     * */
    private native int decodeG726 (byte[] input, int count, byte[] output, int outputSize);
    private native void releaseG726Decoder();
    private native void releaseG726Encoder();
}
