package com.zzx.utils.telephony

import android.content.Context
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager

class TelephonyManagerWrapper(context: Context) {

    val mTelephonyManager = context.getSystemService(TelephonyManager::class.java) as TelephonyManager

    private val mSetDataEnableMethod by lazy {
        TelephonyManager::class.java.getDeclaredMethod("setDataEnabled", Integer.TYPE, Boolean::class.java)
    }

    private val mIsDataEnabledMethod by lazy {
        TelephonyManager::class.java.getDeclaredMethod("getDataEnabled", Integer.TYPE)
    }

    private val mIsMultiSimEnabled by lazy {
        TelephonyManager::class.java.getDeclaredMethod("isMultiSimEnabled")
    }

    fun setDataEnabled(subId: Int = SubscriptionManager.getDefaultDataSubscriptionId(), enable: Boolean) {
        try {
            mSetDataEnableMethod.invoke(mTelephonyManager, subId, enable)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isDataEnabled(subId: Int = SubscriptionManager.getDefaultDataSubscriptionId()): Boolean {
        return try {
            mIsDataEnabledMethod.invoke(mTelephonyManager, subId) as Boolean
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun isMultiSimEnabled(): Boolean {
        return try {
            mIsMultiSimEnabled.invoke(mTelephonyManager) as  Boolean
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}