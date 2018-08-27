//
// Created by DonkeyTomy on 2017/4/14.
//
extern "C"
{
#include <jni.h>
}
#include <g726.h>
#include <g726_codec.h>


CG726* mG726Encoder;
CG726* mG726Decoder;


BYTE *mBufferEncoder;
BYTE *mBufferDecoder;

JNIEXPORT jint JNICALL initG726Codec(JNIEnv *env, jobject obj, jboolean isEncode, jint sampleRate,
                                     jint channelCount, jint bitsPerRawSample, jint bitsPerCodedSample) {
    if (isEncode) {
        mG726Encoder = new CG726();
        if (!mG726Encoder->Open(isEncode, sampleRate, channelCount, bitsPerRawSample, bitsPerCodedSample)) {
            releaseG726Encoder(env, obj);
            return JNI_ERR;
        }
        mBufferEncoder = (BYTE *) malloc(sampleRate);
    } else {
        mG726Decoder = new CG726();
        if (!mG726Decoder->Open(isEncode, sampleRate, channelCount, bitsPerRawSample, bitsPerCodedSample)) {
            releaseG726Decoder(env, obj);
            return JNI_ERR;
        }
        mBufferDecoder = (BYTE *) malloc(sampleRate);
    }
    return JNI_OK;
}

JNIEXPORT void JNICALL releaseG726Encoder(JNIEnv* env, jobject) {
    if (mG726Encoder != NULL) {
        mG726Encoder->Close();
        mG726Encoder = NULL;
    }
    if (mBufferEncoder != NULL) {
        free(mBufferEncoder);
        mBufferEncoder = NULL;
    }
}


JNIEXPORT jint JNICALL encodeG726(JNIEnv* env, jobject,
                                  jbyteArray data, jint dataCount, jbyteArray outData, jint outLen) {
    jbyte* buffer   = env->GetByteArrayElements(data, JNI_FALSE);
//    jsize dataLen    = env->GetArrayLength(data);
//    dataCount = dataLen > dataCount ? dataCount : dataLen;
    memcpy(mBufferEncoder, buffer, dataCount);
    env->ReleaseByteArrayElements(data, buffer, 0);
    BYTE* outBuffer = (BYTE *) malloc(outLen);
    int dataLen;
    if (!mG726Encoder->Encode(mBufferEncoder, dataCount, outBuffer, &dataLen)) {
        free(outBuffer);
        return JNI_ERR;
    }
    outLen = outLen <= dataLen ? outLen : dataLen;
    env->SetByteArrayRegion(outData, 0, outLen, (const jbyte *) outBuffer);
    free(outBuffer);
    return outLen;
}



JNIEXPORT jint JNICALL initG726Decoder(JNIEnv* env, jobject, jint sampleRate,
                                       jint channelCount, jint bitsPerRawSample, jint bitsPerCodedSample) {
    mG726Decoder = new CG726();
    mG726Decoder->Open(false, sampleRate, channelCount, bitsPerRawSample, bitsPerCodedSample);
    mBufferDecoder = (BYTE *) malloc(sampleRate);
    return JNI_OK;
}

JNIEXPORT void JNICALL releaseG726Decoder(JNIEnv* env, jobject) {
    if (mG726Decoder != NULL) {
        mG726Decoder->Close();
        mG726Decoder = NULL;
    }
    if (mBufferDecoder != NULL) {
        free(mBufferDecoder);
        mBufferDecoder = NULL;
    }
}


JNIEXPORT jint JNICALL decodeG726(JNIEnv* env, jobject,
                                  jbyteArray data, jint dataCount, jbyteArray outData, jint outLen) {
    jbyte* buffer   = env->GetByteArrayElements(data, JNI_FALSE);
//    jint dataLen    = env->GetArrayLength(data);
//    dataCount = dataLen > dataCount ? dataCount : dataLen;
    memcpy(mBufferDecoder, buffer, dataCount);
    env->ReleaseByteArrayElements(data, buffer, 0);

    BYTE *outBuffer = (BYTE *) malloc(outLen);
    int dataLen;
    if (!mG726Decoder->Decode(mBufferDecoder, dataCount, outBuffer, &dataLen)) {
        free(outBuffer);
        return JNI_ERR;
    }
    outLen = outLen <= dataLen ? outLen : dataLen;
    env->SetByteArrayRegion(outData, 0, outLen, (const jbyte *) outBuffer);
    free(outBuffer);
    return outLen;
}
