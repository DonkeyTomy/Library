package com.zzx.media.custom.view.opengl.renderer

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.*
import android.os.Handler
import android.os.Message
import com.zzx.media.custom.view.opengl.egl.FullFrameRect
import com.zzx.media.custom.view.opengl.egl.OffscreenEGLSurface
import com.zzx.media.custom.view.opengl.egl.Texture2DProgram
import com.zzx.media.custom.view.opengl.egl.WindowEGLSurface
import com.zzx.media.custom.view.opengl.egl14.EGL14Core
import com.zzx.media.values.TAG
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.*
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

    private var mFrameRenderListener: OnFrameRenderListener? = null

    private val mDisplaySurface: OffscreenEGLSurface<EGLContext, EGLSurface, EGLConfig>

    private val mSurfaceMap by lazy {
        ConcurrentHashMap<Int, WindowEGLSurface<EGLContext, EGLSurface, EGLConfig>>()
    }

    private val mRefreshSet by lazy {
        TreeSet<Int>()
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

    fun setOnFrameRenderListener(listener: OnFrameRenderListener?) {
        Timber.tag(TAG.SURFACE_ENCODER).i("setOnFrameRenderListener -- $listener")
        mFrameRenderListener = listener
    }

    fun getSurfaceTexture() = mSurfaceTexture

    fun startRender() {
        mSurfaceTexture.setOnFrameAvailableListener(this)
        GLES20.glViewport(0, 0, mPreviewWidth, mPreviewHeight)
    }

    fun setRenderSize(width: Int, height: Int) {
        mPreviewWidth   = width
        mPreviewHeight  = height
    }

    fun stopRender() {
        mSurfaceTexture.setOnFrameAvailableListener(null)
    }

    fun registerPreviewSurface(surface: Any, needCallback: Boolean = false) {
        val hashCode = System.identityHashCode(surface)
        if (mSurfaceMap.containsKey(hashCode)) {
            return
        }
        if (needCallback) {
            mRefreshSet.add(hashCode)
        }
        Timber.w("registerPreviewSurface.surface = $surface")
        mSurfaceMap[hashCode] = WindowEGLSurface(mEGLCore, surface, false)
    }

    /**
     * @return 已注册渲染的窗口个数.
     */
    fun getRegisterSurfaceCount(): Int {
        return mSurfaceMap.size
    }

    /**
     * @return true 若有窗口注册渲染;反之false.
     */
    fun isRenderBusy(): Boolean {
        val registerCount = getRegisterSurfaceCount()
        Timber.tag(TAG.RENDER).w("isRenderBusy.registerCount = $registerCount")
        return registerCount > 0
    }

    fun unregisterPreviewSurface(surface: Any) {
        Timber.tag(TAG.RENDER).w("unregisterPreviewSurface")
        val hasCode = System.identityHashCode(surface)
        mSurfaceMap[hasCode]?.apply {
            release()
        }
        mRefreshSet.remove(hasCode)
        if (mRefreshSet.isEmpty()) {
            mFrameRenderListener = null
        }
        mSurfaceMap.remove(hasCode)
    }

    fun renderFrame() {
//        Timber.i("renderFrame()")
        try {
            mSurfaceTexture.updateTexImage()
            mSurfaceTexture.getTransformMatrix(mTmpMatrix)
            mSurfaceMap.forEach { (id: Int, surface: WindowEGLSurface<EGLContext, EGLSurface, EGLConfig>) ->
                surface.makeCurrent()
                mFullFrameRect.drawFrame(mTextureID, mTmpMatrix)
                mFrameRenderListener?.apply {
                    Timber.tag(TAG.RENDER).i("renderFrame")
                    if (mRefreshSet.contains(id)) {
                        onFrameSoon(id)
                    }
                }
                surface.swapBuffers()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getEGLContext(): EGLContext {
        return mEGLCore.getCurrentContext()
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        mHandler.sendEmptyMessage(0)
    }

    interface OnFrameRenderListener {
        fun onFrameSoon(id: Int)
    }

    companion object {

        const val PREVIEW_WIDTH     = 480
        const val PREVIEW_HEIGHT    = 270
//        const val PREVIEW_HEIGHT    = 360

    class MainHandler(sharedRender: SharedRender): Handler() {
        private val mWeakContext = WeakReference<SharedRender>(sharedRender)

        override fun handleMessage(msg: Message) {
            mWeakContext.get()?.renderFrame()
        }

    }
}

}