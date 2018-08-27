package com.zzx.utils.context

import android.content.Context
import android.content.Intent

/**@author Tomy
 * Created by Tomy on 2018/7/10.
 */
object ContextUtil {

    fun startOtherActivity(context: Context, pkgName: String, clsName: String): Boolean {
        return try {
            val intent = Intent().apply {
                setClassName(pkgName, clsName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}