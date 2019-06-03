package com.zzx.utils

import android.app.Application
import com.zzx.utils.file.FileUtil
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

/**@author Tomy
 * Created by Tomy on 2018/8/12.
 */
class ExceptionHandler private constructor(application: Application, dir: String): Thread.UncaughtExceptionHandler {
    private val mFormatter = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault())
    private val mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    private var mContext: Application? = application

    private val LOG_DIR by lazy {
        "${FileUtil.getStoragePath(mContext!!)}/log/$dir"
    }

    init {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    fun release() {
        mContext = null
        Thread.setDefaultUncaughtExceptionHandler(null)
    }

    private fun handleException(ex: Throwable) {
        saveException2File(ex)
        ex.printStackTrace()
    }

    @Synchronized
    fun saveException2File(ex: Throwable) {
        Observable.just(ex)
                .observeOn(Schedulers.io())
                .subscribe {
                    val stringWriter = StringWriter()
                    val writer = PrintWriter(stringWriter)
                    ex.printStackTrace(writer)
                    var cause = ex.cause
                    while (cause != null) {
                        cause.printStackTrace(writer)
                        cause = cause.cause
                    }
                    writer.close()
                    val result = stringWriter.toString()
                    val time = mFormatter.format(Date())
                    val fileName = "$time.txt"
                    if (LOG_DIR.isNotEmpty()) {
                        val dir = File(LOG_DIR)
                        if (!dir.exists() || !dir.isDirectory) {
                            dir.mkdirs()
                        }
                        val fos = FileWriter(File(LOG_DIR, fileName))
                        fos.write(result)
                        fos.close()
                    }
                }
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
            handleException(e)
            mDefaultHandler.uncaughtException(t, e)
    }

    companion object {
        private var mInstance: ExceptionHandler? = null
        fun getInstance(application: Application? = null, dir: String = ""): ExceptionHandler {
            if (mInstance != null) {
                return mInstance!!
            }
            if (mInstance == null) {
                mInstance = ExceptionHandler(application!!, dir)
            }
            return mInstance!!
        }

        const val TAG = "ExceptionHandler"
    }

}