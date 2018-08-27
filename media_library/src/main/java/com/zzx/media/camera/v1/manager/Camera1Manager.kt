package com.zzx.media.camera.v1.manager

import android.hardware.Camera
import android.util.Size
import android.view.Surface
import android.view.SurfaceHolder
import com.zzx.media.camera.ICameraManager
import com.zzx.media.recorder.IRecorder
import java.util.concurrent.atomic.AtomicBoolean

/**@author Tomy
 * Created by Tomy on 2018/4/5.
 */
class Camera1Manager: ICameraManager<SurfaceHolder, Camera> {

    private var mCamera: Camera? = null

    private var mCameraId: Int = 0

    private var mCameraClosed = AtomicBoolean(false)

    private val mObject = Object()

    private var mPreviewSurface: SurfaceHolder? = null


    private var mVideoRecorder: IRecorder? = null

    override fun openFrontCamera() {
        for (i in 0 until getCameraCount()) {
            val info = Camera.CameraInfo()
            Camera.getCameraInfo(i, info)
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mCameraId = i
                mCamera = Camera.open(i)
                return
            }
        }
    }

    override fun openBackCamera() {
        for (i in 0 until getCameraCount()) {
            val info = Camera.CameraInfo()
            Camera.getCameraInfo(i, info)
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mCameraId = i
                mCamera = Camera.open(i)
                return
            }
        }
    }

    override fun openExternalCamera() {
    }

    /**
     * 只设置了预览 Surface ,但是不会调用 [startPreview].
     * 此方法跟[startPreview]共同使用由自身决定何时启动预览.
     * */
    override fun setPreviewSurface(surfaceTexture: SurfaceHolder) {
        mPreviewSurface = surfaceTexture
    }

    /**
     * 此方法调用之前必须先调用[setPreviewSurface],自行决定决定何时启动预览.
     * */
    override fun startPreview() {
        mCamera?.startPreview()
    }

    /**
     * 等价于[setPreviewSurface]+[startPreview].
     * 设置完预览界面后即可启动预览.
     * */
    override fun startPreview(surface: SurfaceHolder) {
        setPreviewSurface(surface)
    }

    override fun stopPreview() {
        mCamera?.stopPreview()
    }

    /**
     * 开始录像
     * */
    override fun startRecordPreview(surface: Surface) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setIRecorder(recorder: IRecorder) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * 停止录像
     * */
    override fun stopRecord() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun closeCamera() {
        mCamera?.release()
    }

    override fun releaseCamera() {
        mCamera?.release()
    }

    override fun getCameraCount(): Int {
        return Camera.getNumberOfCameras()
    }

    override fun getSupportPreviewSizeList(): Array<Size> {
        val parameters = mCamera?.parameters
        val list = parameters!!.supportedPreviewSizes
        return Array(list.size) {
            Size(list[it].width, list[it].height)
        }
    }

    override fun getSupportPreviewFormatList(): Array<Int> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setPreviewParams(width: Int, height: Int, format: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSupportCaptureSizeList(): Array<Size> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSupportCaptureFormatList(): Array<Int> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setCaptureParams(width: Int, height: Int, format: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSupportRecordSizeList(): Array<Size> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSensorOrientation(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun takePicture(callback: ICameraManager.PictureDataCallback) {
        mCamera?.takePicture(null, null, Camera.PictureCallback {
            data, _ ->
            callback.onCaptureFinished(data)
        })
    }

    override fun takePictureBurst(count: Int, callback: ICameraManager.PictureDataCallback) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setPictureCallback(callback: ICameraManager.PictureDataCallback) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setRecordPreviewCallback(callback: ICameraManager.RecordPreviewReady) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun takePicture() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun takePictureBurst(count: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setOrientation(rotation: Int) {
        mCamera?.setDisplayOrientation(rotation)
    }

    /**
     * 获得摄像头设备.
     * @see Camera
     * */
    override fun getCameraDevice(): Camera? {
        return mCamera
    }

    override fun setStateCallback(stateCallback: ICameraManager.CameraStateCallback<Camera>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}