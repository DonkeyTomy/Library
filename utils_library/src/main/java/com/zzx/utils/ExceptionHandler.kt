package com.zzx.utils

import android.app.Application
import com.zzx.utils.file.FileUtil
import com.zzx.utils.rxjava.FlowableUtil
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

/**@author Tomy
 * Created by Tomy on 2018/8/12.
 */
class ExceptionHandler private constructor(application: Application): Thread.UncaughtExceptionHandler {
    private val mFormatter = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault())
    private val mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    private var mContext: Application? = application

    private val LOG_DIR by lazy {
        FileUtil.getExternalStoragePath(mContext!!)
    }

    init {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    fun release() {
        mContext = null
        Thread.setDefaultUncaughtExceptionHandler(null)
    }

    private fun handleException(ex: Throwable) {
        FlowableUtil.setBackgroundThread(Consumer {
            saveException2File(ex)
        })
        ex.printStackTrace()
    }

    private fun saveException2File(ex: Throwable) {
        val writer = PrintWriter(StringWriter())
        ex.printStackTrace(writer)
        var cause = ex.cause
        while (cause != null) {
            cause.printStackTrace(writer)
            cause = cause.cause
        }
        writer.close()
        val result = writer.toString()
        try {
            val time = mFormatter.format(Date())
            val fileName = "$time.txt"
            if (LOG_DIR != "") {
                val dir = File(LOG_DIR)
                if (!dir.exists() || !dir.isDirectory) {
                    dir.mkdirs()
                }
                val fos = FileWriter(File(LOG_DIR, fileName))
                fos.write(result)
                fos.close()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun uncaughtException(t: Thread?, e: Throwable?) {
        try {
            handleException(e!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    companion object {
        private var mInstance: ExceptionHandler? = null
        fun getInstance(application: Application): ExceptionHandler {
            if (mInstance == null) {
                mInstance = ExceptionHandler(application)
            }
            return mInstance!!
        }

        const val TAG = "ExceptionHandler"
    }

}