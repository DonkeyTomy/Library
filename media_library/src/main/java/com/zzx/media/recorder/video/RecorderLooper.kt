package com.zzx.media.recorder.video

import android.content.Context
import android.hardware.Camera
import android.media.CamcorderProfile
import android.os.SystemClock
import android.view.Surface
import com.zzx.media.camera.ICameraManager
import com.zzx.media.camera.v1.manager.Camera1Manager
import com.zzx.media.camera.v2.manager.Camera2Manager
import com.zzx.media.recorder.IRecorder
import com.zzx.media.utils.FileNameUtils
import com.zzx.utils.file.FileUtil
import com.zzx.utils.zzx.DeviceUtils
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**@author Tomy
 * Created by Tomy on 2018/6/11.
 */
class RecorderLooper<surface, camera>(var mContext: Context, @IRecorder.FLAG flag: Int, private var mNeedLoopDelete: Boolean = false) {

    var mRecorder: IRecorder = VideoRecorder(false)

    init {
        mRecorder.setFlag(flag)
        mRecorder.setRecordCallback(RecordStateCallback())
    }

    private var mStartTime = 0L


    private var mCameraManager: ICameraManager<surface, camera>? = null

    private var mLastDir: File? = null

    private var mRecordDuration = 20

    private var mDirPath: String? = null

    private var mRotation = 0

    private var mLooping = AtomicBoolean(false)

    private var mRecording = AtomicBoolean(false)

    private var mDelayRecord = AtomicBoolean(false)

    private var mOutputFile: File? = null

    private var mFileList: ArrayList<File>? = null

    /**
     * 代表是否录完立即就删除.预录模式下录一个删一个.
     */
    private var mAutoDelete = false

    private var mAutoDeleteFileCount = 0

    private var mAutoDeleteFilePre: File? = null

    private var mRecordStateCallback: IRecorder.IRecordCallback? = null

    private var mQuality = CamcorderProfile.QUALITY_720P

    private var mHighQuality = true

    private val mRecordScheduler = Schedulers.from(Executors.newSingleThreadExecutor())

    /**
     * @param needLoop 设置是否开启循环录像自动删除功能.
     */
    fun setNeedLoopDelete(needLoop: Boolean) {
        Timber.e("setNeedLoopDelete: $needLoop")
        mNeedLoopDelete = needLoop
    }


    fun setCameraManager(cameraManager: ICameraManager<surface, camera>?) {
        mCameraManager = cameraManager
        mCameraManager?.apply {

            if (mCameraManager is Camera2Manager) {
                setRecordPreviewCallback(object : ICameraManager.RecordPreviewReady {
                    override fun onRecordPreviewReady() {
                        mRecorder.startRecord()
                    }
                })
            }


        }
    }

    fun setCamera() {
        mRecorder.setCamera(mCameraManager?.getCameraDevice() as Camera)
    }

    fun setQuality(quality: Int, highQuality: Boolean = true) {
        mQuality = quality
        mHighQuality = highQuality
    }

    private fun setupRecorder() {
        setRecordHintRotation()
        mOutputFile = File(mDirPath, FileNameUtils.getTmpVideoName("${DeviceUtils.getUserNum(mContext)}_"))
        mRecorder.setOutputFile(mOutputFile!!)
        mRecorder.setProperty(mQuality, mHighQuality)
    }

    private fun setRecordHintRotation() {
        if (mRecorder is VideoRecorder) {
            (mRecorder as VideoRecorder).setSensorRotationHint(mCameraManager!!.getSensorOrientation())
        }
    }

    fun getOutputFile(): File? = mOutputFile

    fun setRecordDuration(duration: Int) {
        mRecordDuration = if (duration >= 0) duration else 0
    }

    fun setRotation(rotation: Int) {
        mRotation = rotation
        mRecorder.setSensorRotationHint(Surface.ROTATION_90)
    }

    fun setDirPath(dirPath: String?) {
        mDirPath = dirPath
    }

    fun setFlag(@IRecorder.FLAG flag: Int) {
        mRecorder.setFlag(flag)
    }

    fun setRecordCallback(callback: IRecorder.IRecordCallback?) {
        mRecordStateCallback = callback
    }

