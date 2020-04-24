package com.zzx.media.codec

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaMuxer.OutputFormat
import android.os.SystemClock
import java.nio.ByteBuffer

/**@author Tomy
 * Created by Tomy on 2020/4/24.
 */
class MuxerWrapper(private var mFilePath: String, private var mOutputFormat: Int = OutputFormat.MUXER_OUTPUT_MPEG_4) {

    private var mMuxer: MediaMuxer? = null
    private var mVideoTrackFormat: MediaFormat? = null
    private var mVideoTrackIndex    = -1

    private var mAudioTrackFormat: MediaFormat? = null
    private var mAudioTrackIndex    = -1

    private var mStartMillis    = 0L

    init {
        setupMuxer()
    }

    fun setupMuxer() {
        if (mMuxer == null) {
            mMuxer = MediaMuxer(mFilePath, mOutputFormat)
        }
    }

    fun setFilePath(filePath: String) {
        mFilePath   = filePath
    }

    fun setOutputFormat(format: Int) {
        mOutputFormat   = format
    }

    fun getFilePath() = mFilePath

    fun getOutputFormat()   = mOutputFormat

    /**
     * @param format MediaFormat
     * @param isVideo Boolean
     * @param needJustStart Boolean 是否直接启动,可在只设置一个track的时候使用
     * @param autoStart Boolean 若两个track都已添加是否自动启动.
     */
    @Synchronized
    fun addTrack(format: MediaFormat, isVideo: Boolean, needJustStart: Boolean, autoStart: Boolean) {
        if (mAudioTrackIndex >= 0 && mVideoTrackIndex >= 0) {
            return
        }
        mMuxer?.apply {
            val trackIndex = addTrack(format)
            if (isVideo) {
                mVideoTrackIndex    = trackIndex
                mVideoTrackFormat   = format
            } else {
                mAudioTrackFormat   = format
                mAudioTrackIndex    = trackIndex
            }
            if (needJustStart) {
                start()
            }
            if (autoStart && mAudioTrackIndex >= 0 && mVideoTrackIndex >= 0) {
                start()
            }
        }
    }

    @Synchronized
    fun putStream(outputBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo, isVideo: Boolean) {
        if (isVideo) {
            if (mVideoTrackIndex < 0) {
                return
            }
        } else {
            if (mAudioTrackIndex < 0) {
                return
            }
        }
        val trackIndex = if (isVideo) mVideoTrackIndex else mAudioTrackIndex
        mMuxer?.writeSampleData(trackIndex, outputBuffer, bufferInfo)
    }

    fun start() {
        mMuxer?.apply {
            start()
            mStartMillis    = SystemClock.elapsedRealtime()
        }
    }

    fun release() {
        mMuxer?.apply {
            release()
        }
        mMuxer = null
    }

}