package com.zzx.media.custom.view.surface

import android.content.Context
import android.hardware.Camera
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.zzx.media.custom.view.camera.ISurfaceView
import java.util.concurrent.atomic.AtomicBoolean

/**@author Tomy
 * Created by Tomy on 2017/12/4.
 */
class MySurfaceView(context: Context, attributeSet: AttributeSet): SurfaceView(context, attributeSet), ISurfaceView, SurfaceHolder.Callback {
    override fun setPreviewSize(width: Int, height: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setLayoutParams(width: Int, height: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    init {
        initParams()
    }

    private var mCamera: Camera? = null
    private val mPreview: AtomicBoolean = AtomicBoolean(false)

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        mCamera?.stopPreview()
        mCamera?.setPreviewDisplay(null)
        mCamera?.release()
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        mCamera?.setPreviewDisplay(holder)
        mCamera?.startPreview()
    }

    override fun initParams() {
        try {
            mCamera = Camera.open()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun setCamera(camera: Camera) {
        mCamera = camera
    }

    override fun startPreview() {

    }

    override fun stopPreview() {

    }

    override fun release() {

    }
}