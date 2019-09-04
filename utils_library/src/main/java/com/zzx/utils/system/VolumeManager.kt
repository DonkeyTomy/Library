package com.zzx.utils.system

import android.content.Context
import android.media.AudioManager
import android.provider.Settings
import timber.log.Timber

/**@author Tomy
 * Created by Tomy on 2014-12-31.
 *
 * 设置各声音通道音量.
 */
class VolumeManager(var context: Context, maxLevel: Int = MAX_LEVEL_DEFAULT) {
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
        setVoiceCallVolume(level)
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

    fun setVoiceCallVolume(level: Int, needSound: Boolean = false) {
        setVolume(AudioManager.STREAM_VOICE_CALL, level, needSound)
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

    fun isVolumeEnabled(): Boolean {
//        val zenMode = Settings.Global.getInt(context.contentResolver, ZEN_MODE, ZEN_MODE_OFF)
        Timber.e("$TAG ringerMode = ${mManager.ringerMode}")
        return mManager.ringerMode == AudioManager.RINGER_MODE_NORMAL
    }

    fun setVolumeEnabled(enable: Boolean) {
        try {
            Timber.e("$TAG setVolumeEnabled = $enable")
            if (isVolumeEnabled() != enable) {
//                Settings.Global.putInt(context.contentResolver, ZEN_MODE, if (enable) ZEN_MODE_OFF else ZEN_MODE_ALARMS)
                mManager.ringerMode = if (enable) AudioManager.RINGER_MODE_NORMAL else AudioManager.RINGER_MODE_VIBRATE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {

        const val TAG = "VolumeManager"

        const val MAX_LEVEL_DEFAULT = 10

        /**
         * 勿扰模式
         */
        const val ZEN_MODE = "zen_mode"
        /**
         * 勿扰模式关
         */
        const val ZEN_MODE_OFF = 0
        /**
         * 勿扰模式：仅限重要打扰
         */
        const val ZEN_MODE_IMPORTANT_INTERRUPTIONS = 1
        /**
         * 勿扰模式：完全静音
         */
        const val ZEN_MODE_NO_INTERRUPTIONS = 2
        /**
         * 勿扰模式：仅限闹钟
         */
        const val ZEN_MODE_ALARMS = 3
    }
}
