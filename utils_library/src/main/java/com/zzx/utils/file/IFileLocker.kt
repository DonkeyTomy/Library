package com.zzx.utils.file

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File

/**@author Tomy
 * Created by Tomy on 2018/6/18.
 * @see lockFile 实现该方法.返回是否锁定成功
 * @see FileLockListener
 */
abstract class IFileLocker {

    companion object {
        const val TAG = "[IFileLocker]: "
    }


    protected var mListener: IFileLocker.FileLockListener? = null


    abstract fun lockFile(file: File): Boolean

    /**
     * @param file File
     */
    fun startLockFile(file: File) {
        Observable.just(file)
                .observeOn(AndroidSchedulers.mainThread())
                .map {
                    Timber.e("$TAG onLockStart: thread = ${Thread.currentThread().name}")
                    mListener?.onLockStart()
                }.observeOn(Schedulers.io())
                .map {
                    Timber.e("$TAG onLockProgress: thread = ${Thread.currentThread().name}")
                    lockFile(file)
                }.observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Timber.e("$TAG onLockFinished: thread = ${Thread.currentThread().name}")
                    if (it) {
                        mListener?.onLockFinished()
                    } else {
                        mListener?.onLockFailed()
                    }
                }
    }

    fun startLockFile(path: String) = startLockFile(File(path))

    abstract fun isLockFileFull(): Boolean

    abstract fun deleteLastLockFile()

    abstract fun setLockDir(dir: File)

    /**
     * 文件锁定状态回调
     * */
    fun setLockListener(listener: FileLockListener) {
        mListener = listener
    }

    /**
     * 回调均运行于UI线程.可做一些UI提示.
     */
    interface FileLockListener {

        /**
         * 开始锁定.
         */
        fun onLockStart()

        /**
         * 锁定成功.
         */
        fun onLockFinished()

        /**
         * 锁定失败
         */
        fun onLockFailed()

    }

}