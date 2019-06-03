//
// Created by DonkeyTomy on 2019/5/10.
//

#ifndef RV123_FILE_WRAPPER_H
#define RV123_FILE_WRAPPER_H
extern "C"
{
#include <jni.h>
}
#include <string>
#include <jni_util.h>
#include <stdio.h>
#include <string.h>
#include <sys/types.h>
#include "FileWrapper.h"

#define TAG "FileWrapper"

FileWrapper *mFileWrapper = new FileWrapper();

jint registerMethod(JNIEnv* env);

JNIEXPORT jboolean JNICALL write(JNIEnv* env, jobject, jstring msg);

JNIEXPORT jboolean JNICALL writeOnce(JNIEnv* env, jobject, jstring path, jstring msg);

JNIEXPORT jboolean JNICALL open(JNIEnv* env, jobject, jstring path);

JNIEXPORT void JNICALL close(JNIEnv* env, jobject);


#endif //RV123_FILE_WRAPPER_H
