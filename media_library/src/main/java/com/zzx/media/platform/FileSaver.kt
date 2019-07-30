package com.zzx.media.platform

import android.content.Context
import android.location.Location
import com.zzx.media.bean.SaveRequest
import com.zzx.media.utils.FileNameUtils
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**@author Tomy
 * Created by Tomy on 2018/10/22.
 */
class FileSaver(var mContext: Context): IFileSaver {

    private val mQueue = ArrayDeque<SaveRequest>()

    private var mSubscribe: Observable<Unit>? = null

    private val mObject = Object()

    private val mSubscribed = AtomicBoolean(false)

    private var mSaveDone = false

    override fun init(fileType: IFileSaver.FILE_TYPE, outputFileFormat: Int, resolution: String, rotation: Int) {
    }

    override fun uninit() {
    }

    override fun setRawFlagEnabled(isEnable: Boolean) {
    }

    override fun savePhotoFile(photoData: ByteArray?, file: File?, date: Long, location: Location?, tag: Int, listener: IFileSaver.OnFileSavedListener?): Boolean {
        synchronized(mQueue) {
            mQueue.add(SaveRequest(photoData, file))
        }
        startSave()
        return true
    }

    override fun saveRawFile(dngData: ByteArray, width: Int, height: Int, fileName: String, date: Long, location: Location, tag: Int, listener: IFileSaver.OnFileSavedListener): Boolean {
        return false
    }

    override fun saveVideoFile(location: Location, tempPath: String, duration: Long, tag: Int, listener: IFileSaver.OnFileSavedListener): Boolean {
        return false
    }

    override fun waitDone() {
        Timber.tag(FileSaver::class.java.simpleName).w("waitDone()")
        synchronized(mObject) {
            if (!mSaveDone) {
                mObject.wait()
            }
        }
        Timber.tag(FileSaver::class.java.simpleName).w("mQueue.size = ${mQueue.size}")
        Timber.tag(FileSaver::class.java.simpleName).w("done()")
        mSaveDone = false
        mSubscribed.set(false)
        mQueue.clear()
    }

    private fun startSave() {
        if (mSubscribe == null) {
            mSubscribe = Observable.just(Unit)
                    .observeOn(Schedulers.io())
                    .map {
//                        Timber.tag(FileSaver::class.java.simpleName).e("thread = ${Thread.currentThread().name}")
                        while (!mSaveDone) {
                            var request: SaveRequest? = null
                            synchronized(mQueue) {
                                if (mQueue.isNotEmpty()) {
                                    request = mQueue.pop()
                                } else {
                                    Thread.sleep(100)
                                }
                            }
//                            Timber.tag(FileSaver::class.java.simpleName).w("request = ${request == null}")
                            synchronized(mObject) {
                                request?.apply {
                                    if (file != null && data != null) {
                                        saveRequest(this)
                                        Thread.sleep(100)
                                    } else {
                                        mSaveDone = true
                                        Timber.tag(FileSaver::class.java.simpleName).e("mSaveDone = $mSaveDone")
                                        mObject.notifyAll()
                                    }
                                }
                            }
                        }
                    }

        }
        if (!mSubscribed.get()) {
            mSubscribed.set(true)
            mSubscribe?.subscribe()
        }
    }

    private fun saveRequest(request: SaveRequest) {
        val output = FileOutputStream(request.file!!)
        Timber.tag(FileSaver::class.java.simpleName).e("saveRequest = ${request.file.absolutePath}")
        output.write(request.data!!)
        output.close()
        FileNameUtils.insertImage(mContext, request.file)
        Timber.tag(FileSaver::class.java.simpleName).w("saveRequest done")
    }


}