package com.zzx.media.custom.view.glsurface

import android.content.Context
import android.hardware.Camera
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.zzx.media.custom.view.camera.ISurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**@author Tomy
 * Created by Tomy on 2017/8/23.
 */
class MyGLSurfaceView(context: Context?, attrs: AttributeSet?) : GLSurfaceView(context, attrs), ISurfaceView {

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
    }

    var mCamera: Camera? = null

    init {
        setEGLContextClientVersion(2)
        renderMode  = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        setRenderer(MyRender())
    }

    override fun setCamera(camera: Camera) {
        mCamera = camera
    }

    fun createTextureID(): Int {
        val texture = intArrayOf(1)
        GLES20.glGenTextures(1, texture, 0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0])
        return 1
    }

    class MyRender : GLSurfaceView.Renderer {


        override fun onDrawFrame(gl: GL10?) {
            //Redraw background color
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
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