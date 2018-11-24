package com.tomy.lib.ui.wifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.SupplicantState
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import com.tomy.lib.ui.widget.SwitchWidgetController
import com.zzx.utils.system.wifi.ConnectivityManagerWrapper
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

/**@author Tomy
 * Created by Tomy on 2018/11/21.
 */
class WifiEnabler(var mContext: Context, var mSwitchWidget: SwitchWidgetController): SwitchWidgetController.OnSwitchChangeListener {

    private val mWifiManager: WifiManager

    private val mConnectivityManager: ConnectivityManagerWrapper

    private var mListeningToOnSwitchChange = false

    private var mStateMachineEvent = false

    private val mConnected = AtomicBoolean(false)

    private val mIntentFilter by lazy {
        IntentFilter().apply {
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
            addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)
        }
    }

    init {
        mSwitchWidget.setListener(this)
        mWifiManager = mContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        mConnectivityManager = ConnectivityManagerWrapper(mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
        setupSwitchController()
    }

    fun setupSwitchController() {

        val state = mWifiManager.wifiState
        handleWifiStateChanged(state)
        if (!mListeningToOnSwitchChange) {
            mSwitchWidget.startListening()
            mListeningToOnSwitchChange = true
        }
        mSwitchWidget.setupView()
    }

    fun tearDownSwitchController() {
        if (mListeningToOnSwitchChange) {
            mSwitchWidget.stopListening()
            mListeningToOnSwitchChange = false
        }
        mSwitchWidget.teardownView()
    }

    fun resume() {
        mContext.registerReceiver(mReceiver, mIntentFilter)
        if (!mListeningToOnSwitchChange) {
            mSwitchWidget.startListening()
            mListeningToOnSwitchChange = true
        }
    }

    fun pause() {
        mContext.unregisterReceiver(mReceiver)
        if (mListeningToOnSwitchChange) {
            mSwitchWidget.stopListening()
            mListeningToOnSwitchChange = false
        }
    }

    /**
     * 处理WiFi状态变化.
     * @param state Int当前的WiFi状态
     */
    private fun handleWifiStateChanged(state: Int) {
        when (state) {
            WifiManager.WIFI_STATE_ENABLING -> {}
            WifiManager.WIFI_STATE_ENABLED  -> {
                setSwitchBarChecked(true)
                mSwitchWidget.setEnabled(true)
            }
            WifiManager.WIFI_STATE_DISABLING-> {}
            WifiManager.WIFI_STATE_DISABLED -> {
                setSwitchBarChecked(false)
                mSwitchWidget.setEnabled(false)
            }
            else -> {
                setSwitchBarChecked(false)
                mSwitchWidget.setEnabled(true)
            }
        }

    }

    private fun setSwitchBarChecked(checked: Boolean) {
        mStateMachineEvent = true
        mSwitchWidget.setChecked(checked)
        mStateMachineEvent = false
    }

    fun handleStateChanged(state: NetworkInfo.DetailedState) {
    }

    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                WifiManager.WIFI_STATE_CHANGED_ACTION -> {
                    handleWifiStateChanged(mWifiManager.wifiState)
                }

                WifiManager.SUPPLICANT_STATE_CHANGED_ACTION -> {
                    if (!mConnected.get()) {
                        handleStateChanged(WifiInfo.getDetailedStateOf(intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE) as SupplicantState))
                    }
                }

                WifiManager.NETWORK_STATE_CHANGED_ACTION -> {
                    val info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO) as NetworkInfo
                    mConnected.set(info.isConnected)
                    handleStateChanged(info.detailedState)
                }

            }
        }
    }

    override fun onSwitchToggled(isChecked: Boolean): Boolean {
        Timber.e("onSwitchToggled.isChecked = $isChecked; mStateMachineEvent = $mStateMachineEvent")
        //Do nothing if called as a result of a state machine event.
        if (mStateMachineEvent) {
            return true
        }

        if (mayDisableTethering(isChecked)) {
            mConnectivityManager.stopTethering(ConnectivityManagerWrapper.TETHERING_WIFI)
        }

        if (!mWifiManager.setWifiEnabled(isChecked)) {
            mSwitchWidget.setEnabled(true)
        }
        return false
    }

    private fun mayDisableTethering(isChecked: Boolean): Boolean {
        val state = mWifiManager.wifiState
        return isChecked && ((state == WIFI_AP_STATE_ENABLED) || (state == WIFI_AP_STATE_ENABLING))
    }

    companion object {
        const val WIFI_AP_STATE_DISABLING = 10
        const val WIFI_AP_STATE_DISABLED = 11
        const val WIFI_AP_STATE_ENABLING = 12
        const val WIFI_AP_STATE_ENABLED = 13
    }
}