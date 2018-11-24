package com.tomy.lib.ui.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.tomy.lib.ui.R
import com.tomy.lib.ui.widget.SummaryUpdater
import com.zzx.utils.system.bluetooth.BluetoothStatusTracker

/**@author Tomy
 * Created by Tomy on 2018/11/23.
 */
class BluetoothSummaryUpdater(context: Context, listener: OnSummaryChangeListener): SummaryUpdater(context, listener) {

    private val mBluetoothStatusTracker = BluetoothStatusTracker()

    private val mIntentFilter by lazy {
        IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED).apply {
            addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        }
    }

    private val mReceiver =
            object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent) {
                    mBluetoothStatusTracker.handleBroadcast(intent)
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
        if (!mBluetoothStatusTracker.enabled) {
            return mContext.getString(R.string.switch_off_text)
        }
        if (!mBluetoothStatusTracker.connected) {
            return mContext.getString(R.string.disconnected)
        }
        return mBluetoothStatusTracker.deviceName
    }

}