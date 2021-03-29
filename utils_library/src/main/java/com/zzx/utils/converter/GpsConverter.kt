package com.zzx.utils.converter

import timber.log.Timber
import kotlin.math.min

/**@author Tomy
 * Created by Tomy on 2014/7/8.
 */
object GpsConverter {
    fun convertToDegree(gpsData: Double): Double {
        try {
            val temp = gpsData.toString()
            val temp1 = temp.split(".").toTypedArray()
            val limit = temp1[0]
            val fraction = ("0." + temp1[1]).toDouble()
            val fractionDes = fraction * 60
            val temp2 = fractionDes.toString()
            val temp3 = temp2.split(".").toTypedArray()
            /*val secTmp = temp3[1].substring(0, min(temp3[1].length, 5)).toDouble()
            val sec = secTmp * 60*/
            var min = temp3[0]
            if (min.length == 1) {
                min = "0$min"
            }
            val dest = "$limit$min.${temp3[1].substring(0, min(temp3[1].length, 4))}"
            return dest.toDouble()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0000.0000
    }

    fun getGpsString(temp: String): String {
        try {
            val temp1 = temp.split(".").toTypedArray()
            if (temp1.size < 2) {
                Timber.w("format [$temp] failed.size = ${temp1.size}")
                return "0"
            }
            val limit = temp1[0] //获得度数
            val fraction = ("0." + temp1[1]).toDouble() //将分加个"0."转换为double
            val fractionDes = fraction * 60 //分*60
            val temp2 = fractionDes.toString() //转换为字符串
            val temp3 = temp2.split(".").toTypedArray() //分出分秒,temp3[0]即分数
            val secTmp = temp3[1].substring(0, min(temp3[1].length, 4)).toDouble() //秒数取4位
            var sec = (secTmp * 60).toString() //秒数*60
            if (sec.contains(".")) //若*60后变成了*.*E7等,则去除.
                sec = sec.replace(".", "")
            return "$limit°${temp3[0]}′${sec.substring(0, min(sec.length, 4))}″"
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "0"
    }

    fun getGpsString(data: Double): String {
        return getGpsString(data.toString())
    }
}