package com.zzx.utils.file

import java.io.File

/**@author Tomy
 * Created by Tomy on 2018/6/18.
 */
interface IFileLocker {

    fun lockFile(file: File)

    fun lockFile(path: String)

    fun isLockFileFull(): Boolean

    fun deleteLastLockFile()

    fun setLockDir(dir: File)

    /**
     * 文件锁定状态回调
     * */
    fun setLockListener(listener: FileLockListener)

    interface FileLockListener {

        fun onLockStart()

        fun onLockFinished()

        fun onLockFailed()

    }

}