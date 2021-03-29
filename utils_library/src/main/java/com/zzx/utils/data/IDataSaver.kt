package com.zzx.utils.data

/**@author Tomy
 * Created by Tomy on 2018/6/17.
 */
interface IDataSaver<in key> {

    fun saveInt(key: key, value: Int)

    fun saveString(key: key, value: String)

    fun saveFloat(key: key, value: Float)

    fun saveBoolean(key: key, value: Boolean)

    fun saveLong(key: key, value: Long)

    fun getInt(key: key, defValue: Int = -1): Int

    fun getString(key: key, defValue: String = ""): String

    fun getLong(key: key, defValue: Long = -1L): Long

    fun getFloat(key: key, defValue: Float = -1f): Float

    fun getBoolean(key: key, defValue: Boolean = false): Boolean

    fun clear()
}