package com.zzx.media.custom.view.texture

import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.GLUtils
import javax.microedition.khronos.egl.*

/**@author Tomy
 * Created by Tomy on 2017/9/11.
 */
abstract class TextureSurfaceRenderer(surfaceTexture: SurfaceTexture, width: Int, height: Int):  Runnable {

    var mSurfaceTexture: SurfaceTexture = surfaceTexture
    val mWidth: Int = width
    val mHeight: Int = height
    var mRunning    = true

    init {
        mRunning    = true
        val thread  = Thread(this)
        thread.start()
    }

    lateinit var mEGL: EGL11
    lateinit var mEGLContext: EGLContext
    var mEGLSurface: EGLSurface? = null
    lateinit var mEGLDisplay: EGLDisplay

    override fun run() {
        initEGL()
        initEGLComponents()
        while (mRunning) {
            if (draw()) {
                mEGL.eglSwapBuffers(mEGLDisplay, mEGLSurface)
            }
        }
        deinitEGLComponents()
        deinitEGL()
    }

    private fun initEGL() {
        mEGL    = EGLContext.getEGL() as EGL11
        mEGLDisplay = mEGL.eglGetDisplay(EGL11.EGL_DEFAULT_DISPLAY)
        val version = IntArray(2)
        mEGL.eglInitialize(mEGLDisplay, version)
        val eglConfig   = chooseEglConfig()
        mEGLSurface = mEGL.eglCreateWindowSurface(mEGLDisplay, eglConfig, mSurfaceTexture, null)
        mEGLContext = createContext(mEGL, mEGLDisplay, eglConfig)

        try {
            if (mEGLSurface == null || mEGLSurface == EGL10.EGL_NO_SURFACE) {
                throw RuntimeException("EGL error: ${GLUtils.getEGLErrorString(mEGL.eglGetError())}")
            }

            if (!mEGL.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext)) {
                throw RuntimeException("GL Make current Error: ${GLUtils.getEGLErrorString(mEGL.eglGetError())}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deinitEGL() {
        mEGL.eglMakeCurrent(mEGLDisplay, EGL11.EGL_NO_SURFACE, EGL11.EGL_NO_SURFACE, EGL11.EGL_NO_CONTEXT)
        mEGL.eglDestroySurface(mEGLDisplay, mEGLSurface)
        mEGL.eglDestroyContext(mEGLDisplay, mEGLContext)
        mEGL.eglTerminate(mEGLDisplay)
    }

    abstract fun draw(): Boolean

    abstract fun initEGLComponents()

    abstract fun deinitEGLComponents()

    abstract fun getSurfaceTexture(): SurfaceTexture?

    /**
     * 位当前渲染的API创建一个渲染上下文
     * @return a handle to the context
     * */
    private fun createContext(egl: EGL11, eglDisplay: EGLDisplay?, config: EGLConfig?): EGLContext {
        val attrs   = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL10.EGL_NONE)
        return egl.eglCreateContext(eglDisplay, config, EGL10.EGL_NO_CONTEXT, attrs)
    }

    /**
     * 选择一个你所期望的配置.
     * @return 一个与期望最接近的EGL帧缓存配置.
     * */
    private fun chooseEglConfig(): EGLConfig? {
        val configsCount    = IntArray(1)
        val configs = emptyArray<EGLConfig>()
        val attributes  = getAttributes()
        val confSize    = 1
        if (!mEGL.eglChooseConfig(mEGLDisplay, attributes, configs, confSize, configsCount)) {
            throw IllegalArgumentException("Failed to choose config: ${GLUtils.getEGLErrorString(mEGL.eglGetError())}")
        } else if (configsCount[0] > 0) {
            return configs[0]
        }
        return null
    }

    /**
     * 构造你所期望的绘制需要的特性配置,如RGB的位数
     * */
    fun getAttributes(): IntArray {
        return intArrayOf(EGL11.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL11.EGL_RED_SIZE, 8,
                EGL11.EGL_GREEN_SIZE, 8,
                EGL11.EGL_BLUE_SIZE, 8,
                EGL11.EGL_ALPHA_SIZE, 8,
                EGL11.EGL_STENCIL_SIZE, 0,
                EGL11.EGL_NONE)
    }

}