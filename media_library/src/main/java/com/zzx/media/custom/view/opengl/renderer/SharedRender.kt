package com.zzx.media.custom.view.opengl.renderer

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.*
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.util.Size
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
class SharedRender(var context: Context, var sharedContext: EGLContext = EGL14.EGL_NO_CONTEXT) : SurfaceTexture.OnFrameAvailableListener {

    private lateinit var mEGLCore: EGL14Core

    private lateinit var mFullFrameRect: FullFrameRect

    private var mTextureID = 0

    private lateinit var mSurfaceTexture: SurfaceTexture

    private val mTmpMatrix = FloatArray(16)

    private var mPreviewWidth   = PREVIEW_WIDTH
    private var mPreviewHeight  = PREVIEW_HEIGHT

    private var mFrameRenderListener: OnFrameRenderListener? = null

    private lateinit var mDisplaySurface: OffscreenEGLSurface<EGLContext, EGLSurface, EGLConfig>

    private val mSurfaceMap by lazy {
        ConcurrentHashMap<Int, WindowEGLSurface<EGLContext, EGLSurface, EGLConfig>>()
    }

    private val mSizeMap by lazy {
        ConcurrentHashMap<Int, Size>()
    }

    private val mRefreshSet by lazy {
        TreeSet<Int>()
    }

    private val mHandlerThread = HandlerThread(SharedRender::class.java.name)

    private val mHandler by lazy { MainHandler(this, mHandlerThread.looper) }

    init {
        mHandlerThread.start()
        mHandler.sendEmptyMessage(INIT)
    }

    fun init() {
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

    /*fun setRenderSize(width: Int, height: Int) {
        mPreviewWidth   = width
        mPreviewHeight  = height
    }*/

    fun stopRender() {
        mSurfaceTexture.setOnFrameAvailableListener(null)
    }

    /**
     * @see unregisterPreviewSurface
     * @param surface Any
     * @param needCallback Boolean
     * @param width Int
     * @param height Int
     */
    fun registerPreviewSurface(surface: Any, width: Int, height: Int, needCallback: Boolean = false, surfaceNeedRelease: Boolean = false) {
        synchronized(mSurfaceMap) {
            val hashCode = System.identityHashCode(surface)
            Timber.w("registerPreviewSurface.hashCode = $hashCode")
            mSizeMap[hashCode] = Size(width, height)
            if (mSurfaceMap.containsKey(hashCode)) {
                return
            }
            if (needCallback) {
                mRefreshSet.add(hashCode)
            }
            Timber.w("registerPreviewSurface.surface = $surface")
            mSurfaceMap[hashCode] = WindowEGLSurface(mEGLCore, surface, surfaceNeedRelease)
        }
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
        unregisterPreviewSurface(System.identityHashCode(surface))
    }

    fun unregisterPreviewSurface(id: Int) {
        synchronized(mSurfaceMap) {
            Timber.tag(TAG.RENDER).w("unregisterPreviewSurface.id = $id")
            mSizeMap.remove(id)
            mSurfaceMap[id]?.apply {
                release()
            }
            mRefreshSet.remove(id)
            if (mRefreshSet.isEmpty()) {
                mFrameRenderListener = null
            }
            mSurfaceMap.remove(id)
        }
    }

    private var releaseId = 0

    fun renderFrame() {
//        Timber.i("renderFrame()")
        try {
//            FlowableUtil.setMainThread(Consumer {
                mSurfaceTexture.updateTexImage()
//            })
            mSurfaceTexture.getTransformMatrix(mTmpMatrix)
            synchronized(mSurfaceMap) {
                mSurfaceMap.forEach { (id: Int, surface: WindowEGLSurface<EGLContext, EGLSurface, EGLConfig>) ->
                    releaseId = id
                    surface.makeCurrent()
                    mSizeMap[id]?.apply {
                        GLES20.glViewport(0, 0, width, height)
                    }
                    mFullFrameRect.drawFrame(mTextureID, mTmpMatrix)
                    mFrameRenderListener?.apply {
                        Timber.tag(TAG.RENDER).v("renderFrame")
                        if (mRefreshSet.contains(id)) {
                            onFrameSoon(id)
                        }
                    }
                    surface.swapBuffers()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            e.message?.apply {
//                if (contains("eglMake") && contains("failed")) {
                    Timber.tag(TAG.RENDER).e("releaseId.id = $releaseId")
                    unregisterPreviewSurface(releaseId)
//                }
            }
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
        const val INIT = 100
//        const val PREVIEW_HEIGHT    = 360

    class MainHandler(sharedRender: SharedRender, looper: Looper): Handler(looper) {
        private val mWeakContext = WeakReference<SharedRender>(sharedRender)

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                INIT -> mWeakContext.get()?.init()
                else -> {
                    mWeakContext.get()?.renderFrame()
                }
            }
        }

    }
}

}