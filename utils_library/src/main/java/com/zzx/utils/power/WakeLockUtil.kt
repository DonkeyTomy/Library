package com.zzx.utils.power

import android.content.Context
import android.os.PowerManager
import java.util.concurrent.atomic.AtomicBoolean

/**@author Tomy
 * Created by Tomy on 2019/9/25.
 */
class WakeLockUtil(context: Context) {

    private val mLockAcquire = AtomicBoolean(false)

    private val mPm by lazy {
        context.getSystemService(PowerManager::class.java)
    }

    private val mWakeLock by lazy {
        mPm?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "cpuLock")
    }

    fun lock() {
        if (mLockAcquire.get()) {
            return
        }
        mWakeLock?.acquire()
        mLockAcquire.set(true)
    }

    fun releaseLock() {
        if (!mLockAcquire.get()) {
            return
        }
        mWakeLock?.release()
        mLockAcquire.set(false)
    }

}