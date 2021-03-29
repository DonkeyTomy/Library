package com.zzx.media.codec

import android.media.*
import android.os.Process
import android.util.Log
import java.io.IOException
import java.nio.ByteBuffer

/*
 * AudioVideoRecordingSample
 * Sample project to cature audio and video from internal mic/camera and save as MPEG4 file.
 *
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 *
 * File name: MediaAudioEncoder.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * All files in the folder are under this Apache License, Version 2.0.
*/
class MediaAudioEncoder(muxer: MediaMuxerWrapper, listener: MediaEncoderListener) :
    MediaEncoder(muxer, listener) {

    private var mSampleRate = SAMPLE_RATE

    private var mChannels = 1

    fun init(sampleRate: Int, channelCount: Int) {
        mSampleRate = sampleRate
        mChannels = channelCount
    }

    private var mAudioThread: AudioThread? = null



    @Throws(IOException::class)
    override fun prepare() {
        if (DEBUG) Log.v(TAG, "prepare:")
        mTrackIndex = -1
        mIsEOS = false
        mMuxerStarted = mIsEOS
        // prepare MediaCodec for AAC encoding of audio data from internal mic.
        val audioCodecInfo = selectAudioCodec(MIME_TYPE)
        if (audioCodecInfo == null) {
            Log.e(TAG, "Unable to find an appropriate codec for " + MIME_TYPE)
            return
        }
        if (DEBUG) Log.i(TAG, "selected codec: " + audioCodecInfo.name)
        val audioFormat = MediaFormat.createAudioFormat(MIME_TYPE, mSampleRate, mChannels)
        audioFormat.apply {
            setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_MONO)
            setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE)
            setInteger(MediaFormat.KEY_CHANNEL_COUNT, mChannels)
        }

        //		audioFormat.setLong(MediaFormat.KEY_MAX_INPUT_SIZE, inputFile.length());
//      audioFormat.setLong(MediaFormat.KEY_DURATION, (long)durationInMs );
        if (DEBUG) Log.i(TAG, "format: $audioFormat")
        mMediaCodec = MediaCodec.createEncoderByType(MIME_TYPE)
        mMediaCodec.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        if (DEBUG) Log.i(TAG, "prepare finishing")
        mListener?.onPrepared(this)
    }

    override fun startRecording() {
        mMediaCodec.start()
        super.startRecording()
        // create and execute audio capturing thread using internal mic
        /*if (mAudioThread == null) {
            mAudioThread = AudioThread()
            mAudioThread!!.start()
        }*/
    }

    override fun release() {
//        mAudioThread = null
        super.release()
    }

    fun encodeData(byteBuffer: ByteBuffer, size: Int) {
        encode(byteBuffer, size, ptsUs)
        frameAvailableSoon()
    }

    /**
     * Thread to capture audio data from internal mic as uncompressed 16bit PCM data
     * and write them to the MediaCodec encoder
     */
    private inner class AudioThread : Thread() {
        override fun run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
            try {
                val minBufferSize = AudioRecord.getMinBufferSize(
                    SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                var bufferSize = SAMPLES_PER_FRAME * FRAMES_PER_BUFFER
                if (bufferSize < minBufferSize) bufferSize =
                    (minBufferSize / SAMPLES_PER_FRAME + 1) * SAMPLES_PER_FRAME * 2
                var audioRecord: AudioRecord? = null
                for (source in AUDIO_SOURCES) {
                    try {
                        audioRecord = AudioRecord(
                            source, SAMPLE_RATE,
                            AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize
                        )
                        if (audioRecord.state != AudioRecord.STATE_INITIALIZED) audioRecord = null
                    } catch (e: Exception) {
                        audioRecord = null
                    }
                    if (audioRecord != null) break
                }
                if (audioRecord != null) {
                    try {
                        if (mIsCapturing) {
                            if (DEBUG) Log.v(TAG, "AudioThread:start audio recording")
                            val buf = ByteBuffer.allocateDirect(SAMPLES_PER_FRAME)
                            var readBytes: Int
                            audioRecord.startRecording()
                            try {
                                while (mIsCapturing && !mRequestStop && !mIsEOS) {

                                    // read audio data from internal mic
                                    buf.clear()
                                    readBytes = audioRecord.read(buf, SAMPLES_PER_FRAME)
                                    if (readBytes > 0) {
                                        // set audio data to encoder
                                        buf.position(readBytes)
                                        buf.flip()
                                        encode(buf, readBytes, ptsUs)
                                        frameAvailableSoon()
                                    }
                                }
                                frameAvailableSoon()
                            } finally {
                                audioRecord.stop()
                            }
                        }
                    } finally {
                        audioRecord.release()
                    }
                } else {
                    Log.e(TAG, "failed to initialize AudioRecord")
                }
            } catch (e: Exception) {
                Log.e(TAG, "AudioThread#run", e)
            }
            if (DEBUG) Log.v(TAG, "AudioThread:finished")
        }
    }

    companion object {
        private const val DEBUG = false // TODO set false on release
        private const val TAG = "AudioMediaEncoderT"
        private const val MIME_TYPE = "audio/mp4a-latm"
        private const val SAMPLE_RATE =
            48000 // 44.1[KHz] is only setting guaranteed to be available on all devices.
        private const val BIT_RATE = 64000
        const val SAMPLES_PER_FRAME = 1024 // AAC, bytes/frame/channel
        const val FRAMES_PER_BUFFER = 25 // AAC, frame/buffer/sec
        private val AUDIO_SOURCES = intArrayOf(
            MediaRecorder.AudioSource.MIC,
            MediaRecorder.AudioSource.DEFAULT,
            MediaRecorder.AudioSource.CAMCORDER,
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            MediaRecorder.AudioSource.VOICE_RECOGNITION
        )

        /**
         * select the first codec that match a specific MIME type
         * @param mimeType
         * @return
         */
        private fun selectAudioCodec(mimeType: String): MediaCodecInfo? {
            if (DEBUG) Log.v(TAG, "selectAudioCodec:")
            var result: MediaCodecInfo? = null
            // get the list of available codecs
            val numCodecs = MediaCodecList.getCodecCount()
            LOOP@ for (i in 0 until numCodecs) {
                val codecInfo = MediaCodecList.getCodecInfoAt(i)
                if (!codecInfo.isEncoder) {    // skipp decoder
                    continue
                }
                val types = codecInfo.supportedTypes
                for (j in types.indices) {
                    if (DEBUG) Log.i(TAG, "supportedType:" + codecInfo.name + ",MIME=" + types[j])
                    if (types[j].equals(mimeType, ignoreCase = true)) {
                        if (result == null) {
                            result = codecInfo
                            break@LOOP
                        }
                    }
                }
            }
            return result
        }
    }

    init {
        mIsVideo = false
    }
}