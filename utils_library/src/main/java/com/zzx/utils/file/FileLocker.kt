package com.zzx.utils.file

import com.zzx.utils.CommonConst
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File

/**@author Tomy
 * Created by Tomy on 2018/6/18.
 */
class FileLocker(private var mLockDir: File?): IFileLocker {

    private var mListener: IFileLocker.FileLockListener? = null

    override fun lockFile(path: String) {
        lockFile(File(path))
    }

    override fun isLockFileFull(): Boolean {
        return FileUtil.getDirFileCount(getLockDir()!!) >= LOCK_FILE_COUNT
    }

    /**
     * 文件锁定状态回调
     * */
    override fun setLockListener(listener: IFileLocker.FileLockListener) {
        mListener = listener
    }

    override fun lockFile(file: File) {
        if (!FileUtil.checkFileExist(file)) {
            return
        }

        Observable.just(file)
                .observeOn(AndroidSchedulers.mainThread())
                .map {
                    it ->
                    if (isLockFileFull()) {

                    }
                    return@map it
                }.observeOn(Schedulers.newThread())
                .map {
                    mListener?.onLockStart()
                    return@map startLock(it)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    when(it) {
                        CommonConst.SUCCESS -> mListener?.onLockFinished()
                        CommonConst.FAILED  -> mListener?.onLockFailed()
                    }
                }

    }

    fun startLock(file: File): Int {
        try {
            if (!FileUtil.checkDirExist(mLockDir!!, true)) {
                return CommonConst.FAILED
            }
            val runtime = Runtime.getRuntime()
            Timber.e(" ============= startLock ============= ")
            val process = runtime.exec("mv ${file.absolutePath} ${mLockDir!!.absolutePath}")
            process.waitFor()
            process.destroy()
            Timber.e(" ============= finishLock ============= ")
            return CommonConst.SUCCESS
        } catch (e: Exception) {
            e.printStackTrace()
            return CommonConst.FAILED
        }
    }

    override fun setLockDir(dir: File) {
        mLockDir = dir
    }

    override fun deleteLastLockFile() {

    }

    fun getLockDir(): File? {
        return mLockDir
    }

    companion object {
        const val LOCK_FILE_COUNT   = 10
    }
}