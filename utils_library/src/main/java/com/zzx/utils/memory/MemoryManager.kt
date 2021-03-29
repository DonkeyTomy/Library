package com.zzx.utils.memory

import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration


/**@author Tomy
 * Created by Tomy on 2019/7/17.
 * MemoryManager is designed for continuous shot,
 * who monitor the system and DVM memory status,
 * and provide the action to slow down or stop continuous shot.
 */
class MemoryManager(context: Context): ComponentCallbacks2 {

    companion object {
        // Let's signal only 40% of max memory is allowed to be used by normal
        // continuous shots.
        const val DVM_SLOWDOWN_THRESHOLD    = 0.4f
        const val DVM_STOP_THRESHOLD        = 0.1f
        const val SYSTEM_SLOWDOWN_THRESHOLD = 100L
        const val LOW_SUITABLE_SPEED_FPS    = 1
        const val BYTES_IN_KILOBYTE         = 1024L
        const val LOW_MEMORY_DEVICE         = 512
        const val LOW_MEMORY_DIVISOR        = 2L
        const val SYSTEM_STOP_DIVISOR       = 2L
    }

    private val mMaxDvmMemory: Long = 0
    private val mDvmSlowdownThreshold: Long = 0
    private val mDvmStopThreshold: Long = 0
    private val mSystemSlowdownThreshold: Long = 0
    private val mSystemStopThreshold: Long = 0
    private val mMiniMemFreeMb: Long = 0
    private val mLeftStorage: Long = 0
    private val mUsedStorage: Long = 0
    private val mPengdingSize: Long = 0
    private val mStartTime: Long = 0
    private val mCount: Int = 0
    private val mSuitableSpeed: Int = 0

    private val mMemoryActon = MemoryAction.NORMAL

    private val mRuntime = Runtime.getRuntime()

    enum class MemoryAction {
        NORMAL, ADJUST_SPEED, STOP
    }

    override fun onLowMemory() {
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
    }

    override fun onTrimMemory(level: Int) {
    }


}