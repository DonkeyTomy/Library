package com.zzx.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Environment
import com.zzx.utils.file.FileUtil
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**@author Tomy
 * Created by Tomy on 2018/6/20.
 */
class StorageListener(var mContext: Context, var needComputeAvailablePercent: Boolean = false) {

    private val mReceiver = StorageBroadcast()

    private var mCallback: StorageCallback? = null

    private var mDisposable: Disposable? = null

    private var mExternalPath: String? = null

    init {
        val intentFilter = IntentFilter(Intent.ACTION_MEDIA_MOUNTED).apply {
            addAction(Intent.ACTION_MEDIA_UNMOUNTED)
            addAction(Intent.ACTION_MEDIA_BAD_REMOVAL)
            addAction(Intent.ACTION_MEDIA_UNMOUNTABLE)
            addDataScheme("file")
        }
        mContext.registerReceiver(mReceiver, intentFilter)
        storageAvailable()
        Timber.e("StorageListener.registerReceiver()")
    }

    fun setStorageCallback(callback: StorageCallback) {
        mCallback = callback
    }

    fun release() {
        mContext.unregisterReceiver(mReceiver)
        mDisposable?.dispose()
        mDisposable = null
    }

    fun storageAvailable() {
        if (!needComputeAvailablePercent) {
            return
        }
        Observable.just(Unit)
                .observeOn(Schedulers.newThread())
                .map {
                    return@map FileUtil.checkExternalStorageMounted(mContext)
                }
                .subscribe {
                    if (it) {
                        if (FileUtil.getExternalStorageState(mContext) == Environment.MEDIA_UNMOUNTABLE) {
                            mCallback?.onExternalStorageChanged(true, false)
                            mCallback?.onAvailablePercentChanged(-1)
                            return@subscribe
                        }
                        mExternalPath = FileUtil.getExternalStoragePath(mContext)
                        Observable.interval(1, 10, TimeUnit.SECONDS)
                                .observeOn(Schedulers.computation())
                                .subscribe(
                                        {
                                            computeAvailableSpace()
                                        },
                                        {},
                                        {},
                                        {
                                            mDisposable = it
                                        }
                                )
                    } else {
                        mCallback?.onAvailablePercentChanged(-1)
                    }
                }

    }

    private fun computeAvailableSpace() {
        mExternalPath?.apply {
            val totalSpace = FileUtil.getDirTotalSpaceByMB(this@apply)
            val availableSpace = FileUtil.getDirFreeSpaceByMB(this@apply)
            val percent = availableSpace * 100 / totalSpace
            Timber.d("availableSpace / totalSpace = $availableSpace / $totalSpace; percent = $percent")
            mCallback?.onAvailablePercentChanged(percent.toInt())

        }

    }

    inner class StorageBroadcast: BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            Timber.e("${CommonConst.TAG_RECORD_FLOW}action = ${intent!!.action}")
            when(intent.action) {
                Intent.ACTION_MEDIA_EJECT,
                Intent.ACTION_MEDIA_UNMOUNTED,
                Intent.ACTION_MEDIA_BAD_REMOVAL -> {
                    mDisposable?.dispose()
                    mDisposable = null
                    mCallback?.onExternalStorageChanged(false, false)
                    mCallback?.onAvailablePercentChanged(-1)
                }
                Intent.ACTION_MEDIA_MOUNTED -> {
                    mCallback?.onExternalStorageChanged(true)
                    storageAvailable()
                    Timber.e( "${intent.action}. file = ${intent.data}")
                }
                Intent.ACTION_MEDIA_UNMOUNTABLE -> {
                    mDisposable?.dispose()
                    mDisposable = null
                    mCallback?.onExternalStorageChanged(true, false)
                    mCallback?.onAvailablePercentChanged(-1)
                }
            }
        }
    }

    interface StorageCallback {

        fun onExternalStorageChanged(exist: Boolean, mounted: Boolean = true)

        fun onAvailablePercentChanged(percent: Int)
    }

}