package com.zzx.media.platform

import android.content.ContentValues
import android.content.Context
import android.location.Location
import android.os.AsyncTask
import android.provider.MediaStore
import android.provider.MediaStore.Images.ImageColumns
import com.zzx.media.bean.SaveRequest
import com.zzx.media.platform.IFileSaver.OnFileSaveListener
import io.reactivex.rxjava3.core.Observable
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**@author Tomy
 * Created by Tomy on 2018/10/22.
 */
class FileSaver(var mContext: Context): IFileSaver {

    private val mQueue = LinkedList<ISaveRequest>()

    private var mSubscribe: Observable<Unit>? = null

    private val mObject = Object()

    private val mSubscribed = AtomicBoolean(false)

    private var mSaveTask: SaveTask? = null

    private var mSaveDone = false

    private val mContentResolver by lazy {
        mContext.contentResolver
    }

    override fun init(fileType: IFileSaver.FILE_TYPE, outputFileFormat: Int, resolution: String, rotation: Int) {
    }

    override fun unInit() {
    }

    override fun setRawFlagEnabled(isEnable: Boolean) {
    }

    override fun savePhotoFile(photoData: ByteArray?, file: File?, date: Long, location: Location?, tag: Int, listener: OnFileSaveListener?): Boolean {
        file?.let {
            PhotoOperator(it, FILE_TYPE_PHOTO).apply {
                mTitle   = it.nameWithoutExtension
                if (date != 0L) {
                    mDateTaken = date
                }
                setData(photoData)
                addRequest()
            }
        }
        startSave()
        return true
    }

    override fun saveRawFile(dngData: ByteArray, width: Int, height: Int, fileName: String, date: Long, location: Location, tag: Int, listener: OnFileSaveListener): Boolean {
        return false
    }

    override fun saveVideoFile(location: Location, tempPath: String, duration: Long, tag: Int, listener: OnFileSaveListener): Boolean {
        return false
    }

