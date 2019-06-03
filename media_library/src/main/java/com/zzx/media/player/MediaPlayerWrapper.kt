package com.zzx.media.player

import android.media.MediaPlayer
import android.view.Surface
import timber.log.Timber

/**@author Tomy
 * Created by Tomy on 2019/5/21.
 */
class MediaPlayerWrapper: BaseMediaPlayerWrapper() {

    private val mPlayer by lazy { MediaPlayer() }

    override fun setSpeed(speedRate: Float) {
        try {
            mPlayer.playbackParams = mPlayer.playbackParams.apply {
                speed = speedRate
                Timber.e("speed = $speed")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getSpeed(): Float {
        return mPlayer.playbackParams.speed
    }

    override fun getSpeedMax(): Float {
        return 5.0f
    }

    override fun seekTo(position: Long) {
        mPlayer.seekTo(position, MediaPlayer.SEEK_CLOSEST)
    }

    override fun getDuration(): Long {
        return mPlayer.duration.toLong()
    }

    override fun getCurrentPosition(): Long {
        return mPlayer.currentPosition.toLong()
    }

    override fun getWidth(): Int {
        return mPlayer.videoWidth
    }

    override fun getHeight(): Int {
        return mPlayer.videoHeight
    }

    override fun isPlaying(): Boolean {
        return mPlayer.isPlaying
    }

    override fun setDataSource(path: String) {
        mPlayer.setDataSource(path)
    }

    override fun setSurface(surface: Surface) {
        mPlayer.setSurface(surface)
    }

    override fun prepareAsync() {
        mPlayer.prepareAsync()
    }

    override fun start() {
        mPlayer.start()
    }

    override fun pause() {
        mPlayer.pause()
    }

    override fun stop() {
        mPlayer.stop()
    }

    override fun reset() {
        mPlayer.reset()
    }

    override fun release() {
        mPlayer.release()
    }

    override fun setOnInfoListener(listener: MediaPlayer.OnInfoListener?) {
        mPlayer.setOnInfoListener(listener)
    }

    override fun setOnPreparedListener(listener: MediaPlayer.OnPreparedListener?) {
        mPlayer.setOnPreparedListener(listener)
    }

    override fun setOnSeekCompleteListener(listener: MediaPlayer.OnSeekCompleteListener?) {
        mPlayer.setOnSeekCompleteListener(listener)
    }

    override fun setOnCompletionListener(listener: MediaPlayer.OnCompletionListener?) {
        mPlayer.setOnCompletionListener(listener)
    }

    override fun setOnErrorListener(listener: MediaPlayer.OnErrorListener?) {
        mPlayer.setOnErrorListener(listener)
    }
}