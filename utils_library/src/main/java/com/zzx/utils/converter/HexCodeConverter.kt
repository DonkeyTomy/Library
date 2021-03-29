package com.zzx.utils.converter

import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.util.*
import java.util.regex.Pattern
import kotlin.experimental.and

/**@author Tomy
 * Created by Tomy on 2014/10/29.
 */
object HexCodeConverter {
    /** Convert byte[] to hex string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。
     * @param src byte[] data
     * @return hex string
     */
    fun bytesToHexString(src: ByteArray?, len: Int = src?.size ?: 0): String? {
        val stringBuilder = StringBuilder("")
        if (src == null || src.isEmpty()) {
            return null
        }
        /*if (src.size == len) {
            for (aSrc in src) {
                val v: Int = aSrc.and(0xFF.toByte()).toInt()
                val hv = Integer.toHexString(v)
//            stringBuilder.append("0x")
                if (hv.length < 2) {
                    stringBuilder.append(0)
                }
                stringBuilder.append("${hv.toUpperCase(Locale.ROOT)} ")
            }
        }*/
        for (i in 0 until len) {
            val v: Int = src[i].and(0xFF.toByte()).toInt()
            var hv = Integer.toHexString(v)
//            stringBuilder.append("0x")
//            Log.d("SerialPortTool", "hv = $hv")
            if (hv.length < 2) {
                stringBuilder.append(0)
            } else if (hv.length > 2) {
                hv = hv.substring(hv.length - 2)
            }
            stringBuilder.append("${hv.toUpperCase(Locale.ROOT)} ")
        }
        return stringBuilder.toString()
    }

    fun byteToHexString(byte: Byte): String {
        val stringBuilder = StringBuilder("")
        val v: Int = byte.and(0xFF.toByte()).toInt()
        var hv = Integer.toHexString(v)
        if (hv.length < 2) {
            stringBuilder.append(0)
        } else if (hv.length > 2) {
            hv = hv.substring(hv.length - 2)
        }
        stringBuilder.append("${hv.toUpperCase(Locale.ROOT)} ")
        return stringBuilder.toString()
    }

    fun intToHexString(src: Int): String? {
        val byteArray = ByteArray(Int.SIZE_BYTES)
        for (index in byteArray.indices) {
            byteArray[index] = src.shr(((Int.SIZE_BYTES - 1) - index) * 8).toByte()
        }
        return bytesToHexString(byteArray)
    }

    fun stringToHexString(src: String?): String {
        val stringBuilder = StringBuilder("")
        src?.forEach {
            val v: Int = it.toInt().and(0xFF)
            val hv = Integer.toHexString(v)
            if (hv.length < 2) {
                stringBuilder.append(0)
            }
            stringBuilder.append(hv.toUpperCase(Locale.ROOT))
        }
        return stringBuilder.toString()
    }

    /*

 *  把十六进制Unicode编码字符串转换为中文字符串

 */
    fun unicodeToString(str: String): String {
        var str = str
        val pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))")
        val matcher = pattern.matcher(str)
        var ch: Char
        while (matcher.find()) {
            ch = matcher.group(2).toInt(16).toChar()
            str = str.replace(matcher.group(1), ch.toString() + "")
        }
        return str
    }

    /**
     * 将16进制数字解码成字符串,适用于所有字符（包括中文）
     */
    fun decode(bytes: String): String {
        val baos = ByteArrayOutputStream(bytes.length / 2)
        //将每2位16进制整数组装成一个字节
        var i = 0
        while (i < bytes.length) {
            baos.write(hexString.indexOf(bytes[i]) shl 4 or hexString.indexOf(bytes[i + 1]))
            i += 2
        }
        var bb = ""
        try {
            bb = String(baos.toByteArray(), Charset.forName("GB2312"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bb
    }

    /**
     * Convert hex string to byte[]
     * @param hexString the hex string
     * @return byte[]
     */
    fun hexStringToBytes(hexString: String?): ByteArray? {
        var hexString = hexString
        if (hexString == null || hexString == "") {
            return null
        }
        hexString = hexString.toUpperCase(Locale.ROOT)
        val length = hexString.length / 2
        val hexChars = hexString.toCharArray()
        val d = ByteArray(length)
        for (i in 0 until length) {
            val pos = i * 2
//            d[i] = (charToByte(hexChars[pos]) shl 4 or charToByte(hexChars[pos + 1])) as Byte
        }
        return d
    }

    private const val hexString = "0123456789ABCDEF"

    /**
     * Convert char to byte
     * @param c char
     * @return byte
     */
    private fun charToByte(c: Char): Byte {
        return "0123456789ABCDEF".indexOf(c).toByte()
    }

    fun longToByte(number: Long): ByteArray {
        var temp = number
        val b = ByteArray(8)
        for (i in b.indices.reversed()) {
            b[i] = java.lang.Long.valueOf(temp and 0xff).toByte() // 将最低位保存在最高位
            temp = temp shr 8 // 向右移8位
        }
        return b
    }

    fun intToByte(number: Int): ByteArray {
        var temp = number
        val b = ByteArray(4)
        for (i in b.indices.reversed()) {
            b[i] = Integer.valueOf(temp and 0xff).toByte() // 将最低位保存在最高位
            temp = temp shr 8 // 向右移8位
        }
        return b
    }

    fun byteToLong(value: ByteArray): Long {
        var temp: Long = 0
        for (i in value.indices) {
            temp = temp or value[i].toLong() shl 8
        }
        return temp
    }

    fun parseUnicode2String(unicodeStr: String): String {
        var unicodeStr = unicodeStr
        val regex = "&#(\\d+);"
        val p = Pattern.compile(regex)
        val ma = p.matcher(unicodeStr)
        while (ma.find()) {
            val src = ma.group()
            val tmp = ma.group(1)
            val value = tmp.toInt()
            val dst = String(Character.toChars(value))
            unicodeStr = unicodeStr.replace(src, dst)
        }
        return unicodeStr
    }

    fun parseUnicodeX2String(unicodeStr: String): String {
        var unicodeStr = unicodeStr
        val regex = "&#x(\\d+);"
        val p = Pattern.compile(regex)
        val ma = p.matcher(unicodeStr)
        while (ma.find()) {
            val src = ma.group()
            val tmp = ma.group(1)
            val value = tmp.toInt()
            val dst = String(Character.toChars(value))
            unicodeStr = unicodeStr.replace(src, dst)
        }
        return unicodeStr
    }
}