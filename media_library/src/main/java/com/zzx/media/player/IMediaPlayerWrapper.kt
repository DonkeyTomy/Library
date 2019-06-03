package com.zzx.media.player

import android.media.MediaPlayer
import android.view.Surface

/**@author Tomy
 * Created by Tomy on 2019/5/21.
 */
interface IMediaPlayerWrapper {

    fun setSpeed(speedRate: Float)

    fun getSpeed(): Float

    fun getSpeedMax(): Float

    fun seekTo(position: Long)

    fun getDuration(): Long

    fun getCurrentPosition(): Long

    fun getWidth(): Int

    fun getHeight(): Int

    fun isPlaying(): Boolean

    fun setDataSource(path: String)

    fun setSurface(surface: Surface)

    fun prepareAsync()

    fun start()

    fun pause()

    fun stop()

    fun reset()

    fun release()

    fun setOnInfoListener(listener: MediaPlayer.OnInfoListener?)

    fun setOnPreparedListener(listener: MediaPlayer.OnPreparedListener?)

    fun setOnSeekCompleteListener(listener: MediaPlayer.OnSeekCompleteListener?)

    fun setOnCompletionListener(listener: MediaPlayer.OnCompletionListener?)

    fun setOnErrorListener(listener: MediaPlayer.OnErrorListener?)

}