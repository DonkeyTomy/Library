package com.zzx.utils.network

import android.content.Context
import android.net.ConnectivityManager

object NetworkUtil {

    /**
     * 返回网络状态
     * @param context
     * @return
     */
    fun isNetworkConnected(context: Context?): Boolean {
        if (context != null) {
            val mConnectivityManager = context
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val mNetworkInfo = mConnectivityManager.activeNetworkInfo
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable && mNetworkInfo.isConnected
            }
        }
        return false
    }

    /**
     * 返回当前Wifi是否连接上
     * @param context
     * @return true 已连接
     */
    fun isWifiConnected(context: Context): Boolean {
        val conMan = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = conMan.activeNetworkInfo
        return netInfo != null && netInfo.type == ConnectivityManager.TYPE_WIFI && netInfo.isConnected
    }

    fun getConnectManager(context: Context): ConnectivityManager {
        return context.getSystemService(ConnectivityManager::class.java)
    }

    fun setWifiConnected(context: Context) {
        val connectivityManager = getConnectManager(context)
    }

}
