package com.zzx.utils

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import timber.log.Timber

/**@author Tomy
 * Created by Tomy on 2018/6/20.
 */
class StorageListener(var mContext: Context) {

    private val mReceiver = StorageBroadcast()

    private var mCallback: StorageCallback? = null

    init {
        val intentFilter = IntentFilter(Intent.ACTION_MEDIA_MOUNTED).apply {
            addAction(Intent.ACTION_MEDIA_UNMOUNTED)
            addAction(Intent.ACTION_MEDIA_BAD_REMOVAL)
            addDataScheme("file")
        }
        mContext.registerReceiver(mReceiver, intentFilter)
    }

    fun setStorageCallback(callback: StorageCallback) {
        mCallback = callback
    }

    fun release() {
        mContext.unregisterReceiver(mReceiver)
    }

    inner class StorageBroadcast: BroadcastReceiver() {

        @SuppressLint("LogNotTimber")
        override fun onReceive(context: Context?, intent: Intent?) {
            Timber.e("${CommonConst.TAG_RECORD_FLOW}action = ${intent!!.action}")
            when(intent.action) {
                Intent.ACTION_MEDIA_EJECT,
                Intent.ACTION_MEDIA_UNMOUNTED,
                Intent.ACTION_MEDIA_BAD_REMOVAL -> {
                    mCallback?.onExternalStorageChanged(false)
                }
                Intent.ACTION_MEDIA_MOUNTED -> {
                    mCallback?.onExternalStorageChanged(true)
                    Log.e(this@StorageListener.javaClass.simpleName, "${intent.action}. file = ${intent.data}")
                }
            }
        }
    }

    interface StorageCallback {
        fun onExternalStorageChanged(mount: Boolean)
    }

}