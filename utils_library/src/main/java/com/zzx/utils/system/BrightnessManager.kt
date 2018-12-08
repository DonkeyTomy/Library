package com.zzx.utils.system

import android.content.ContentResolver
import android.content.Context
import android.provider.Settings.System
import timber.log.Timber

/**@author Tomy
 * Created by Tomy on 2014-12-31.
 */
class BrightnessManager(context: Context, maxLevel: Int = MAX_LEVEL_DEFAULT) {

    private var mMaxLevel = 0
    private val mResolver: ContentResolver = context.contentResolver

    init {
        setMaxLevel(maxLevel)
    }

    var brightness: Int
        get() {
            try {
                return System.getInt(mResolver, System.SCREEN_BRIGHTNESS)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return 0
        }
        set(brightness) {
            try {
                setBrightnessMode(System.SCREEN_BRIGHTNESS_MODE_MANUAL)
                Timber.e("setBrightness = $brightness")
                System.putInt(mResolver, System.SCREEN_BRIGHTNESS, brightness)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    fun getBrightnessLevel(): Int {
        val brightness = brightness
        return if (mMaxLevel == 0) brightness else brightness / (255 / mMaxLevel)
    }

    fun setBrightnessLevel(level: Int) {
        val brightnessValue: Int = when {
            mMaxLevel == 0 -> level
            mMaxLevel <= level -> 255
            else -> 255 * level / mMaxLevel
        }
        brightness = brightnessValue
    }

    fun setMaxLevel(maxLevel: Int) {
        mMaxLevel = maxLevel
    }

    fun increase() {
        var level = getBrightnessLevel()
        if (mMaxLevel != 0 && level >= mMaxLevel) {
            return
        }
        brightness = ++level
    }

    fun decrease() {
        var level = getBrightnessLevel()
        if (level > 0) {
            brightness = --level
        }
    }

    fun setBrightnessAutoEnabled(enable: Boolean) {
        try {
            System.putInt(mResolver, System.SCREEN_BRIGHTNESS_MODE, if (enable) System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC else System.SCREEN_BRIGHTNESS_MODE_MANUAL)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun isBrightnessAutoEnabled(): Boolean {
        return System.getInt(mResolver, System.SCREEN_BRIGHTNESS_MODE) == System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
    }

    fun setBrightnessMode(mode: Int) {
        try {
            val preMode = System.getInt(mResolver, System.SCREEN_BRIGHTNESS_MODE)
            if (preMode != mode) {
                System.putInt(mResolver, System.SCREEN_BRIGHTNESS_MODE, mode)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    companion object {
        const val MAX_LEVEL_DEFAULT = 10
    }
}
