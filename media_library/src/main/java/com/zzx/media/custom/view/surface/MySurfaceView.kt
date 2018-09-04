package com.zzx.media.custom.view.surface

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.Camera
import android.util.AttributeSet
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.zzx.media.camera.v1.manager.Camera1Manager
import com.zzx.media.custom.view.camera.ISurfaceView
import java.util.concurrent.atomic.AtomicBoolean

/**@author Tomy
 * Created by Tomy on 2017/12/4.
 */
class MySurfaceView(context: Context, attributeSet: AttributeSet): SurfaceView(context, attributeSet), ISurfaceView<SurfaceHolder, Camera>, SurfaceHolder.Callback {

    init {
        holder.addCallback(this)
        initParams()
    }

    private var mCamera: Camera? = null

    private var mICameraManager: Camera1Manager? = null

    private val mPreview: AtomicBoolean = AtomicBoolean(false)

    private var mDisplayRotation = 0

    private var mSurfaceStateCallback: ISurfaceView.StateCallback<SurfaceHolder>? = null

    private var mPreviewSize: Size? = null

    override fun setStateCallback(stateCallback: ISurfaceView.StateCallback<SurfaceHolder>) {
        mSurfaceStateCallback = stateCallback
    }


    override fun setPreviewSize(width: Int, height: Int) {
        mPreviewSize = Size(width, height)
    }

    override fun setLayoutParams(width: Int, height: Int) {
    }

    override fun setRotation(rotation: Int) {
        mDisplayRotation = rotation
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        mSurfaceStateCallback?.onSurfaceSizeChange(holder, width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        mSurfaceStateCallback?.onSurfaceDestroyed(holder)
        release()
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        mSurfaceStateCallback?.onSurfaceCreate(holder)
        startPreview()
    }

    override fun initParams() {

    }

    override fun setCamera(camera: Camera) {
        mCamera = camera
    }

    override fun startPreview() {
        mICameraManager?.apply {
            setPreviewParams(mPreviewSize!!.width, mPreviewSize!!.height, ImageFormat.YV12)
            setOrientation(mDisplayRotation)
            startPreview(holder!!)
        }
    }

    override fun stopPreview() {
        mICameraManager?.stopPreview()
    }

    override fun release() {
        mICameraManager?.releaseCamera()
    }
}