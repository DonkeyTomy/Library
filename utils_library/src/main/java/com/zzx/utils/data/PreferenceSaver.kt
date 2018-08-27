package com.zzx.utils.data

import android.content.Context

/**@author Tomy
 * Created by Tomy on 2018/6/17.
 */
class PreferenceSaver(private var context: Context, name: String = context.packageName): IDataSaver<String> {

    private var mPreference = context.getSharedPreferences(name, Context.MODE_PRIVATE)
    private var mEdit = mPreference.edit()

    override fun saveInt(key: String, value: Int) {
        mEdit.putInt(key, value).apply()
    }

    override fun saveString(key: String, value: String) {
        mEdit.putString(key, value).apply()
    }

    override fun saveFloat(key: String, value: Float) {
        mEdit.putFloat(key, value).apply()
    }

    override fun saveBoolean(key: String, value: Boolean) {
        mEdit.putBoolean(key, value).apply()
    }

    override fun saveLong(key: String, value: Long) {
        mEdit.putLong(key, value).apply()
    }

    override fun getInt(key: String, defValue: Int): Int {
        return mPreference.getInt(key, defValue)
    }

    override fun getString(key: String, defValue: String): String {
        return mPreference.getString(key, defValue)
    }

    override fun getLong(key: String, defValue: Long): Long {
        return mPreference.getLong(key, defValue)
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return mPreference.getFloat(key, defValue)
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return mPreference.getBoolean(key, defValue)
    }

    override fun clear() {
        mEdit.clear()
    }

}