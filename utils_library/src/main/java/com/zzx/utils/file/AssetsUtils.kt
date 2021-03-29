package com.zzx.utils.file

import android.content.Context
import java.io.InputStream
import java.nio.charset.Charset

/**@author Tomy
 * Created by Tomy on 2019/11/3.
 */
object AssetsUtils {

    fun read(context: Context, dir: String, fileName: String): String? {
        var result: String? = null
        var input: InputStream? = null
        try {
            input = context.resources.assets.open("$dir/$fileName")
            val buffer = ByteArray(input.available())
            input.read(buffer)
            result = String(buffer, Charset.forName("utf-8"))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                input?.close()
            } catch (e: Exception) {
            }
        }
        return result
    }

}