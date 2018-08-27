package com.zzx.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;

/**@author Tomy
 * Created by Tomy on 2016/3/16.
 */
public class SettingsUtils {
    public static String getString(Context context, String key) {
        return Settings.System.getString(context.getContentResolver(), key);
    }

    public static int getString(Context context, String key, int defaultValue) {
        return Settings.System.getInt(context.getContentResolver(), key, defaultValue);
    }

    public static float getString(Context context, String key, float defaultValue) {
        return Settings.System.getFloat(context.getContentResolver(), key, defaultValue);
    }

    public static void putValue(Context context, String key, Object value) {
        ContentResolver resolver = context.getContentResolver();
        if (value instanceof Integer) {
            Settings.System.putInt(resolver, key, (Integer) value);
        } else if (value instanceof String) {
            Settings.System.putString(resolver, key, (String) value);
        } else if (value instanceof Float) {
            Settings.System.putFloat(resolver, key, (Float) value);
        }
    }
}
