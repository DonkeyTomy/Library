package com.zzx.media.custom.view.glsurface

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.zzx.media.camera.ICameraManager
import com.zzx.media.custom.view.camera.ISurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**@author Tomy
 * Created by Tomy on 2017/8/23.
 */
class MyGLSurfaceView(context: Context?, attrs: AttributeSet?) : GLSurfaceView(context, attrs), ISurfaceView<SurfaceTexture, Camera> {

    var mCamera: Camera? = null

    private var mSurfaceTexture: SurfaceTexture? = null

    init {
        setEGLContextClientVersion(2)
        renderMode  = RENDERMODE_CONTINUOUSLY
        setRenderer(MyRender())
    }

    fun createTextureID(): Int {
        val texture = intArrayOf(1)
        //创建纹理
        GLES20.glGenTextures(1, texture, 0)
        //将此纹理绑定到外部纹理上
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0])
        //设置纹理过滤参数
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST.toFloat())
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE.toFloat())
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE.toFloat())
        //接触绑定纹理
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
        return texture[0]
    }

    fun initSurfaceTexture(): SurfaceTexture? {
        mSurfaceTexture = SurfaceTexture(createTextureID())
        mSurfaceTexture?.setOnFrameAvailableListener {
            requestRender()
        }
        return mSurfaceTexture
    }


    override fun setRotation(rotation: Int) {
    }

    override fun setStateCallback(stateCallback: ISurfaceView.StateCallback<SurfaceTexture>) {
    }

    override fun setPreviewSize(width: Int, height: Int) {

    }

    override fun setLayoutParams(width: Int, height: Int) {

    }

    override fun initParams() {

    }

    override fun startPreview() {

    }

    override fun stopPreview() {

    }

    override fun release() {
        mSurfaceTexture?.release()
    }

    override fun setCameraManager(cameraManager: ICameraManager<SurfaceTexture, Camera>) {
    }

    override fun setCamera(camera: Camera) {
        mCamera = camera
    }


    inner class MyRender : Renderer {


        override fun onDrawFrame(gl: GL10) {
            //Redraw background color
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            mSurfaceTexture?.apply {
                updateTexImage()
            }
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)
        }

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            //Set the background frame color
            GLES20.glClearColor(0f, 0f, 0f, 1f)
        }

    }

}