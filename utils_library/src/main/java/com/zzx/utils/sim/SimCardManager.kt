package com.zzx.utils.sim

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.telephony.SubscriptionManager
import timber.log.Timber

@SuppressLint("PrivateApi")
class SimCardManager(var mContext: Context) {

    private val mSubscriptionManager by lazy {
        /*ISub.Stub.asInterface(Class.forName("android.os.ServiceManager").getMethod("getService", String::class.java)
                .invoke(null, "isub") as IBinder)*/
        mContext.getSystemService(SubscriptionManager::class.java)
    }

    private val mSetDefaultDataSubIdMethod by lazy {
        SubscriptionManager::class.java.getDeclaredMethod("setDefaultDataSubId", Integer.TYPE)
    }

    private val mSetDefaultSmsSubIdMethod by lazy {
        SubscriptionManager::class.java.getDeclaredMethod("setDefaultSmsSubId", Integer.TYPE)
    }


    @SuppressLint("MissingPermission")
    fun getActivitySubInfoList() = mSubscriptionManager.activeSubscriptionInfoList

    @SuppressLint("MissingPermission")
    fun getActivitySubInfoCount() = mSubscriptionManager.activeSubscriptionInfoCount

    fun getActivitySubIdCountMax() = mSubscriptionManager.activeSubscriptionInfoCountMax

    fun setDefaultDataSubId(subId: Int) {
        Timber.e("setDefaultDataSubId($subId);")
        mSetDefaultDataSubIdMethod.invoke(mSubscriptionManager, subId)
    }

    fun setDefaultSmsSubId(subId: Int) {
        Timber.e("setDefaultSmsSubId($subId);")
        mSetDefaultSmsSubIdMethod.invoke(mSubscriptionManager, subId)
    }

    fun openApnSetting(subId: Int) {
        Intent(Settings.ACTION_APN_SETTINGS).apply {
            putExtra("sub_id", subId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            mContext.startActivity(this)
        }
    }

}