    override fun waitDone() {
        Timber.tag(FileSaver::class.java.simpleName).w("waitDone()")
        synchronized(mObject) {
            while (mQueue.isNotEmpty()) {
                try {
                    mObject.wait()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
//        Timber.tag(FileSaver::class.java.simpleName).w("mQueue.size = ${mQueue.size}")
        Timber.tag(FileSaver::class.java.simpleName).w("done()")
        mSaveDone = false
//        mSubscribed.set(false)
        mQueue.clear()
    }

    private val mFileSaverListner = object :OnFileSaveListener {

        override fun onQueueStatus(full: Boolean) {
        }

        override fun onFileSaved(request: ISaveRequest) {
        }

        override fun onSaveDone() {
            synchronized(mObject) {
                mObject.notifyAll()
            }
        }

    }

    private fun startSave() {
        if (mSaveTask == null) {
            mSaveTask = SaveTask()
            mSaveTask?.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }

        /*if (mSubscribe == null) {
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
//                            Timber.tag(FileSaver::class.java.simpleName).w("mRequest = ${mRequest == null}")
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
        }*/

    }

    @Synchronized
    private fun saveRequest(request: SaveRequest) {
        val output = FileOutputStream(request.file!!)
        Timber.tag(FileSaver::class.java.simpleName).e("saveRequest = ${request.file.absolutePath}")
        output.write(request.data!!)
        output.close()
        Timber.tag(FileSaver::class.java.simpleName).w("saveRequest done")
    }

    private fun saveImageToStorage(filePath: String, data: ByteArray) {
        var out: FileOutputStream? = null
        try {
            Timber.e("saveImageToStorage ----------------------- $filePath")
            out = FileOutputStream(filePath)
            out.write(data)
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                out?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun addSaveRequest(r: ISaveRequest) {
        var size: Int
        synchronized(mQueue) {
            size = mQueue.size
        }
        Timber.tag(TAG).e("addSaveRequest start")
        while (size >= QUEUE_MAX_COUNT) {
            try {
//                Timber.tag(TAG).w("addSaveRequest wait")
                synchronized(mObject) {
                    mObject.wait()
                }
//                Timber.tag(TAG).e("addSaveRequest getSize")
                synchronized(mQueue) {
                    size = mQueue.size
                }
//                Timber.tag(TAG).e("addSaveRequest continue. size = $size")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        synchronized(mQueue) {
            mQueue.add(r)
        }
//        Timber.tag(TAG).e("addSaveRequest add")
    }

    inner class PhotoOperator(file: File, fileType: Int): RequestOperator(file) {

        init {
            mFileType   = fileType
            mMimeType   = generateMimeType(mFileType)
            mDateTaken  = System.currentTimeMillis()
        }

        override fun isQueueFull(): Boolean {
            return mQueue.size >= QUEUE_MAX_COUNT
        }

        override fun prepareRequest() {
            mFileType   = FILE_TYPE_PHOTO
        }

        override fun addRequest() {
            mData?.let {
                addSaveRequest(this)
            }
        }

        @Synchronized
        override fun saveRequest() {
            mData?.apply {
                mDataSize   = size.toLong()
                mTitle      = mFile.nameWithoutExtension
                saveImageToStorage(mFile.absolutePath, this)
            }
            saveToDatabase(this)
//            Timber.tag(TAG).w("saveRequest Done")
        }


        override fun notifyListener() {
            mListener?.onFileSaved(this)
        }

        override fun saveSync() {
        }

        override fun saveToDatabase(r: RequestOperator) {
            val values = ContentValues(14)
            values.apply {
                put(ImageColumns.TITLE, r.mTitle)
                put(ImageColumns.DISPLAY_NAME, r.getFileName())
                put(ImageColumns.DATE_TAKEN, r.mDateTaken)
                put(ImageColumns.MIME_TYPE, r.mMimeType)
                put(ImageColumns.DATA, r.getFilePath())
                put(ImageColumns.SIZE, r.mDataSize)
                mLocation?.let {
                    put(ImageColumns.LATITUDE, it.latitude)
                    put(ImageColumns.LONGITUDE, it.longitude)
                }
                put(ImageColumns.ORIENTATION, r.mOrientation)
                put(ImageColumns.WIDTH, r.mWidth)
                put(ImageColumns.HEIGHT, r.mHeight)
            }
            try {
                r.mUri  = mContentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }

    inner class SaveTask: AsyncTask<Unit, Unit, Unit>() {
        var mRequest: ISaveRequest? = null
        private val mListenerObject = Object()
        override fun doInBackground(vararg params: Unit?) {
            var lastFileSaverListener: OnFileSaveListener? = null
            while (mQueue.isNotEmpty()) {
//                Timber.tag(TAG).w("------------- pop One -----------")
                mRequest = mQueue[0]
//                Timber.tag(TAG).w("------------- pop One -----------1")
                if (mRequest?.getListener() != lastFileSaverListener) {
                    lastFileSaverListener?.onSaveDone()
                }
//                Timber.tag(TAG).w("------------- pop One -----------2")
                mRequest?.apply {
                    saveRequest()
                }
//                Timber.tag(TAG).w("------------- pop One -----------3")
                mRequest?.notifyListener()
                synchronized(mQueue) {
//                    Timber.tag(TAG).w("------------- pop One -----------33")
                    mQueue.remove()
                }
//                Timber.tag(TAG).w("------------- pop One -----------4")
                try {
                    synchronized(mObject) {
                        mObject.notifyAll()
//                        Timber.tag(TAG).w("saveOne notifyAll")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                synchronized(mListenerObject) {
                    mRequest?.getListener()?.onFileSaved(mRequest!!)
                }
                lastFileSaverListener   = mRequest?.getListener()
//                Timber.tag(TAG).w("============== next One =============")
            }
            mSaveTask = null
            synchronized(mListenerObject) {
                mRequest?.getListener()?.onSaveDone()
            }
            try {
                synchronized(mObject) {
                    mObject.notifyAll()
//                    Timber.tag(TAG).w("saveDone notifyAll")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    companion object {

        const val TAG   = "save"

        const val QUEUE_MAX_COUNT   = 10

        const val FILE_TYPE_PHOTO   = 0
        const val FILE_TYPE_VIDEO   = 1
        const val FILE_TYPE_PANO    = 2
        const val FILE_TYPE_LIV     = 3 //live photo
    }
}