package com.zzx.utils.system

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.provider.Settings.*

/**@author Tomy
 * Created by Tomy on 2016/3/16.
 */
object SettingsUtils {
    fun getSystemString(context: Context, key: String): String {
        return System.getString(context.contentResolver, key)
    }

    fun getSystemInt(context: Context, key: String, defaultValue: Int = -1): Int {
        return System.getInt(context.contentResolver, key, defaultValue)
    }

    fun getSystemFloat(context: Context, key: String, defaultValue: Float = -1f): Float {
        return System.getFloat(context.contentResolver, key, defaultValue)
    }

    fun putSystemValue(context: Context, key: String, value: Any) {
        try {
            val resolver = context.contentResolver
            when (value) {
                is Int -> System.putInt(resolver, key, value)
                is String -> System.putString(resolver, key, value)
                is Float -> System.putFloat(resolver, key, value)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getGlobalString(context: Context, key: String): String {
        return Global.getString(context.contentResolver, key)
    }

    fun getGlobalInt(context: Context, key: String, defaultValue: Int = -1): Int {
        return Global.getInt(context.contentResolver, key, defaultValue)
    }

    fun getGlobalFloat(context: Context, key: String, defaultValue: Float = -1f): Float {
        return Global.getFloat(context.contentResolver, key, defaultValue)
    }

    fun putGlobalValue(context: Context, key: String, value: Any) {
        try {
            val resolver = context.contentResolver
            when (value) {
                is Int -> Global.putInt(resolver, key, value)
                is String -> Global.putString(resolver, key, value)
                is Float -> Global.putFloat(resolver, key, value)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun getSecureString(context: Context, key: String): String {
        return Secure.getString(context.contentResolver, key)
    }

    fun getSecureInt(context: Context, key: String, defaultValue: Int = -1): Int {
        return Secure.getInt(context.contentResolver, key, defaultValue)
    }

    fun getSecureFloat(context: Context, key: String, defaultValue: Float = -1f): Float {
        return Secure.getFloat(context.contentResolver, key, defaultValue)
    }

    fun putSecureValue(context: Context, key: String, value: Any) {
        try {
            val resolver = context.contentResolver
            when (value) {
                is Int -> Secure.putInt(resolver, key, value)
                is String -> Secure.putString(resolver, key, value)
                is Float -> Secure.putFloat(resolver, key, value)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isAirplaneOn(context: Context): Boolean {
        return getGlobalInt(context, Global.AIRPLANE_MODE_ON, 0) == 1
    }

    fun setAirplaneState(context: Context, enable: Boolean) {
        putGlobalValue(context, Global.AIRPLANE_MODE_ON, if (enable) 1 else 0)
        Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED).apply {
            flags = Intent.FLAG_RECEIVER_REPLACE_PENDING
            putExtra("state", enable)
            context.sendBroadcast(this)
        }
    }


    fun setLocationState(context: Context, enable: Boolean) {
        putSecureValue(context, Secure.LOCATION_MODE, if (enable) Secure.LOCATION_MODE_HIGH_ACCURACY else Secure.LOCATION_MODE_OFF)
    }

    fun isLocationOn(context: Context): Boolean {
        return getSecureInt(context, Secure.LOCATION_MODE, Secure.LOCATION_MODE_OFF) != Secure.LOCATION_MODE_OFF
    }


    fun setTimeOut(context: Context, timeInMill: Long) {
        putSystemValue(context, System.SCREEN_OFF_TIMEOUT, timeInMill)
    }


    fun setTimeAutoSet(context: Context, enable: Boolean) {
        putGlobalValue(context, Global.AUTO_TIME, if (enable) 1 else 0)
    }
}
