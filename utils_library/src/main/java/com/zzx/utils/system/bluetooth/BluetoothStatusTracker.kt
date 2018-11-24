package com.zzx.utils.system.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.WifiManager

/**@author Tomy
 * Created by Tomy on 2018/11/23.
 */
class BluetoothStatusTracker {

    var state       = 0
    var enabled     = false
    var connected   = false
    var connecting  = false
    var deviceName  = ""

    fun handleBroadcast(intent: Intent) {

        when (intent.action) {
            BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED -> {
                state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.STATE_DISCONNECTED)
                connecting  = state == BluetoothAdapter.STATE_CONNECTING
                connected   = state == BluetoothAdapter.STATE_CONNECTED
                if (connected) {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    deviceName = device.name ?: device.address
                }
            }

            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)
                enabled = state == BluetoothAdapter.STATE_ON
            }


        }
    }


}