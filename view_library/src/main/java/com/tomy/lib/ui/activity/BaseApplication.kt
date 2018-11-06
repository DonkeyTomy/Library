package com.tomy.lib.ui.activity

import android.app.Application
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
    }

    override fun onTerminate() {
        super.onTerminate()
        Timber.uprootAll()
    }

}