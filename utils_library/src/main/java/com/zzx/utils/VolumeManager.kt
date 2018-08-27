package com.zzx.utils

import android.content.Context
import android.media.AudioManager
import timber.log.Timber

/**@author Tomy
 * Created by Tomy on 2014-12-31.
 *
 * 设置各声音通道音量.
 */
class VolumeManager(context: Context, maxLevel: Int = MAX_LEVEL_DEFAULT) {
    private val mManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var mMaxLevel = 0

    var isMicMute: Boolean
        get() = mManager.isMicrophoneMute
        set(mute) {
            mManager.isMicrophoneMute = mute
        }

    fun getSystemVolumeLevel() = getVolumeLevel(AudioManager.STREAM_SYSTEM)

    fun getRingVolumeLevel() = getVolumeLevel(AudioManager.STREAM_RING)

    init {
        setMaxLevel(maxLevel)
    }

    fun isMute() = mManager.ringerMode == AudioManager.RINGER_MODE_SILENT

    fun setMute() {
        mManager.ringerMode = AudioManager.RINGER_MODE_SILENT
    }

    fun setNormal() {
        mManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
    }

    fun setMaxLevel(maxLevel: Int) {
        mMaxLevel = if (maxLevel < 0) MAX_LEVEL_DEFAULT else maxLevel
    }

    fun increaseVolume(type: Int) {
        var level = getVolumeLevel(type)
        setVolume(type, ++level)
    }

    fun decreaseVolume(type: Int) {
        var level = getVolumeLevel(type)
        setVolume(type, --level)
    }

    fun setAllVolume(level: Int, needSound: Boolean = false) {
        setSystemVolume(level, needSound)
        setMusicVolume(level)
        setRingVolume(level)
        setNotifyVolume(level)
        setAlarmVolume(level)
    }

    fun maxAll() {
        setAllVolume(mMaxLevel, true)
    }

    fun minAll() {
        setAllVolume(0, true)
    }

    fun increaseAll() {
        var level = getSystemVolumeLevel()
        ++level
        setAllVolume(level, true)
    }

    fun decreaseAll() {
        var level = getSystemVolumeLevel()
        --level
        setAllVolume(level, true)
    }

    fun setSystemVolume(level: Int, needSound: Boolean = false) {
        setVolume(AudioManager.STREAM_SYSTEM, level, needSound)
    }

    fun setRingVolume(level: Int, needSound: Boolean = false) {
        setVolume(AudioManager.STREAM_RING, level, needSound)
    }

    fun setMusicVolume(level: Int, needSound: Boolean = false) {
        setVolume(AudioManager.STREAM_MUSIC, level, needSound)
    }

    fun setNotifyVolume(level: Int, needSound: Boolean = false) {
        setVolume(AudioManager.STREAM_NOTIFICATION, level, needSound)
    }

    fun setAlarmVolume(level: Int, needSound: Boolean = false) {
        setVolume(AudioManager.STREAM_ALARM, level, needSound)
    }

    @JvmOverloads
    fun setVolume(type: Int, level: Int, needSound: Boolean = false) {
        if (level < 0)
            return
        val volume: Float
        val maxVolume = getMaxVolume(type)
        volume = if (mMaxLevel == 0) {
            (if (level > maxVolume) maxVolume.toInt() else level).toFloat()
        } else {
            if (level >= mMaxLevel) maxVolume else maxVolume / mMaxLevel * level
        }
        val flag = if (needSound) AudioManager.FLAG_PLAY_SOUND else AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
        Timber.e("volume = $volume")
        mManager.setStreamVolume(type, volume.toInt(), flag)
    }

    fun getVolume(type: Int): Int {
        return mManager.getStreamVolume(type)
    }

    fun getMaxVolume(type: Int): Float {
        return mManager.getStreamMaxVolume(type).toFloat()
    }

    fun getVolumeLevel(type: Int): Int {
        val volume = getVolume(type)
        if (mMaxLevel == 0 || volume == 0) {
            return volume
        }
        val maxV = getMaxVolume(type)
        val maxVolume = maxV / mMaxLevel
        return if (volume > maxVolume) (volume / maxVolume).toInt() else (maxVolume / volume).toInt()
    }

    companion object {
        const val MAX_LEVEL_DEFAULT = 10
    }
}
