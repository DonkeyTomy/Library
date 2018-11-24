package com.zzx.utils.system.wifi

import android.net.ConnectivityManager
import java.lang.Exception

/**@author Tomy
 * Created by Tomy on 2018/11/21.
 */
class ConnectivityManagerWrapper(var mConnectivityManager: ConnectivityManager) {

    fun stopTethering(type: Int) {
        try {
            val cls = ConnectivityManager::class.java
            val method = cls.getDeclaredMethod("stopTethering", Integer.TYPE)
            method.invoke(mConnectivityManager, type)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        const val TETHERING_WIFI = 0
    }

}