package com.zzx.utils.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**@author Tomy
 * Created by Tomy on 2019/9/11.
 */
class BaseBroadcastReceiver(private var mContext: Context?): BroadcastReceiver() {

    private val mIntentFilter by lazy {
        IntentFilter()
    }

    private var mReceiver: BroadcastReceiver? = null

    private val mRegister = AtomicBoolean(false)

    fun addAction(action: String) {
        if (mRegister.get()) {
            return
        }
        mIntentFilter.addAction(action)
        mContext?.registerReceiver(this, mIntentFilter)
        mRegister.set(true)
    }

    fun addActionList(actionList: List<String>) {
        if (mRegister.get()) {
            return
        }
        for (action in actionList) {
            mIntentFilter.addAction(action)
        }
        mContext?.registerReceiver(this, mIntentFilter)
        mRegister.set(true)
    }

    fun addActionArray(actionArray: Array<String>) {
        if (mRegister.get()) {
            return
        }
        for (action in actionArray) {
            mIntentFilter.addAction(action)
        }
        mContext?.registerReceiver(this, mIntentFilter)
        mRegister.set(true)
    }

    fun setReceiver(receiver: BroadcastReceiver) {
        mReceiver = receiver
    }

    fun unregisterReceiver() {
        if (!mRegister.get()) {
            return
        }
        mContext?.unregisterReceiver(this)
        mRegister.set(false)
    }

    override fun onReceive(context: Context, intent: Intent) {
        mReceiver?.onReceive(context, intent)
    }
}