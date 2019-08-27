package com.zzx.utils.usb

import android.content.Context
import android.hardware.usb.UsbManager

class UsbManagerWrapper(context: Context) {

    private val mUsbManager = context.getSystemService(UsbManager::class.java)

    private val setModelFunction by lazy {
        UsbManager::class.java.getDeclaredMethod("setCurrentFunction", String::class.java, Boolean::class.java)
    }

    fun enableMtpModel() {
        setCurrentModel(FUNCTION_MTP)
    }

    fun enableFtpModel() {
        setCurrentModel(FUNCTION_FTP)
    }

    fun enableMassModel() {
        setCurrentModel(FUNCTION_MASS)
    }

    private fun setCurrentModel(function: String) {
        setModelFunction.invoke(mUsbManager, function, true)
    }

    companion object {
        const val FUNCTION_MTP  = "mtp"
        const val FUNCTION_FTP  = "ftp"
        const val FUNCTION_MASS  = "mass_storage"
    }

}