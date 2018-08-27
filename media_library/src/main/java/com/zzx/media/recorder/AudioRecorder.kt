package com.zzx.media.recorder

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.support.annotation.IntDef
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

/**@author Tomy
 * Created by Tomy on 2018/1/15.
 */
class AudioRecorder(sampleRate: Int, channelCount: Int, @Format audioFormat: Int) {

    private var mRecorder: AudioRecord
    private var mBufferMinSize: Int = 0
    private val mBuffer: ByteBuffer
    private var mBufferArray: ByteArray
    private var mAudioReadCallback: IAudioRecorder.AudioReadCallback? = null
    private var mIsInterrupt: AtomicBoolean = AtomicBoolean(false)

    init {
        val channelConfig = when(channelCount) {
            1 -> AudioFormat.CHANNEL_IN_MONO
            2 -> AudioFormat.CHANNEL_IN_STEREO
            else -> {
                AudioFormat.CHANNEL_IN_MONO
            }
        }
        mBufferMinSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        W("channelConfig[$channelConfig].mBufferMinSize = $mBufferMinSize audioFormat[$audioFormat]")
        mRecorder = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, mBufferMinSize)
        mBuffer = ByteBuffer.allocate(mBufferMinSize)
        mBufferArray = ByteArray(mBufferMinSize)
    }

    fun setAudioReadCallback(audioReadCallback: IAudioRecorder.AudioReadCallback) {
        mAudioReadCallback = audioReadCallback
    }

    fun stopRecord() {
        mIsInterrupt.set(true)
        mRecorder.stop()
    }

    fun startRecord() {
        mIsInterrupt.set(false)
        mRecorder.startRecording()
        var size: Int
        Flowable.create<ByteArray>({
            e: FlowableEmitter<ByteArray> ->
            run {
                while (!mIsInterrupt.get()) {
                    mBuffer.clear()
                    size = mRecorder.read(mBufferArray, 0, mBufferMinSize)
                    W("size = $size")
                    /*if (size != mBufferMinSize) {
                        mBufferArray = ByteArray(size)
                    }*/
                    if (size > 0) {
//                        mBuffer.setupRecorder(mBufferArray)
                        e.onNext(mBufferArray)
                    }
                }
                e.onComplete()
            }
        }, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.io())
                .subscribe {
                    data: ByteArray ->
                    mAudioReadCallback?.onAudioRead(data)
                }
    }

    fun W(msg: String) {
        Timber.w("<-------------- $msg ----------->")
    }

    fun E(msg: String) {
        Timber.e("<-------------- $msg ----------->")
    }

    @IntDef(AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Format

}

