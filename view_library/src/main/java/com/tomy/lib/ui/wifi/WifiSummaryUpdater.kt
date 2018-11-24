package com.tomy.lib.ui.wifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import com.tomy.lib.ui.R
import com.tomy.lib.ui.widget.SummaryUpdater
import com.zzx.utils.system.wifi.WifiStatusTracker

/**@author Tomy
 * Created by Tomy on 2018/11/22.
 * 继承[SummaryUpdater]
 * 1. [register]注册/注销WiFi/网络状态变化的广播
 * 2. [getSummary]返回 关闭Wifi/断开连接/网络的SSID三个状态概要
 * 3. [mReceiver]中监听广播,然后[WifiStatusTracker]保存更新状态.再调用[notifyChangedIfNeeded]刷新Summary.
 */
class WifiSummaryUpdater(context: Context, listener: OnSummaryChangeListener): SummaryUpdater(context, listener) {

    private val mWifiStatusTracker = WifiStatusTracker(context.getSystemService(WifiManager::class.java))

    private val mIntentFilter by lazy {
        IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION).apply {
            addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
            addAction(WifiManager.RSSI_CHANGED_ACTION)
        }
    }

    private val mReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                mWifiStatusTracker.handleBroadcast(intent)
                notifyChangedIfNeeded()
            }

        }


    override fun register(register: Boolean) {
        try {
            if (register) {
                mContext.registerReceiver(mReceiver, mIntentFilter)
            } else {
                mContext.unregisterReceiver(mReceiver)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun getSummary(): String? {
        if (!mWifiStatusTracker.enabled) {
            return mContext.getString(R.string.switch_off_text)
        }
        if (!mWifiStatusTracker.connected) {
            return mContext.getString(R.string.disconnected)
        }
        return removeDoubleQuotes(mWifiStatusTracker.ssid)
    }

    fun removeDoubleQuotes(string: String?): String? {
        if (string == null) return null
        val length = string.length
        return if (length > 1 && string[0] == '"' && string[length - 1] == '"') {
            string.substring(1, length - 1)
        } else string
    }
}