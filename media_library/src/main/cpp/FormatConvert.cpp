//
// Created by donke on 2017/7/5.
//
extern "C"
{
#include <jni.h>
}

#include "FormatConverter.h"

FormatConverter* mFormatConverter;
unsigned char* mBufferSrc;
unsigned char* mBufferDst;
size_t mSrcLen, mDstLen;

JNIEXPORT jint JNICALL init(JNIEnv* env, jobject ,int width, int height, int typeFrom, int typeTo, int bitPerFrom, int bitPerTo) {
    mFormatConverter = new FormatConverter(width, height, typeFrom, typeTo);
    mSrcLen = (size_t) (width * height * (bitPerFrom / 8.0f));
    mDstLen = (size_t) (width * height * (bitPerTo / 8.0f));
    mBufferSrc  = (unsigned char *) malloc(mSrcLen);
    mBufferDst  = (unsigned char *) malloc(mDstLen);
    return JNI_OK;
}

JNIEXPORT void JNICALL convert(JNIEnv* env, jobject , jbyteArray _data, jbyteArray outData) {
    jbyte * buffer = env->GetByteArrayElements(_data, 0);
    memcpy(mBufferSrc, buffer, mSrcLen);
    env->ReleaseByteArrayElements(_data, buffer, 0);
    mBufferDst = mFormatConverter->formatConvert(mBufferSrc);
    env->SetByteArrayRegion(outData, 0, mDstLen, (const jbyte *) mBufferDst);
}

JNIEXPORT void JNICALL release() {
    free(mBufferSrc);
    free(mBufferDst);
    delete mFormatConverter;
}


const static JNINativeMethod convertNativeMethod[] = {
        {"initParams",    "(IIIIII)I",      (void *) init},
        {"convert", "([B)V",       (void *) convert},
        {"release", "(V)V",       (void *) release}
};

jint registerConvertMethod(JNIEnv* env) {
    jclass clz = env->FindClass("com/zzx/media/FormatConvert");
    if (clz == NULL) {
        return JNI_ERR;
    }
    if (env->RegisterNatives(clz, convertNativeMethod, sizeof(convertNativeMethod) / sizeof(convertNativeMethod[0])) != JNI_OK) {
        return JNI_ERR;
    }
    return JNI_OK;
}