    /**
     * @see stopRecord
     * @see stopLooper
     * @see startLooper
     * @return Boolean
     */
    fun startRecord():Boolean {
        mStartTime = SystemClock.elapsedRealtime()
        Timber.e("startRecord. mRecording = ${mRecording.get()}; mDirPath = $mDirPath")
        if (mRecording.get()) {
            return true
        }
        if (mDirPath == null) {
            return false
        }
        mRecording.set(true)
        mCameraManager?.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)
        setupRecorder()
        try {
            return if (mCameraManager is Camera2Manager && mRecorder.getState() == IRecorder.State.PREPARED) {
                mCameraManager!!.startRecordPreview(mRecorder.getSurface())
                true
            } else {
                false
            }
        } catch (e: Exception) {
            mRecording.set(false)
            e.printStackTrace()
        }
        return false
    }

    /**
     * @see startRecord
     * @see stopLooper
     */
    fun stopRecord() {
        Timber.e("stopRecord")
        if (mRecording.get()) {
            mDelayRecord.set(false)
            mRecordDelayDisposable?.dispose()
            mRecorder.reset()
            mCameraManager?.apply {
                stopRecord()
                startPreview()
            }
            mRecording.set(false)
        }
//        FileNameUtils.tmpFile2Video(mOutputFile)
    }

    fun stop() {
        if (mLooping.get()) {
            stopLooper()
        } else {
            stopRecord()
        }
    }

    fun release() {
        stopLooper()
        mRecorder.release()
        setCameraManager(null)
        setRecordCallback(null)
    }

    fun isRecording(): Boolean {
        Timber.e("isRecording: loop[${mLooping.get()}], recording[${mRecording.get()}]")
        return mLooping.get() || mRecording.get()
    }

    fun isLoopRecording(): Boolean {
        Timber.e("isLoopRecording: ${mLooping.get()}")
        return mLooping.get()
    }

    fun isRecordStartingOrStopping(): Boolean {
        return mRecorder.isRecordStartingOrStopping()
    }

    /**
     * @see startLooper
     */
    fun stopLooper(finish: ()-> Unit = {}) {
        if (!mLooping.get())
            return
        val timer = isRecordStartingOrStopping()
        Timber.e("stopLooper.recording = ${mLooping.get()}. isRecordStartingOrStopping = $timer")
        Observable.just(timer)
                .observeOn(mRecordScheduler)
                .subscribe {
                    if (!it) {
                        mRecordLoopDisposable?.dispose()
                        mRecordLoopDisposable   = null
                        mLooping.set(false)
                        mCheckLoopDisposable?.dispose()
                        mCheckLoopDisposable    = null
                        stopRecord()
                        finish()
                    } else {
                        Observable.interval(100L, 100L, TimeUnit.MILLISECONDS)
                                .observeOn(mRecordScheduler)
                                .subscribe(
                                        {
                                            if (!isRecordStartingOrStopping() && mStopDisposable?.isDisposed == false) {
                                                mRecordLoopDisposable?.dispose()
                                                mRecordLoopDisposable   = null
                                                mLooping.set(false)
                                                mCheckLoopDisposable?.dispose()
                                                mCheckLoopDisposable    = null
                                                stopRecord()
                                                mStopDisposable?.dispose()
                                                mStopDisposable = null
                                                finish()
                                            }
                                        },
                                        {},
                                        {},
                                        {
                                            disposable ->
                                            mStopDisposable = disposable
                                        }
                                )

                    }
                }


    }

    fun recordSection() {
        if (mLooping.get()) {
            mLooping.set(false)
            startLooper()
        }
    }

    fun cancelLooperTimer() {
        if (mLooping.get()) {
            mRecordLoopDisposable?.dispose()
            mRecordLoopDisposable = null
            mLooping.set(false)
        }
    }

    /**
     * @see startLooper
     * 循环录像Looper的Disposable
     */
    private var mRecordLoopDisposable: Disposable?  = null
    /**
     * 循环删除的Disposable
     * @see checkStorageSpace
     */
    private var mCheckLoopDisposable: Disposable?   = null
    /**
     * 延迟录像的disposable,用于取消.
     * @see delayStop
     */
    private var mRecordDelayDisposable: Disposable? = null

    private var mStopDisposable: Disposable? = null

    fun record() {
        stopLooper()
        Observable.just(Unit)
                .map {
                    startRecord()
                }.delay(10, TimeUnit.SECONDS)
                .map {
                    stopRecord()
                }
    }

    /**
     * @param autoDelete 是否删除上一个录完的视频文件.
     */
    fun setAutoDelete(autoDelete: Boolean) {
        mAutoDelete = autoDelete
    }

    /**
     * 若[duration] > 0,则取消LooperTimer后,重新开启间隔为[duration]的新Looper.
     * @param autoDelete 自动删除录像文件(预录模式下专用),默认为false
     * @see stopLooper
     */
    fun startLooper(duration: Int = 0, autoDelete: Boolean = false) {
        mAutoDelete = autoDelete
        mAutoDeleteFileCount = 0
        /*if (mLooping.get()) {
            return
        }*/
        cancelLooperTimer()
        mRecordDelayDisposable?.dispose()
        mRecordDelayDisposable = null
        if (duration > 0) {
            setRecordDuration(duration)
        }
        Timber.e("startLooper.duration = $duration, autoDelete = $autoDelete")
        Observable.just(Unit)
                .observeOn(mRecordScheduler)
                .map {
                    checkNeedDelete(true)
                }
                .subscribe {
                    mLooping.set(it)
                    if (it) {
                        checkStorageSpace()
                        startRecord()
                        Observable.interval(mRecordDuration.toLong(), mRecordDuration.toLong(), TimeUnit.SECONDS)
                                .observeOn(mRecordScheduler)
                                .subscribe(
                                        {
                                            stopRecord()
                                            if (mRecordLoopDisposable?.isDisposed == false) {
                                                startRecord()
                                            }
                                        },
                                        {
                                            throwable ->
                                            throwable.printStackTrace()
                                        },
                                        {
                                        },
                                        {
                                            disposable ->
                                            mRecordLoopDisposable = disposable
                                        }
                                )
                    } else {
                        mRecordStateCallback?.onRecordStop(IRecorder.IRecordCallback.RECORD_STOP_EXTERNAL_STORAGE_NOT_ENOUGH)
                        stopRecord()
                    }
                }
    }

    /**
     * 若当前已有循环则取消当前循环录像,延迟录像[newDuration]之后重新启动间隔为[newDuration]的新Looper
     * @param newDuration Int
     */
    fun resetTimer(newDuration: Int) {
        if (newDuration <= 0)
            return
        if (mLooping.get()) {
            Observable.just(Unit)
                    .map {
                        cancelLooperTimer()
                    }
                    .delay(newDuration.toLong(), TimeUnit.SECONDS)
                    .subscribe {
                        startLooper(newDuration)
                    }
        } else {
            startLooper(newDuration)
        }

    }

    /**
     * 延迟停止.延录
     * @param delay Int
     */
    fun delayStop(delay: Int, function: ()->Unit) {
        if (delay <= 0) {
            return
        }
        mDelayRecord.set(true)
        mRecordDelayDisposable?.dispose()
        mRecordDelayDisposable = null
        mRecordDelayDisposable = Observable.just(Unit)
                .map {
                    cancelLooperTimer()
                }
                .delay(delay.toLong(), TimeUnit.SECONDS)
                .observeOn(mRecordScheduler)
                .subscribe {
                    stopRecord()
                    function()
                }
    }

    fun isDelayRecord(): Boolean {
        return mDelayRecord.get()
    }

    /***
     * @see startLooper
     */
    fun checkStorageSpace() {
        if (mCheckLoopDisposable == null) {
            Observable.interval(5, 10L, TimeUnit.SECONDS)
                    .observeOn(Schedulers.io())
                    .doOnDispose {
                        Timber.e("checkStorageSpace Disposed")
                    }
                    .subscribe(
                            {
                                checkNeedDelete()
                            }, {}, {},
                            {
                                disposable ->
                                mCheckLoopDisposable = disposable
                            }
                    )
        }

    }

    /**
     * [startLooper]
     * @param first Boolean
     * @return Boolean
     */
    fun checkNeedDelete(first: Boolean = false): Boolean {
        var freeSpace = FileUtil.getDirFreeSpaceByMB(FileUtil.getExternalStoragePath(mContext))
        val needSpace = if (first) 30 else 300
        Timber.e("currentFreeSpace = $freeSpace, mNeedLoopDelete = $mNeedLoopDelete")
        if (mNeedLoopDelete) {
            var count = 0
            while (freeSpace <= needSpace) {
                if (first) {
                    if (count++ >= 10) {
                        return if (freeSpace >= needSpace) {
                            true
                        } else {
                            stopLooper()
                            mRecordStateCallback?.onRecordStop(IRecorder.IRecordCallback.RECORD_STOP_EXTERNAL_STORAGE_NOT_ENOUGH)
                            false
                        }
                    }
                }
                if (mFileList?.size ?: 0 == 0) {
                    mLastDir?.delete()
                    val dirList = FileUtil.sortDirTime(File(mDirPath!!).parentFile)
                    for (dir in dirList) {
                        if (dir == mLastDir) {
                            break
                        }
                        mFileList   = FileUtil.sortDirTime(dir)
                        mLastDir = dir
                        Timber.e("${dir.path}.size = ${mFileList?.size}")
                        if (mFileList?.isEmpty() == true) {
                            FileUtil.deleteFile(dir)
                        } else {
                            break
                        }
                    }
                }
                Timber.e("mFileList.size = ${mFileList?.size}")
                mFileList?.apply {
                    for (file in this) {
                        Timber.e("fileName = ${file.absolutePath}")
                        if (file.absolutePath != mOutputFile?.absolutePath) {
                            FileUtil.deleteFile(file)
                            remove(file)
                            break
                        } else {
                            remove(file)
                        }
                    }
                }
                freeSpace = FileUtil.getDirFreeSpaceByMB(FileUtil.getExternalStoragePath(mContext))
            }

        } else if (freeSpace <= 30) {
//            val currentTime = SystemClock.elapsedRealtime() - mStartTime
            if (isLoopRecording()) {
                /*Observable.just(Unit)
                        .delay(if (currentTime > 1000) 0L else 1000L, TimeUnit.MILLISECONDS)
                        .subscribe {*/
                            stopLooper()
                            mRecordStateCallback?.onRecordStop(IRecorder.IRecordCallback.RECORD_STOP_EXTERNAL_STORAGE_NOT_ENOUGH)
//                        }
            } else if (isRecording()) {
                /*Observable.just(Unit)
                        .delay(if (currentTime > 1000) 0L else 1000L, TimeUnit.MILLISECONDS)
                        .subscribe {*/
                            stopRecord()
                            mRecordStateCallback?.onRecordStop(IRecorder.IRecordCallback.RECORD_STOP_EXTERNAL_STORAGE_NOT_ENOUGH)
//                        }
            }
            return false
        }
        return true
    }

    inner class RecordStateCallback: IRecorder.IRecordCallback {

        override fun onRecorderPrepared() {
            mRecordStateCallback?.onRecorderPrepared()
            if (mCameraManager is Camera1Manager) {
                mCameraManager?.startRecord()
                mRecorder.startRecord()
            }
        }

        override fun onRecordStart() {
//            if (!mAutoDelete) {
                mRecordStateCallback?.onRecordStart()
//            }
        }

        override fun onRecorderConfigureFailed() {
            stopLooper()
            mRecordStateCallback?.onRecorderConfigureFailed()
        }

        override fun onRecordError(errorCode: Int) {
            stopLooper()
            mRecordStateCallback?.onRecordError(errorCode)
        }

        override fun onRecordStop(stopCode: Int) {
            mRecordStateCallback?.onRecordStop(stopCode)
        }

        override fun onRecorderFinished(file: File?) {
            if (mAutoDelete) {
                mAutoDeleteFileCount++
                Timber.e("File: ${file?.absolutePath}. mAutoDeleteFileCount = $mAutoDeleteFileCount")
                if (mAutoDeleteFileCount > 1) {
                    mAutoDeleteFilePre?.delete()
                    Timber.e("delete File: ${mAutoDeleteFilePre?.absolutePath}")
                }
                mRecordStateCallback?.onRecorderFinished(file)
                file?.apply {
                    mAutoDeleteFilePre = FileNameUtils.tmpFile2Video(this)
                }
            } else {
                mRecordStateCallback?.onRecorderFinished(file)
            }
        }

        override fun onRecordPause() {
        }

        override fun onRecordResume() {
        }

    }

}