#include <jni.h>
#include <string>
#include <jni_util.h>
#include <g726_codec.h>
#include "FormatConvert.h"

extern "C"
{
#include "libavformat/avformat.h"
#include "libavcodec/avcodec.h"
}

#define TAG "nativeLib"

JNIEXPORT jstring JNICALL
Java_com_zzx_ffmpeg_AudioActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}


JNIEXPORT jstring avFormatInfo(JNIEnv *env, jobject) {
    char info[40000] = {0};
    av_register_all();

    AVInputFormat *inputFormat = av_iformat_next(NULL);
    AVOutputFormat *outputFormat = av_oformat_next(NULL);

    while (inputFormat != NULL) {
        sprintf(info, "%s[In ][%10s]\n", info, inputFormat->name);
        inputFormat = inputFormat->next;
    }

    while (outputFormat != NULL) {
        sprintf(info, "%s[Out ][%10s]\n", info, outputFormat->name);
        outputFormat = outputFormat->next;
    }

    return env->NewStringUTF(info);
}

JNIEXPORT jstring avCodecInfo(JNIEnv *env, jobject) {
    char info[40000] = {0};

    av_register_all();

    AVCodec *c_temp = av_codec_next(NULL);


    while (c_temp != NULL) {
        if (c_temp->decode != NULL) {
            sprintf(info, "%s decode:", info);
        } else {
            sprintf(info, "%s encode:", info);
        }
        switch (c_temp->type) {
            case AVMEDIA_TYPE_VIDEO:
                sprintf(info, "%s(video):", info);
                break;
            case AVMEDIA_TYPE_AUDIO:
                sprintf(info, "%s(audio):", info);
                break;
            default:
                sprintf(info, "%s(other):", info);
                break;
        }
        sprintf(info, "%s[%10s]\n", info, c_temp->name);
        c_temp = c_temp->next;
    }
    LOG_W(TAG, "info: %s", info);
    return env->NewStringUTF(info);
}


JNIEXPORT jint JNICALL openAVCodecById(JNIEnv* env, jobject, jint id) {
    AVFormatContext* pFormatCtx;
    AVOutputFormat* pOutFmt;
    AVStream* videoStream;
    AVCodecContext* pCodecCtx;
    AVCodec* pCodec;
    AVPacket packet;
    uint8_t* pictureBuf;
    AVFrame* pFrame;
    int pictureSize;
    int ySize;
    int frameCount = 0;
    FILE* inFile = fopen("/sdcard/test.mp4", "rb");
    const char* outFile = "dst.h264";

    av_register_all();

    pFormatCtx = avformat_alloc_context();
    pOutFmt = av_guess_format(NULL, outFile, NULL);
    pFormatCtx->oformat = pOutFmt;

    if (avio_open(&pFormatCtx->pb, outFile, AVIO_FLAG_READ_WRITE) < 0) {
        return -1;
    }

    videoStream = avformat_new_stream(pFormatCtx, 0);

    if (videoStream == NULL) {
        return -1;
    }

    pCodecCtx = videoStream->codec;
    pCodecCtx->codec_id     = pOutFmt->video_codec;
    pCodecCtx->codec_type   = AVMEDIA_TYPE_VIDEO;
    pCodecCtx->pix_fmt      = AV_PIX_FMT_YUV420P;
    pCodecCtx->width        = 420;
    pCodecCtx->height       = 320;
    pCodecCtx->bit_rate     = 400000;
    pCodecCtx->gop_size     = 250;
    pCodecCtx->time_base.num= 1;
    pCodecCtx->time_base.den= 25;
    return JNI_OK;
}

jint simplestYUV420_split(JNIEnv* env, jobject, jstring url, int width, int height, int num) {
    return JNI_OK;
}

jint open(JNIEnv *env, jobject, jint key) {
    return key - 1;
}

jstring getString(JNIEnv* env, jobject) {
    char * hello = (char *) "C++";
    return env->NewStringUTF(hello);
}
/*
static const JNINativeMethod methods[] = {
        {"open", "(I)I", (void*) open},
        {"getString", "()Ljava/lang/String;", (void*) getString},
        {"avFormatInfo", "()Ljava/lang/String;", (void*) avFormatInfo},
        {"avCodecInfo", "()Ljava/lang/String;", (void*) avCodecInfo},
};*/

static const JNINativeMethod g726Method[] = {
        {"initG726Codec", "(ZIIII)I", (void*) initG726Codec},
//        {"initG726Decoder", "(IIII)I", (void*) initG726Decoder},
        {"releaseG726Decoder", "()V", (void*) releaseG726Decoder},
        {"releaseG726Encoder", "()V", (void*) releaseG726Encoder},
        {"encodeG726", "([BI[BI)I", (void*) encodeG726},
        {"decodeG726", "([BI[BI)I", (void*) decodeG726},
};

jint register_jni(JNIEnv* env) {
    /*jclass clazz = env->FindClass("com/zzx/ffmpeg/MainActivity");
    if (clazz == NULL) {
        return JNI_ERR;
    }

    if (env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0])) != JNI_OK) {
        return JNI_ERR;
    }*/
    registerConvertMethod(env);
    jclass encodeClazz = env->FindClass("com/zzx/media/G726Codec");
    if (encodeClazz == NULL) {
        LOG_E(TAG, "cannot find class");
        return JNI_ERR;
    }
    if (env->RegisterNatives(encodeClazz, g726Method, sizeof(g726Method) / sizeof(g726Method[0])) != JNI_OK) {
        LOG_E(TAG, "RegisterNatives failed");
        return JNI_ERR;
    }
    LOG_W(TAG, "RegisterNatives success");
    return JNI_OK;
}

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv *env = NULL;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return JNI_ERR;
    }
    register_jni(env);
    return JNI_VERSION_1_4;
}
