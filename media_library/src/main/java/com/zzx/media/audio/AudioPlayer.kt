package com.zzx.media.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Process
import timber.log.Timber
import java.io.File
import java.nio.ByteBuffer

/**@author Tomy
 * Created by Tomy on 6/1/2021.
 */
class AudioPlayer {

    private var mByteBuffer: ByteBuffer? = null

    private var mAudioTrack: AudioTrack? = null

    private var mFile: File? = null

    private var mMode = AudioTrack.MODE_STREAM

    private var mAudioFileTrackThread: AudioFileTrackThread? = null

    fun initPlayer(format: AudioFormat, sampleRate: Int = 48000, bitsPerSample: Int = 16, buffersPerSecond: Int = 100, mode: Int = AudioTrack.MODE_STREAM, sessionId: Int = AudioManager.AUDIO_SESSION_ID_GENERATE) {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()
        val bufferSizeInByte = sampleRate * bitsPerSample / buffersPerSecond
        mMode = mode
        mAudioTrack = AudioTrack(audioAttributes, format, bufferSizeInByte, mode, sessionId)
    }

    fun setSourceFilePath(filePath: String) {
        mFile = File(filePath)
    }

    fun startPlay() {
        if (mFile != null) {
            if (!(mFile!!.exists() && mFile!!.canRead())) {
                Timber.e("File:$mFile not exist or can not read.")
                return
            }
        }
        if (mMode == AudioTrack.MODE_STREAM) {
            mAudioTrack?.play()
            if (mFile != null) {

            }
        }
    }

    private fun startAudioFileTrackPlay() {
        mAudioFileTrackThread = AudioFileTrackThread()
        mAudioFileTrackThread?.start()
    }

    private fun pauseAudioFileTrack() {
        mAudioFileTrackThread?.pause()
    }

    fun pause() {
        mAudioTrack?.apply {
            if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                pause()
            }
        }
    }

    fun resume() {
        mAudioTrack?.apply {
            if (playState == AudioTrack.PLAYSTATE_PAUSED) {
                play()
            }
        }
    }

    fun stop() {
        mAudioTrack?.apply {
            if (playState == AudioTrack.PLAYSTATE_PAUSED || playState == AudioTrack.PLAYSTATE_PLAYING) {
                stop()
            }
        }
    }

    fun release() {
        mAudioTrack?.release()
    }

    inner class AudioFileTrackThread: Thread() {
        private var keepAlive = true

        fun pause() {

        }

        override fun run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
            when (keepAlive) {

            }
        }

    }

}