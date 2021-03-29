package com.zzx.media.player

import android.media.MediaPlayer

/**@author Tomy
 * Created by Tomy on 2019/5/21.
 */
abstract class BaseMediaPlayerWrapper: IMediaPlayerWrapper {

    protected var mOnInfoListener: MediaPlayer.OnInfoListener? = null
    protected var mOnPreParedListener: MediaPlayer.OnPreparedListener? = null
    protected var mOnSeekCompleteListener: MediaPlayer.OnSeekCompleteListener? = null
    protected var mOnCompletionListener: MediaPlayer.OnCompletionListener? = null
    protected var mOnErrorListener: MediaPlayer.OnErrorListener? = null

    override fun setOnInfoListener(listener: MediaPlayer.OnInfoListener?) {
        mOnInfoListener = listener
    }

    override fun setOnPreparedListener(listener: MediaPlayer.OnPreparedListener?) {
        mOnPreParedListener = listener
    }

    override fun setOnSeekCompleteListener(listener: MediaPlayer.OnSeekCompleteListener?) {
        mOnSeekCompleteListener = listener
    }

    override fun setOnCompletionListener(listener: MediaPlayer.OnCompletionListener?) {
        mOnCompletionListener = listener
    }

    override fun setOnErrorListener(listener: MediaPlayer.OnErrorListener?) {
        mOnErrorListener = listener
    }
}