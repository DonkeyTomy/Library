package com.zzx.utils.usb

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.usb.UsbManager
import com.zzx.utils.file.StorageManagerWrapper
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

@SuppressLint("PrivateApi")
class UsbManagerWrapper(var mContext: Context) {

    private val mUsbManager = mContext.getSystemService(UsbManager::class.java)

    private val setModelFunction by lazy {
        UsbManager::class.java.getDeclaredMethod("setCurrentFunction", String::class.java, Boolean::class.java)
    }

    fun enableMtpModel() {
        setCurrentModel(FUNCTION_MTP)
    }

    fun enableFtpModel() {
        setCurrentModel(FUNCTION_FTP)
    }

    fun enableMassModel(preExecFun: () -> Unit, delayInMill: Long = 1000) {
        Observable.just(Unit)
                .map {
                    preExecFun()
                }
                .delay(delayInMill, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.newThread())
                .subscribe {
                    StorageManagerWrapper.unmountStorage(mContext)
                    setCurrentModel(FUNCTION_MASS)
                }

    }

    private fun setCurrentModel(function: String) {
        setModelFunction.invoke(mUsbManager, function, true)
    }

    companion object {
        const val FUNCTION_MTP  = "mtp"
        const val FUNCTION_FTP  = "ftp"
        const val FUNCTION_PTP  = "ptp"
        const val FUNCTION_MASS  = "mass_storage"

        const val ACTION_USB_STATE  = "android.hardware.usb.action.USB_STATE"
        const val USB_CONNECTED     = "connected"
        const val USB_CONFIGURED = "configured"
    }

}