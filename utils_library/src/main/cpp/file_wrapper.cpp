//
// Created by DonkeyTomy on 2019/5/10.
//
#include <file_wrapper.h>


JNIEXPORT jboolean JNICALL open(JNIEnv* env, jobject obj, jstring path) {
    jboolean isCopy;
    const char *filePath = env->GetStringUTFChars(path, &isCopy);
    bool success = mFileWrapper->open(filePath, "r+");
    env->ReleaseStringUTFChars(path, filePath);
    if (!success) {
        LOG_W(TAG, "open(). filePath = %s. Failed", filePath);
        return JNI_FALSE;
    }
//    LOG_W(TAG, "open(). filePath = %s. Success", filePath);
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL write(JNIEnv* env, jobject obj, jstring msg) {
    jboolean isCopy;
    const char *tmp = env->GetStringUTFChars(msg, &isCopy);
//    int len = static_cast<int>(strlen(tmp));
    int result = mFileWrapper->write(tmp);
//    LOG_W(TAG, "write(). msg = %s, result = %d. len = %d.", tmp, result, len);
    env->ReleaseStringUTFChars(msg, tmp);
    if (result == -1) {
        return JNI_FALSE;
    } else {
        return JNI_TRUE;
    }
}

JNIEXPORT jboolean JNICALL writeOnce(JNIEnv* env, jobject obj, jstring path, jstring msg) {
    if (open(env, obj, path) == JNI_FALSE) {
        return JNI_FALSE;
    }
    jboolean success = write(env, obj, msg);
    close(env, obj);
    return success;
}


JNIEXPORT void JNICALL close(JNIEnv *env, jobject obj) {
    mFileWrapper->close();
}

const static JNINativeMethod convertNativeMethod[] = {
        {"open",      "(Ljava/lang/String;)Z", (void *) open},
        {"write",     "(Ljava/lang/String;)Z",                   (void *) write},
        {"writeOnce", "(Ljava/lang/String;Ljava/lang/String;)Z",                   (void *) writeOnce},
        {"close",     "()V",                   (void *) close}
};

jint registerMethod(JNIEnv *env) {
    jclass clz = env->FindClass("com/zzx/utils/file/JniFile");
    if (clz == NULL) {
        LOG_E(TAG, "cannot find class");
        return JNI_ERR;
    }
//    LOG_E(TAG, "find class");
    if (env->RegisterNatives(clz, convertNativeMethod,
                             sizeof(convertNativeMethod) / sizeof(convertNativeMethod[0])) !=
        JNI_OK) {
        LOG_E(TAG, "RegisterNatives failed");
        return JNI_ERR;
    }
    LOG_W(TAG, "RegisterNatives success");
    return JNI_OK;
}

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return JNI_ERR;
    }
    registerMethod(env);
    return JNI_VERSION_1_4;
}

