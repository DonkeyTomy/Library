package com.zzx.utils.system

import android.content.Context
import android.content.Intent
import android.provider.Settings

/**@author Tomy
 * Created by Tomy on 2018/6/23.
 */
object PermissionChecker {

    fun checkSystemAlertDialog(context: Context): Boolean {
        if (!Settings.canDrawOverlays(context)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            return false
        }
        return true
    }

}