package com.zzx.utils.zzx

import java.text.SimpleDateFormat
import java.util.*

/**@author Tomy
 * Created by Tomy on 2018/12/14.
 */
object SystemInfo {

    const val CLASS_SYSTEM_PROPERTIES = "android.os.SystemProperties"
    const val METHOD_GET = "get"
    const val METHOD_SET = "set"
    const val VERSION_CODE = "zzx.software.version"
    const val VERSION_CODE_HARDWARE = "ro.build.version.hardware"
    const val VERSION_CODE_SOFTWARE = "ro.build.version.software"
    const val MODEL_CODE = "zzx.product.model"
    const val BUILD_TIME = "ro.build.version.incremental"

    private val mTimeFormat by lazy {
        SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    }

    private val mClass by lazy {
        Class.forName(CLASS_SYSTEM_PROPERTIES)
    }

    private val mGetMethod by lazy {
        mClass.getDeclaredMethod(METHOD_GET, String::class.java)
    }

    fun getSystemInfo(): String {
        return "${getDeviceModel()}-V${getVersionCode()}-${getBuildTime()}"
//        return "${getDeviceModel()}-V${getVersionCode()}-20200624"
    }

    fun getDeviceModel(): String {
        val fullModel = getSystemProperty(MODEL_CODE)
         return if (fullModel.contains('_')) {
            fullModel.split('_')[0]
        } else {
             fullModel
         }
    }

    fun getBuildTime(): String {
        val time = getSystemProperty(BUILD_TIME).toLong() * 1000
        return mTimeFormat.format(time)
    }

    fun getVersionCode(): String {
        return getSystemProperty(VERSION_CODE)
    }

    fun getHardwareVersion(): String {
        return getSystemProperty(VERSION_CODE_HARDWARE)
    }

    fun getSystemProperty(key: String): String {
        val value = mGetMethod.invoke(mClass, key)
        return if (value == null) {
            ""
        } else {
            value as String
        }
    }


}