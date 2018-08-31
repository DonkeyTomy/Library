package com.zzx.media.recorder.video

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraDevice
import android.media.CamcorderProfile
import com.zzx.media.R
import com.zzx.utils.file.FileUtil
import com.zzx.media.camera.ICameraManager
import com.zzx.media.recorder.IRecorder
import com.zzx.utils.rxjava.FlowableUtil
import com.zzx.media.utils.MediaInfoUtils
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
class RecorderLooper(var mContext: Context, @IRecorder.FLAG flag: Int) {

    var mRecorder: IRecorder = VideoRecorder().apply {
        init()
    }

    init {
        mRecorder.setFlag(flag)
    }


    private var mCameraManager: ICameraManager<SurfaceTexture, CameraDevice>? = null

    private var mRecordDuration = 20

    private var mDirPath: String? = null

    private var mRotation = 0

    private var mRecording = AtomicBoolean(false)

    private var mOutputFile: File? = null

    private var mFileList: ArrayList<File>? = null


    fun setCameraManager(cameraManager: ICameraManager<SurfaceTexture, CameraDevice>?) {
        mCameraManager = cameraManager
        mCameraManager?.apply {
            setRecordPreviewCallback(object : ICameraManager.RecordPreviewReady {
                override fun onRecordPreviewReady() {
                    mRecorder.startRecord()
                }
            })
            (mRecorder as VideoRecorder).setSensorOrientationHint(getSensorOrientation())
        }
    }

    private fun setupRecorder(quality: Int) {
        mOutputFile = File(mDirPath, MediaInfoUtils.getTmpFileName())
        mRecorder.setOutputFile(mOutputFile!!)
        (mRecorder as VideoRecorder).setProperty(quality)
    }

    fun getOutputFile(): File? = mOutputFile

    fun setRecordDuration(duration: Int) {
        mRecordDuration = if (duration >= 0) duration else 0
    }

    fun setRotation(rotation: Int) {
        mRotation = rotation
        (mRecorder as VideoRecorder).setRotation(mRotation)
    }

    fun setDirPath(dirPath: String) {
        mDirPath = dirPath
    }

    fun setFlag(@IRecorder.FLAG flag: Int) {
        mRecorder.setFlag(flag)
    }

    fun setRecordCallback(callback: IRecorder.IRecordCallback?) {
        mRecorder.setRecordCallback(callback)
    }

    fun startRecord():Boolean {
        setupRecorder(CamcorderProfile.QUALITY_720P)
        try {
            return if (mRecorder.checkState() == IRecorder.State.PREPARED) {
                mCameraManager!!.startRecordPreview(mRecorder.getSurface()!!)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun stopRecord() {
        mRecorder.reset()
        MediaInfoUtils.tmpFile2Video(mOutputFile)
    }

    fun release() {
        stopLooper()
        mRecorder.release()
        setCameraManager(null)
        setRecordCallback(null)
    }

    fun isRecording(): Boolean {
        Timber.e("isRecording: ${mRecording.get()}")
        return mRecording.get()
    }

    fun stopLooper() {
        Timber.e("stopLooper.recording = ${mRecording.get()}")
        if (!mRecording.get())
            return
        mRecording.set(false)
        mCheckLoopDisposable?.dispose()
        mCheckLoopDisposable    = null
        mRecordLoopDisposable?.dispose()
        mRecordLoopDisposable   = null
        stopRecord()
        mCameraManager?.startPreview()
    }

    fun recordSection() {
        if (mRecording.get()) {
            mRecording.set(false)
            startLooper()
        }
    }

    fun cancelTimer() {
        if (mRecording.get()) {
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
        if (mRecording.get()) {
            return
        }
        mRecording.set(true)
        check()
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

}