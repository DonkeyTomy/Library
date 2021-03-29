package com.tomy.lib.ui.activity

import android.app.Application
import com.zzx.utils.TTSToast
import timber.log.Timber

/**@author Tomy
 * Created by Tomy on 2018/11/5.
 */
open class BaseApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        if (Timber.treeCount() <= 0) {
            Timber.plant(Timber.DebugTree())
        }
        TTSToast.init(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        Timber.uprootAll()
        TTSToast.release()
    }

}