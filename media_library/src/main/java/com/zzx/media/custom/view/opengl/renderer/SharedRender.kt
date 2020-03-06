package com.zzx.media.custom.view.opengl.renderer

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.*
import android.os.Build
import android.os.Handler
import android.os.Message
import android.support.annotation.RequiresApi
import android.view.Surface
import com.zzx.media.custom.view.opengl.egl.FullFrameRect
import com.zzx.media.custom.view.opengl.egl.OffscreenEGLSurface
import com.zzx.media.custom.view.opengl.egl.Texture2DProgram
import com.zzx.media.custom.view.opengl.egl.WindowEGLSurface
import com.zzx.media.custom.view.opengl.egl14.EGL14Core
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

/**@author Tomy
 * Created by Tomy on 2020/3/6.
 */
class SharedRender : SurfaceTexture.OnFrameAvailableListener {

    private var mEGLCore: EGL14Core

    private val mFullFrameRect: FullFrameRect

    private var mTextureID: Int

    private var mSurfaceTexture: SurfaceTexture

    private val mTmpMatrix = FloatArray(16)

    private var mPreviewWidth   = PREVIEW_WIDTH
    private var mPreviewHeight  = PREVIEW_HEIGHT

    private val mDisplaySurface: OffscreenEGLSurface<EGLContext, EGLSurface, EGLConfig>

    private val mSurfaceMap by lazy {
        ConcurrentHashMap<Int, WindowEGLSurface<EGLContext, EGLSurface, EGLConfig>>()
    }

    private val mHandler by lazy { MainHandler(this) }

    constructor(context: Context, sharedContext: EGLContext = EGL14.EGL_NO_CONTEXT) {
        mEGLCore = EGL14Core(sharedContext)
        mDisplaySurface = OffscreenEGLSurface(mEGLCore, PREVIEW_WIDTH, PREVIEW_HEIGHT, false)
        mDisplaySurface.makeCurrent()
        mFullFrameRect = FullFrameRect(Texture2DProgram(Texture2DProgram.ProgramType.TEXTURE_EXT, context))
        mTextureID  = mFullFrameRect.createTextureObject()
        mSurfaceTexture = SurfaceTexture(mTextureID)
    }

    fun getSurfaceTexture() = mSurfaceTexture

    fun startRender() {
        mSurfaceTexture.setOnFrameAvailableListener(this)
        GLES20.glViewport(0, 0, mPreviewWidth, mPreviewHeight)
    }

    fun stopRender() {
        mSurfaceTexture.setOnFrameAvailableListener(null)
    }

    fun registerPreviewSurface(surface: Any) {
        val hashCode = System.identityHashCode(surface)
        if (mSurfaceMap.containsKey(hashCode)) {
            return
        }
        Timber.w("registerPreviewSurface.surface = $surface")
        mSurfaceMap[hashCode] = WindowEGLSurface(mEGLCore, surface, true)
    }

    fun unregisterPreviewSurface(surface: Any) {
        mSurfaceMap.remove(System.identityHashCode(surface))
    }

    fun renderFrame() {
        Timber.i("renderFrame()")
        mSurfaceTexture.updateTexImage()
        mSurfaceTexture.getTransformMatrix(mTmpMatrix)
        mSurfaceMap.forEach { (_: Int, surface: WindowEGLSurface<EGLContext, EGLSurface, EGLConfig>) ->
            surface.makeCurrent()
            mFullFrameRect.drawFrame(mTextureID, mTmpMatrix)
            surface.swapBuffers()
        }
    }

    fun getEGLContext(): EGLContext {
        return mEGLCore.getCurrentContext()
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        mHandler.sendEmptyMessage(0)
    }

    companion object {

        const val PREVIEW_WIDTH     = 1280
        const val PREVIEW_HEIGHT    = 720

    class MainHandler(sharedRender: SharedRender): Handler() {
        private val mWeakContext = WeakReference<SharedRender>(sharedRender)

        override fun handleMessage(msg: Message) {
            mWeakContext.get()?.renderFrame()
        }

    }
}

}