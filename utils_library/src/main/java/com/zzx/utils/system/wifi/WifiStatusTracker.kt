package com.zzx.utils.system.wifi

import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager

/**@author Tomy
 * Created by Tomy on 2018/11/22.
 */
class WifiStatusTracker(var mWifiManager: WifiManager) {

    var state       = 0
    var ssid: String? = null
    var rssi        = 0
    var level       = 0
    var enabled     = false
    var connected   = false
    var connecting  = false

    fun handleBroadcast(intent: Intent) {

        when (intent.action) {
            WifiManager.WIFI_STATE_CHANGED_ACTION -> {
                state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)
                enabled = state == WifiManager.WIFI_STATE_ENABLED
            }

            WifiManager.NETWORK_STATE_CHANGED_ACTION -> {
                val networkInfo = intent.getParcelableExtra<NetworkInfo>(WifiManager.EXTRA_NETWORK_INFO)
                connecting  = networkInfo?.isConnected != true && networkInfo?.isConnectedOrConnecting == true
                connected   = networkInfo?.isConnected == true
                ssid = if (connected) {
                    var wifiInfo = intent.getParcelableExtra<WifiInfo>(WifiManager.EXTRA_WIFI_INFO)
                    if (wifiInfo == null) {
                        wifiInfo = mWifiManager.connectionInfo
                    }
                   getSsid(wifiInfo)
                } else {
                   null
                }
            }

            WifiManager.RSSI_CHANGED_ACTION -> {
                rssi    = intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI, -200)
                level   = WifiManager.calculateSignalLevel(rssi, 5)
            }
        }
    }

    private fun getSsid(wifiInfo: WifiInfo?): String? {
        return if (wifiInfo == null) {
            null
        } else {
            val ssid = wifiInfo.ssid
            if (ssid != null) {
                return ssid
            } else {
                val networks = mWifiManager.configuredNetworks
                networks.forEach {
                    if (it.networkId == wifiInfo.networkId) {
                        return it.SSID
                    }
                }
                return null
            }
        }
    }

}