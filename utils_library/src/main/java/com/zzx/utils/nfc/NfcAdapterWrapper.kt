package com.zzx.utils.nfc

import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.NfcManager

/**@author Tomy
 * Created by Tomy on 2018/12/26.
 */
class NfcAdapterWrapper(context: Context) {

    private val mNfcAdapter by lazy {
        context.getSystemService(NfcManager::class.java).defaultAdapter
    }

    private val mEnableMethod by lazy {
        NfcAdapter::class.java.getDeclaredMethod("enable")
    }

    private val mDisableMethod by lazy {
        NfcAdapter::class.java.getDeclaredMethod("disable")
    }

    fun isEnabled(): Boolean {
        return mNfcAdapter.isEnabled
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