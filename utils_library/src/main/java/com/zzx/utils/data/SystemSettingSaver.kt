package com.zzx.utils.data

import android.content.Context
import android.provider.Settings.System

class SystemSettingSaver(context: Context): IDataSaver<String> {

    private val mContentResolver = context.contentResolver

    override fun saveInt(key: String, value: Int) {
        System.putInt(mContentResolver, key, value)
    }

    override fun saveString(key: String, value: String) {
        System.putString(mContentResolver, key, value)
    }

    override fun saveFloat(key: String, value: Float) {
        System.putFloat(mContentResolver, key, value)
    }

    override fun saveBoolean(key: String, value: Boolean) {
        System.putInt(mContentResolver, key, if (value) 1 else -1)
    }

    override fun saveLong(key: String, value: Long) {
        System.putLong(mContentResolver, key, value)
    }

    override fun getInt(key: String, defValue: Int): Int {
        return System.getInt(mContentResolver, key, defValue)
    }

    override fun getString(key: String, defValue: String): String {
        return System.getString(mContentResolver, key) ?: ""
    }

    override fun getLong(key: String, defValue: Long): Long {
        return System.getLong(mContentResolver, key, defValue)
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return System.getFloat(mContentResolver, key, defValue)
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return System.getInt(mContentResolver, key, if (defValue) 1 else -1) == 1
    }

    override fun clear() {

    }
}