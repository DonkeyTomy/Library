package com.zzx.utils.nfc

import android.annotation.SuppressLint
import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.os.Build

/**@author Tomy
 * Created by Tomy on 2018/12/26.
 */
@SuppressLint("PrivateApi")
class NfcAdapterWrapper(context: Context) {

    private val mNfcAdapter by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.getSystemService(NfcManager::class.java)?.defaultAdapter
        } else {
            (context.getSystemService(NfcManager::class.java.name) as NfcManager).defaultAdapter
        }
    }

    private val mEnableMethod by lazy {
        NfcAdapter::class.java.getDeclaredMethod("enable")
    }

    private val mDisableMethod by lazy {
        NfcAdapter::class.java.getDeclaredMethod("disable")
    }

    fun isEnabled(): Boolean {
        return mNfcAdapter?.isEnabled ?: false
    }

    fun enable(): Boolean {
        try {
            return mEnableMethod.invoke(mNfcAdapter) as Boolean
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun disable(): Boolean {
        try {
            return mDisableMethod.invoke(mNfcAdapter) as Boolean
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun controlEnable(enable: Boolean): Boolean {
        return if (enable) {
            enable()
        } else {
            disable()

        }
    }

}