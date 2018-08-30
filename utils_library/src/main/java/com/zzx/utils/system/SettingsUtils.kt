package com.zzx.utils.system

import android.content.Context
import android.provider.Settings

/**@author Tomy
 * Created by Tomy on 2016/3/16.
 */
object SettingsUtils {
    fun getString(context: Context, key: String): String {
        return Settings.System.getString(context.contentResolver, key)
    }

    fun getInt(context: Context, key: String, defaultValue: Int = 1): Int {
        return Settings.System.getInt(context.contentResolver, key, defaultValue)
    }

    fun getFloat(context: Context, key: String, defaultValue: Float = -1f): Float {
        return Settings.System.getFloat(context.contentResolver, key, defaultValue)
    }

    fun putValue(context: Context, key: String, value: Any) {
        val resolver = context.contentResolver
        when (value) {
            is Int -> Settings.System.putInt(resolver, key, value)
            is String -> Settings.System.putString(resolver, key, value)
            is Float -> Settings.System.putFloat(resolver, key, value)
        }
    }
}
