package com.tomy.lib.ui.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.SupplicantState
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import com.tomy.lib.ui.widget.SwitchWidgetController
import com.zzx.utils.system.wifi.ConnectivityManagerWrapper
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

/**@author Tomy
 * Created by Tomy on 2018/11/23.
 */
class BluetoothEnabler(var mContext: Context, var mSwitchWidget: SwitchWidgetController): SwitchWidgetController.OnSwitchChangeListener {

    private val mBluetoothManager: BluetoothManager

    private var mListeningToOnSwitchChange = false

    private var mStateMachineEvent = false

    private val mConnected = AtomicBoolean(false)

    private val mIntentFilter by lazy {
        IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        }
    }

    init {
        mSwitchWidget.setListener(this)
        mBluetoothManager = mContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        setupSwitchController()
    }

    fun setupSwitchController() {

        val state = mBluetoothManager.adapter.state
        handleBluetoothStateChanged(state)
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
    private fun handleBluetoothStateChanged(state: Int) {
        when (state) {
            BluetoothAdapter.STATE_TURNING_ON -> {}
            BluetoothAdapter.STATE_ON  -> {
                setSwitchBarChecked(true)
                mSwitchWidget.setEnabled(true)
            }
            BluetoothAdapter.STATE_TURNING_OFF-> {}
            BluetoothAdapter.STATE_OFF -> {
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

    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    handleBluetoothStateChanged(mBluetoothManager.adapter.state)
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

        if (isChecked) {
            mBluetoothManager.adapter.enable()
            mSwitchWidget.setEnabled(true)
        } else {
            mBluetoothManager.adapter.disable()
        }
        return false
    }


}