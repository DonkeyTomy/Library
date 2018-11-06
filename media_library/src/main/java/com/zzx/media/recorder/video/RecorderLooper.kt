package com.zzx.media.recorder.video

import android.content.Context
import android.hardware.Camera
import android.media.CamcorderProfile
import android.view.Surface
import com.zzx.media.camera.ICameraManager
import com.zzx.media.camera.v1.manager.Camera1Manager
import com.zzx.media.camera.v2.manager.Camera2Manager
import com.zzx.media.recorder.IRecorder
import com.zzx.media.utils.FileNameUtils
import com.zzx.utils.file.FileUtil
import com.zzx.utils.rxjava.FlowableUtil
import com.zzx.utils.zzx.DeviceUtils
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**@author Tomy
 * Created by Tomy on 2018/6/11.
 */
class RecorderLooper<surface, camera>(var mContext: Context, @IRecorder.FLAG flag: Int) {

    var mRecorder: IRecorder = VideoRecorder(false)

    init {
        mRecorder.setFlag(flag)
        mRecorder.setRecordCallback(RecordStateCallback())
    }


    private var mCameraManager: ICameraManager<surface, camera>? = null

    private var mRecordDuration = 20

    private var mDirPath: String? = null

    private var mRotation = 0

    private var mLooping = AtomicBoolean(false)

    private var mRecording = AtomicBoolean(false)

    private var mOutputFile: File? = null

    private var mFileList: ArrayList<File>? = null

    private var mRecordStateCallback: IRecorder.IRecordCallback? = null

    private var mQuality = CamcorderProfile.QUALITY_720P


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

    fun setQuality(quality: Int) {
        mQuality = quality
    }

    private fun setupRecorder() {
        setRecordHintRotation()
        mOutputFile = File(mDirPath, FileNameUtils.getTmpVideoName("${DeviceUtils.getUserNum(mContext)}_"))
        mRecorder.setOutputFile(mOutputFile!!)
        mRecorder.setProperty(mQuality)
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

    fun setDirPath(dirPath: String) {
        mDirPath = dirPath
    }

    fun setFlag(@IRecorder.FLAG flag: Int) {
        mRecorder.setFlag(flag)
    }

    fun setRecordCallback(callback: IRecorder.IRecordCallback?) {
        mRecordStateCallback = callback
    }

    fun startRecord():Boolean {
        Timber.e("startRecord")
        mRecording.set(true)
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

    fun stopRecord() {
        Timber.e("stopRecord")
        mRecording.set(false)
        mRecorder.reset()
        mCameraManager?.stopRecord()
//        FileNameUtils.tmpFile2Video(mOutputFile)
    }

    fun release() {
        stopLooper()
        mRecorder.release()
        setCameraManager(null)
        setRecordCallback(null)
    }

    fun isRecording(): Boolean {
        Timber.e("isRecording: ${mLooping.get()}")
        return mLooping.get() || mRecording.get()
    }

    fun stopLooper() {
        Timber.e("stopLooper.recording = ${mLooping.get()}")
        if (!mLooping.get())
            return
        mLooping.set(false)
        mCheckLoopDisposable?.dispose()
        mCheckLoopDisposable    = null
        mRecordLoopDisposable?.dispose()
        mRecordLoopDisposable   = null
        stopRecord()
        mCameraManager?.startPreview()
    }

    fun recordSection() {
        if (mLooping.get()) {
            mLooping.set(false)
            startLooper()
        }
    }

    fun cancelTimer() {
        if (mLooping.get()) {
            mRecordLoopDisposable?.dispose()
            mRecordLoopDisposable = null
        }
    }

    private var mRecordLoopDisposable: Disposable?  = null
    private var mCheckLoopDisposable: Disposable?   = null

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

    fun startLooper() {
        if (mLooping.get()) {
            return
        }
        mLooping.set(true)
//        check()
        Observable.interval(0, mRecordDuration.toLong(),  TimeUnit.SECONDS)
                .observeOn(Schedulers.newThread())
                .subscribe(
                        {
                            stopRecord()
                            startRecord()
                            Timber.e("onNext. Thread = ${Thread.currentThread().name}.\nRecordDuration = $mRecordDuration")
                        },
                        {
                            it.printStackTrace()
//                            Timber.e("onError. Thread = ${Thread.currentThread().name}")
                        },
                        {
//                            Timber.e("onComplete. Thread = ${Thread.currentThread().name}")
                        },
                        {
                            mRecordLoopDisposable = it
                        }
                )
    }

    fun check() {
        FlowableUtil.setMainThreadMapBackground<Unit>(
                Function {
                    checkNeedDelete()
                }, Consumer {
                    Observable.interval(0, 10L, TimeUnit.SECONDS)
                            .observeOn(Schedulers.io())
                            .doOnDispose {
                                Timber.e("check Disposed")
                            }
                            .subscribe(
                                    {
                                        checkNeedDelete()
                                    },{},{},
                                    {
                                        mCheckLoopDisposable = it
                                    }
                            )
                }
        )

    }

    fun checkNeedDelete() {
        val freeSpace = FileUtil.getDirFreeSpaceByMB(FileUtil.getExternalStoragePath(mContext))
        Timber.e("currentFreeSpace = $freeSpace")
        if (freeSpace <= 500) {
            if (mFileList?.size ?: 0 == 0) {
                val dirList = FileUtil.sortDirTime(File(mDirPath!!).parentFile)
                for (dir in dirList) {
                    mFileList   = FileUtil.sortDirTime(dir)
                    Timber.e("delete Dir ${dir.path}.size = ${mFileList?.size}")
                    if (mFileList!!.isEmpty()) {
                        FileUtil.deleteFile(dir)
                    } else {
                        break
                    }
                }
            }
            Timber.e("mFileList.size = ${mFileList?.size}")
            for (file in mFileList!!) {
                Timber.e("fileName = ${file.absolutePath}")
                if (file.absolutePath != mOutputFile!!.absolutePath) {
                    FileUtil.deleteFile(file)
                    mFileList!!.remove(file)
                    break
                }
            }
        }
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
            mRecordStateCallback?.onRecordStart()
        }

        override fun onRecorderConfigureFailed() {
            mRecordStateCallback?.onRecorderConfigureFailed()
        }

        override fun onRecordError() {
            mRecordStateCallback?.onRecordError()
        }

        override fun onRecorderFinished(file: File?) {
            mRecordStateCallback?.onRecorderFinished(file)
        }

        override fun onRecordPause() {
        }

        override fun onRecordResume() {
        }

    }

}