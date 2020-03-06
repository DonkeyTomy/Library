package com.zzx.media.custom.view.opengl.egl

import android.graphics.Bitmap
import android.opengl.EGL14
import android.opengl.GLES20
import timber.log.Timber
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGL10

/**@author Tomy
 * Created by Tomy on 2019/11/9.
 * Common base class for EGL Surface.
 * <p>
 *     There can be multiple surfaces associated with a single context.
 *     每个实例代表一个EGLSurface.
 *     可以传入不同的surface(SurfaceHolder/SurfaceTexture)通过同一个EGLCore(EGLContext)来创建不同的EGLSurface.
 *
 */
open class EGLSurfaceBase<eglContext, eglSurface, eglConfig>(protected var mEGLCore: EGLCore<eglContext, eglSurface, eglConfig>, var isEGL10: Boolean) {

    private var mEGLSurface = (if (isEGL10) EGL10.EGL_NO_SURFACE else EGL14.EGL_NO_SURFACE) as eglSurface

    private val EGL_NO_SURFACE = if (isEGL10) EGL10.EGL_NO_SURFACE else EGL14.EGL_NO_SURFACE

    private var mWidth  = -1
    private var mHeight = -1

    fun createWindowSurface(surface: Any): eglSurface {
        Timber.i("createWindowSurface. mEGLSurface = $mEGLSurface")
        if (mEGLSurface != EGL_NO_SURFACE) {
            throw RuntimeException("surface already created")
        }
        mEGLSurface = mEGLCore.createWindowSurface(surface)
        Timber.i("mEGLSurface = $mEGLSurface")
        return mEGLSurface
    }

    fun getWindowSurface(): eglSurface {
        return mEGLSurface
    }

    fun createOffscreenSurface(width: Int, height: Int) {
        if (checkSurface()) {
            mEGLSurface = mEGLCore.createOffscreenSurface(width, height)
            mWidth  = width
            mHeight = height
        }
    }

    fun getSurfaceWidth(): Int {
        if (mWidth < 0) {
            mWidth = mEGLCore.querySurface(mEGLSurface, EGL14.EGL_WIDTH)
        }
        return mWidth
    }

    fun getSurfaceHeight(): Int {
        if (mHeight < 0) {
            mHeight = mEGLCore.querySurface(mEGLSurface, EGL14.EGL_HEIGHT)
        }
        return mHeight
    }

    fun releaseEglSurface() {
        Timber.i("releaseEglSurface")
        mEGLCore.releaseSurface(mEGLSurface)
        mEGLSurface = (if (isEGL10) EGL10.EGL_NO_SURFACE else EGL14.EGL_NO_SURFACE) as eglSurface
        mWidth  = -1
        mHeight = -1
    }

    fun makeNothingCurrent() {
        Timber.i("makeNothingCurrent")
        mEGLCore.makeNothingCurrent()
    }

    open fun release() {
        releaseEglSurface()
    }

    fun makeCurrent() {
//        Timber.w("makeCurrent")
        mEGLCore.makeCurrent(mEGLSurface)
    }

    /**
     * 用自身的EGLContext和Surface来绘画来自[readSurface]的输入源.
     * @param readSurface EGLSurfaceBase
     */
    fun makeCurrentReadFrom(readSurface: EGLSurfaceBase<eglContext, eglSurface, eglConfig>) {
        mEGLCore.makeCurrent(mEGLSurface, readSurface.mEGLSurface)
    }

    fun swapBuffers(): Boolean {
//        Timber.w("swapBuffers")
        return mEGLCore.swapBuffers(mEGLSurface)
    }

    fun setPresentationTime(timeInNano: Long) {
        mEGLCore.setPresentationTime(mEGLSurface, timeInNano)
    }

    /**
     * 保存当前帧.要确保自身的EGLSurface正在使用.
     * @param file File
     */
    fun saveFrameToFile(file: File) {
        if (!mEGLCore.isCurrent(mEGLSurface)) {
            throw RuntimeException("EGL Context/Surface is not current")
        }

        /**
         * [GLES20.glReadPixels]方法采用'大端存储的RGBA'填满一个长方形Byte数组.
         * 而Bitmap的构造方法采用的是'小端存储的ARGB'
         */

        val width   = getSurfaceWidth()
        val height  = getSurfaceHeight()
        val buf = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.LITTLE_ENDIAN)
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf)
        buf.rewind()
        var bos: BufferedOutputStream? = null
        try {
            bos = BufferedOutputStream(FileOutputStream(file))
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bmp.apply {
                copyPixelsFromBuffer(buf)
                compress(Bitmap.CompressFormat.PNG, 90, bos)
                recycle()
            }
        } catch (e: Exception) {

        } finally {
            try {
                bos?.close()
            } catch (e: Exception) {}
        }
    }

    fun checkSurface(): Boolean {
        if (mEGLSurface != EGL14.EGL_NO_SURFACE) {
            throw RuntimeException("surface already created")
        }
        return true
    }

}