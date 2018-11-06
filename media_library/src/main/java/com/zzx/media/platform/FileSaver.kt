package com.zzx.media.platform

import android.content.Context
import android.location.Location
import com.zzx.media.bean.SaveRequest
import com.zzx.media.utils.FileNameUtils
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**@author Tomy
 * Created by Tomy on 2018/10/22.
 */
class FileSaver(var mContext: Context): IFileSaver {

    private val mQueue = ArrayDeque<SaveRequest>()

    private var mDisposable: Disposable? = null

    override fun init(fileType: IFileSaver.FILE_TYPE, outputFileFormat: Int, resolution: String, rotation: Int) {
    }

    override fun uninit() {
    }

    override fun setRawFlagEnabled(isEnable: Boolean) {
    }

    override fun savePhotoFile(photoData: ByteArray, file: File, date: Long, location: Location?, tag: Int, listener: IFileSaver.OnFileSavedListener?): Boolean {
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
        mDisposable?.dispose()
        mQueue.clear()
        mDisposable = null
    }

    private fun startSave() {
        if (mDisposable == null) {
            Observable.just(mQueue)
                    .observeOn(Schedulers.io())
                    .subscribe(
                            {
                                while (mDisposable?.isDisposed == false) {
                                    var request: SaveRequest? = null
                                    synchronized(mQueue) {
                                        if (mQueue.isNotEmpty()) {
                                            request = mQueue.pop()
                                        } else {
                                            Thread.sleep(100)
                                        }
                                    }
                                    if (request != null) {
                                        saveRequest(request!!)
                                    }

                                }
                            }, {}, {}, {
                            mDisposable = it
                        }
                    )
        }
    }

    private fun saveRequest(request: SaveRequest) {
        val output = FileOutputStream(request.file)
        Timber.e("saveRequest = ${request.file.absolutePath}")
        output.write(request.data)
        output.close()
        FileNameUtils.insertImage(mContext, request.file)
    }


}