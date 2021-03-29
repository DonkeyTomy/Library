package com.zzx.media.recorder.video

import android.content.Context
import android.hardware.Camera
import android.media.CamcorderProfile
import android.os.SystemClock
import android.provider.MediaStore
import android.view.Surface
import com.zzx.media.camera.ICameraManager
import com.zzx.media.camera.v1.manager.Camera1Manager
import com.zzx.media.camera.v2.manager.Camera2Manager
import com.zzx.media.recorder.IRecorder
import com.zzx.media.recorder.RecordCore
import com.zzx.media.utils.FileNameUtils
import com.zzx.media.utils.MediaInfoUtil
import com.zzx.utils.file.FileUtil
import com.zzx.utils.zzx.DeviceUtils
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**@author Tomy
 * Created by Tomy on 2018/6/11.
 */
class RecorderLooper<surface, camera>(var mContext: Context, @IRecorder.FLAG flag: Int,
                                      private var mNeedLoopDelete: Boolean = false,
                                      private var mErrorAutoStart: Boolean = false) {

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

    private val mRecordCore = RecordCore()

//    private var mLooping = AtomicBoolean(false)

    private val mRecordStarting = AtomicBoolean(false)

    private val mRecordStopping = AtomicBoolean(false)

    private val mLoopNeedStop = AtomicBoolean(false)

//    private var mRecording = AtomicBoolean(false)

    private var mDelayRecord = AtomicBoolean(false)

    private var mOutputFile: File? = null

    private var mFileList: ArrayList<File>? = null

    /**
     * 代表是否录完立即就删除.预录模式下录一个删一个.
     */
    private var mAutoDelete = false

    private var mAutoDeleteFileCount = 0

    private var mAutoDeleteFilePre: File? = null

    private var mRecordStateCallback: IRecordLoopCallback? = null

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

    /**
     * @param autoStart Boolean 录制中发生错误是否需要自动重新录像而不发送错误信息
     */
    fun setErrorAutoStart(autoStart: Boolean) {
        mErrorAutoStart = autoStart
    }

    fun setCamera(camera: camera?) {
        mRecorder.setCamera(if (camera == null) null else camera as Camera)
    }

    fun startPreview() {
        Timber.d("startPreview.isLooping = ${mRecordCore.isLooping()}")
        if (mRecordCore.isLooping() && mRecordCore.isNeedRestartLoop()) {
            mRecordCore.setNeedRestartLoop(false)
            startLooper(autoDelete = mAutoDelete)
        }
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

    fun setRecordCallback(callback: IRecordLoopCallback?) {
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
        Timber.i("startRecord. mRecording = ${mRecordCore.isRecording()}; mDirPath = $mDirPath")
        if (mRecordCore.isRecording()) {
            return true
        }
        if (mDirPath == null) {
            return false
        }
//        mRecording.set(true)
        mRecordCore.startRecord()
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
//            mRecording.set(false)
            mRecordCore.stopRecord()
            e.printStackTrace()
        }
        return false
    }

    /**
     * @see startRecord
     * @see stopLooper
     */
    fun stopRecord() {
        Timber.i("stopRecord.mRecording = ${mRecordCore.isRecording()}")
        if (mRecordCore.isRecording()) {
//            mRecording.set(false)
            mDelayRecord.set(false)
            mRecordDelayDisposable?.dispose()
            mRecordCore.stopRecord()
            mRecorder.reset()
            /*mCameraManager?.apply {
                stopRecord()
                startPreview()
            }*/
        }
//        FileNameUtils.tmpFile2Video(mOutputFile)
    }

    fun stop() {
        if (mRecordCore.isLooping()) {
            stopLooper()
        } else if (mRecordCore.isRecording()) {
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
        Timber.e("isRecording: loop[${mRecordCore.isLooping()}], recording[${mRecordCore.isRecording()}]")
//        return mLooping.get() || mRecording.get()
        return !mRecordCore.isIDLE()
    }

    fun isLoopRecording(): Boolean {
        Timber.e("isLoopRecording: ${mRecordCore.isLooping()}")
        return mRecordCore.isLooping()
    }

    fun isRecordStartingOrStopping(): Boolean {
        Timber.i("mRecordStarting: ${mRecordStarting.get()}. mRecordStopping = ${mRecordStopping.get()}")
        return mRecordStarting.get() || mRecordStopping.get()
    }

    /**
     * @see startLooper
     * @see stopRecord
     */
    fun stopLooper() {
//    fun stopLooper(finish: ()-> Unit = {}) {
        if (mRecordCore.isIDLE()) {
            mRecordStateCallback?.onLoopStop(IRecordLoopCallback.STOP_CODE_LOOP_NOT_EXIST)
            return
        }
        val timer = isRecordStartingOrStopping()
        Timber.w("stopLooper.recording = ${mRecordCore.isLooping()}. isRecordStartingOrStopping = $timer")
        when {
            mRecordStopping.get() -> {
                performStopLoop(false)
            }
            mRecordStarting.get() -> {
                mLoopNeedStop.set(true)
            }
            else -> {
                performStopLoop(true)
            }
        }

        /*Observable.just(timer)
                .observeOn(mRecordScheduler)
                .subscribe {
                    if (!it) {
                        mRecordLoopDisposable?.dispose()
                        mRecordLoopDisposable   = null
//                        mLooping.set(false)
                        mRecordCore.stopLoop()
                        mCheckLoopDisposable?.dispose()
                        mCheckLoopDisposable    = null
                        stopRecord()
//                        finish()
                        mRecordStateCallback?.onLoopStop()
                    } else {
                        Observable.interval(100L, 100L, TimeUnit.MILLISECONDS)
                                .observeOn(mRecordScheduler)
                                .subscribe(
                                        {
                                            if (!isRecordStartingOrStopping() && mStopDisposable?.isDisposed == false) {
                                                mRecordLoopDisposable?.dispose()
                                                mRecordLoopDisposable   = null
//                                                mLooping.set(false)
                                                mRecordCore.stopLoop()
                                                mCheckLoopDisposable?.dispose()
                                                mCheckLoopDisposable    = null
                                                stopRecord()
                                                mStopDisposable?.dispose()
                                                mStopDisposable = null
//                                                finish()
                                                mRecordStateCallback?.onLoopStop()
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
                }*/


    }

    private fun performStopLoop(needStopRecord: Boolean) {
        mRecordLoopDisposable?.dispose()
        mRecordLoopDisposable   = null
        mRecordCore.stopLoop()
        mCheckLoopDisposable?.dispose()
        mCheckLoopDisposable    = null
        if (needStopRecord) {
            stopRecord()
        }
//        if (!mAutoDelete) {
        try {
            if (mAutoDelete) {
                mAutoDeleteFilePre?.apply {
                    delete()
                    MediaInfoUtil.deleteDatabase(mContext, absolutePath, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                }
                mAutoDeleteFilePre = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mRecordStateCallback?.onLoopStop(if (mAutoDelete) IRecordLoopCallback.LOOP_AUTO_DELETE else IRecordLoopCallback.NORMAL)
//        }
    }

    fun recordSection() {
        if (mRecordCore.isLooping()) {
//            mLooping.set(false)
            mRecordCore.stopLoop()
            startLooper()
        }
    }

    fun cancelLooperTimer() {
//        if (mRecordCore.isLooping()) {
            mRecordLoopDisposable?.dispose()
            mRecordLoopDisposable = null
//            mLooping.set(false)
            mRecordCore.stopLoop()
//        }
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
        Timber.w("startLooper.duration = $duration, mRecordDuration = ${mRecordDuration}, autoDelete = $autoDelete")
        Observable.just(Unit)
                .observeOn(mRecordScheduler)
                .map {
                    checkNeedDelete(true)
                }
                .observeOn(mRecordScheduler)
                .doOnDispose {
                    Timber.i("startLooper. doOnDispose")
                }
                .subscribe {
//                    mLooping.set(it)

                    if (it) {
                        mRecordCore.startLoop()
//                        if (!mAutoDelete) {
                        mRecordStateCallback?.onLoopStart(if (mAutoDelete) IRecordLoopCallback.LOOP_AUTO_DELETE else IRecordLoopCallback.NORMAL)
//                        }
                        checkStorageSpace()
                        Timber.i("startLooper. startRecord")
                        startRecord()
                        mRecordLoopDisposable = Observable.interval(mRecordDuration.toLong(), mRecordDuration.toLong(), TimeUnit.SECONDS)
                                .observeOn(mRecordScheduler)
                                .subscribe(
                                        {
                                            Timber.w("startLooper. stopRecord")
                                            stopRecord()
                                            if (mRecordLoopDisposable?.isDisposed == false) {
                                                startRecord()
                                            }
                                        },
                                        {
                                            throwable ->
                                            throwable.printStackTrace()
                                        }
                                )
                    } else {
                        mRecordStateCallback?.onRecordStop(IRecorder.IRecordCallback.RECORD_STOP_EXTERNAL_STORAGE_NOT_ENOUGH)
                        stopRecord()
                        mRecordCore.stopLoop()
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
        if (mRecordCore.isLooping()) {
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
            .observeOn(mRecordScheduler)
            .map {
                cancelLooperTimer()
            }
            .delay(delay.toLong(), TimeUnit.SECONDS)
            .observeOn(mRecordScheduler)
            .subscribe {
                stopRecord()
                mRecordStateCallback?.onLoopStop()
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
            mCheckLoopDisposable = Observable.interval(5, 10L, TimeUnit.SECONDS)
                    .observeOn(Schedulers.io())
                    .doOnDispose {
                        Timber.e("checkStorageSpace Disposed")
                    }
                    .subscribe(
                            {
                                checkNeedDelete()
                            }, {}
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
        val needSpace = if (first) 50 else 1000
        Timber.e("currentFreeSpace = $freeSpace, mNeedLoopDelete = $mNeedLoopDelete")
        if (mNeedLoopDelete) {
            var count = 0
            while (freeSpace <= needSpace) {
                if (first) {
                    if (count++ >= 50) {
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
                    val dirList = FileUtil.sortDirLongTime(File(mDirPath!!).parentFile)
                    for (dir in dirList) {
                        mFileList   = FileUtil.sortDirTime(dir)
                        if (dir == mLastDir && mFileList?.size ?: 0 == 1) {
                            var needBreak = false
                            mFileList?.apply {
                                for (it in this) {
                                    if (it.absolutePath == mOutputFile?.absolutePath) {
                                        mLastDir = null
                                        needBreak = true
                                        break
                                    }
                                }
                            }
                            if (needBreak)
                                continue
                        }
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

    private fun restartPreview() {
        mCameraManager?.apply {
            stopRecord()
            startPreview()
        }
    }

    inner class RecordStateCallback: IRecorder.IRecordCallback {

        override fun onRecorderPrepared() {
            mRecordStateCallback?.onRecorderPrepared()
            if (mCameraManager is Camera1Manager) {
                mRecorder.startRecord()
            }
        }

        override fun onRecordStart() {
//            if (!mAutoDelete) {
            mCameraManager?.startRecord()
            mRecordStarting.set(false)
            mRecordStateCallback?.onRecordStart()
            if (mLoopNeedStop.get()) {
                mLoopNeedStop.set(false)
                performStopLoop(true)
            }
//            }
        }

        /*override fun onRecorderConfigureFailed() {
            stopLooper()
            mRecordStateCallback?.onRecorderConfigureFailed()
        }*/

        override fun onRecordStarting() {
            Timber.e("onRecordStarting()")
            mRecordStarting.set(true)
            mRecordStateCallback?.onRecordStarting()
        }

        override fun onRecordStopping() {
            Timber.e("onRecordStopping()")
            mRecordStopping.set(true)
            mRecordStateCallback?.onRecordStopping()
        }

        private var mErrorCount = 0

        private var mPreErrorTime = 0L

        override fun onRecordError(errorCode: Int, errorType: Int) {
            Timber.e("onRecordError().errorCode = $errorCode, errorType = $errorType")
            if (errorCode == IRecorder.IRecordCallback.RECORDER_NOT_IDLE) {
                Timber.e("onRecordError().Recorder not idle, currentState = $errorType")
                return
            }
            mRecordStarting.set(false)
            mRecordStopping.set(false)
            mLoopNeedStop.set(false)
            when (errorCode) {
                IRecorder.IRecordCallback.CAMERA_RELEASED, IRecorder.IRecordCallback.CAMERA_IS_NULL -> {
                    mRecordCore.setNeedRestartLoop(true)
                    mRecordCore.stopRecord()
                    return
                }
                /*MediaRecorder.MEDIA_ERROR_SERVER_DIED, MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN, IRecorder.IRecordCallback.RECORD_STOP_ERROR   -> {
                    restartPreview()
                }*/
                IRecorder.IRecordCallback.RECORD_ERROR_TOO_SHORT -> {
                    restartPreview()
                    mErrorCount = 0
                    stopLooper()
                    mRecordStateCallback?.onRecordError(errorCode, errorType)
                    return
                }
                IRecorder.IRecordCallback.RECORD_ERROR_CONFIGURE_FAILED -> {
                    when (errorType) {
                        IRecorder.IRecordCallback.ERROR_CODE_CAMERA_SET_FAILED  -> {
                            restartPreview()
                        }
                        IRecorder.IRecordCallback.ERROR_CODE_FILE_WRITE_DENIED  -> {
                            restartPreview()
                            mErrorCount = 0
                            stopLooper()
                            mCameraManager?.stopRecord()
                            mRecordStateCallback?.onRecordError(errorCode, errorType)
                            return
                        }
                        else -> {
                            restartPreview()
                        }
                    }
                }
                else -> {
                    restartPreview()
                }
            }
            mRecordCore.setNeedRestartLoop(false)
            val currentErrorTime = SystemClock.elapsedRealtime()
            if ((currentErrorTime - mPreErrorTime) > 2000) {
                mErrorCount = 0
            }
            mPreErrorTime = currentErrorTime
            if (mErrorAutoStart && (++mErrorCount < 5)) {
//                mCameraManager?.stopRecord()
                Observable.timer(300, TimeUnit.MILLISECONDS)
                        .observeOn(mRecordScheduler)
                        .subscribe {
                            mRecordCore.stopRecord()
                            Timber.i("onRecordError().isLooping = ${mRecordCore.isLooping()}")
                            if (mRecordCore.isLooping()) {
                                startLooper(autoDelete = mAutoDelete)
                            }
                        }

            } else {
                mErrorCount = 0
                stopLooper()
                mCameraManager?.stopRecord()
                mRecordStateCallback?.onRecordError(errorCode, errorType)
            }
        }

        override fun onRecordStop(stopCode: Int) {
            Timber.e("onRecordStop()")
            mRecordStopping.set(false)
            mCameraManager?.stopRecord()
            mRecordStateCallback?.onRecordStop(stopCode)
        }

        override fun onRecorderFinished(file: File?) {
            restartPreview()
            mRecordStopping.set(false)
            Timber.e("onRecorderFinished().mAutoDelete = $mAutoDelete")
            if (mAutoDelete) {
                mAutoDeleteFileCount++
                Timber.e("File: ${file?.absolutePath}. mAutoDeleteFileCount = $mAutoDeleteFileCount. mAutoDeletePreFile = ${mAutoDeleteFilePre?.name}")
                if (mAutoDeleteFileCount > 1) {
                    mAutoDeleteFilePre?.apply {
                        delete()
                        MediaInfoUtil.deleteDatabase(mContext, absolutePath, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                    }
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

    interface IRecordLoopCallback: IRecorder.IRecordCallback {

        fun onLoopStart(startCode: Int = NORMAL)

        fun onLoopStop(stopCode: Int = NORMAL)

        fun onLoopError(errorCode: Int)

        companion object {
            const val NORMAL          = 0
            const val LOOP_AUTO_DELETE  = 0xF0
            const val STOP_CODE_LOOP_EXIST      = 0xF1
            const val STOP_CODE_LOOP_NOT_EXIST  = 0xF2

        }

        enum class Status {
            LOOP_START,
            RECORD_START,
            RECORD_STOP,
            LOOP_STOP,
            LOOP_ERROR_STOP,
            RECORD_ERROR_STOP
        }

    }

}