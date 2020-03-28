package com.zzx.utils.context

import android.content.Context
import android.content.Intent
import android.os.Bundle

/**@author Tomy
 * Created by Tomy on 2018/7/10.
 */
object ContextUtil {

    fun startOtherActivity(context: Context, pkgName: String, clsName: String, bundle: Bundle? = null): Boolean {
        return try {
            val intent = Intent().apply {
                setClassName(pkgName, clsName)
                bundle?.let {
                    putExtras(it)
                }
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}