package com.zzx.media.player

import android.view.Surface

/**@author Tomy
 * Created by Tomy on 2019/5/21.
 */
class IjkPlayerWrapper: BaseMediaPlayerWrapper()/*,
        IMediaPlayer.OnCompletionListener,
        IMediaPlayer.OnErrorListener, IMediaPlayer.OnPreparedListener, IMediaPlayer.OnInfoListener, IMediaPlayer.OnSeekCompleteListener
*/{

    private var mPlayer = MediaPlayerWrapper()

    init {
        mPlayer.apply {
            /*setOnCompletionListener(this@IjkPlayerWrapper)
            setOnErrorListener(this@IjkPlayerWrapper)
            setOnPreparedListener(this@IjkPlayerWrapper)
            setOnInfoListener(this@IjkPlayerWrapper)
            setOnSeekCompleteListener(this@IjkPlayerWrapper)*/
        }

    }

    override fun setSpeed(speedRate: Float) {
        mPlayer.setSpeed(speedRate)

    }

    override fun getSpeed(): Float {
        return mPlayer.getSpeed()
    }

    override fun getSpeedMax(): Float {
        return 5.0f
    }

    override fun seekTo(position: Long) {
        mPlayer.seekTo(position)
    }

    override fun getDuration(): Long {
        return mPlayer.getDuration()
    }

    override fun getCurrentPosition(): Long {
        return mPlayer.getCurrentPosition()
    }

    override fun getWidth(): Int {
        return mPlayer.getWidth()
    }

    override fun getHeight(): Int {
        return mPlayer.getHeight()
    }

    override fun isPlaying(): Boolean {
        return mPlayer.isPlaying()
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

    /*override fun onSeekComplete(p0: IMediaPlayer?) {
        mOnSeekCompleteListener?.onSeekComplete(null)
    }

    override fun onInfo(p0: IMediaPlayer?, what: Int, extra: Int): Boolean {
        mOnInfoListener?.onInfo(null, what, extra)
        return true
    }

    override fun onPrepared(p0: IMediaPlayer?) {
        mOnPreParedListener?.onPrepared(null)
    }

    override fun onError(p0: IMediaPlayer?, p1: Int, p2: Int): Boolean {
        mOnErrorListener?.onError(null, p1, p2)
        return true
    }

    override fun onCompletion(p0: IMediaPlayer?) {
        mOnCompletionListener?.onCompletion(null)
    }*/

}