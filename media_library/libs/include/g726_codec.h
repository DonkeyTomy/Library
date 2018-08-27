//
// Created by donke on 2017/4/17.
//

#ifndef FFMPEG_G726_CODEC_H
#define FFMPEG_G726_CODEC_H
typedef unsigned char BYTE;


    jint initG726Codec(JNIEnv *jniEnv, jobject, jboolean isEncode, jint sampleRate,
                       jint channelCount, jint bitPerRawSample, jint bitsPerCodedSample);

    void releaseG726Encoder(JNIEnv *env, jobject);

    jint encodeG726(JNIEnv *env, jobject,
                    jbyteArray data, jint dataCount, jbyteArray outData, jint outLen);


    jint initG726Decoder(JNIEnv *jniEnv, jobject, jint sampleRate,
                         jint channelCount, jint bitPerRawSample, jint bitsPerCodedSample);

    void releaseG726Decoder(JNIEnv *env, jobject);

    jint decodeG726(JNIEnv *env, jobject,
                    jbyteArray data, jint dataCount, jbyteArray outData, jint outLen);


#endif //FFMPEG_G726_CODEC_H