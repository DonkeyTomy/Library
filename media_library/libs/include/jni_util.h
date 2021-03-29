//
// Created by Tomy on 2017/4/14.
//

#ifndef FFMPEG_JNI_UTIL_H
#define FFMPEG_JNI_UTIL_H

#include <android/log.h>

#ifndef LOG_TAG
#define LOG_TAG "jniUtil"
//#define LOG_I(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
//#define LOG_W(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
//#define LOG_E(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOG_I(TAG, ...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOG_W(TAG, ...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOG_E(TAG, ...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#endif

#endif //FFMPEG_JNI_UTIL_H

