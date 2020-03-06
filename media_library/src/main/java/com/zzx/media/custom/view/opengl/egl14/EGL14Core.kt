package com.zzx.media.custom.view.opengl.egl14

import android.graphics.SurfaceTexture
import android.opengl.*
import android.view.Surface
import com.zzx.media.custom.view.opengl.egl.EGLCore
import timber.log.Timber

/**@author Tomy
 * Created by Tomy on 2019/11/8.
 *
 * Core EGL state(Display, EGLContext, config).
 * 只管理EGLDisplay,EGLContext,EGLConfig,用来创建及控制EGLSurface而不保存EGLSurface实例.
 * <p>
 *     The EGLContext must only be attached to one thread at a time.
 *     This class is not thread-safe.
 */
class EGL14Core: EGLCore<EGLContext, EGLSurface, EGLConfig> {

    companion object {
        const val TAG = "EGL14Core"

        /**
         * Constructor flag: surface must be recordable.  This discourages EGL from using a
         * pixel format that cannot be converted efficiently to something usable by the video
         * encoder.
         */
        const val FLAG_RECORDABLE = 0x01

        /**
         * Constructor flag: ask for GLES_3, fall back to GLES_2 if not available.  Without this
         * flag, GLES_2 is used.
         */
        const val FLAG_TRY_EGLES_3 = 0x02

        //Android-specific extension
        const val EGL_RECORDABLE_ANDROID = 0x3142
    }

    private var mEGLContext = EGL14.EGL_NO_CONTEXT

    private var mEGLDisplay = EGL14.EGL_NO_DISPLAY

    private var mEGLConfig: EGLConfig? = null

    private var mGLVersion = -1

    constructor(sharedContext: EGLContext = EGL14.EGL_NO_CONTEXT, flags: Int = 0) {
        initEGLContext(sharedContext, flags)
    }

    constructor(eglContext: EGLContext, eglDisplay: EGLDisplay, eglConfig: EGLConfig) {
        mEGLContext = eglContext
        mEGLDisplay = eglDisplay
        mEGLConfig  = eglConfig
    }


    override fun initEGLContext(sharedContext: EGLContext?, flags: Int) {
        val tmpContext = sharedContext ?: EGL14.EGL_NO_CONTEXT
        if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
            throw RuntimeException("EGLDisplay already set up")
        }

        mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            throw RuntimeException("unable to get EGL14 Display")
        }

        val version = IntArray(2)
        if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 0)) {
            mEGLDisplay = null
            throw RuntimeException("unable to initialize EGL14")
        }

        //Try get a EGL_ES 3, if requested.
        if ((flags.and(FLAG_TRY_EGLES_3) != 0)) {
            val config = getConfig(flags, 3)
            if (config != null) {
                val attribute3List = intArrayOf(
                        EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,
                        EGL14.EGL_NONE
                )
                val context = EGL14.eglCreateContext(mEGLDisplay, config, tmpContext, attribute3List, 0)
                if (EGL14.eglGetError() == EGL14.EGL_SUCCESS) {
                    mEGLConfig  = config
                    mEGLContext = context
                    mGLVersion  = 3
                }
            }
        }

        if (mEGLContext == EGL14.EGL_NO_CONTEXT) {
            val config = getConfig(flags, 2)
            if (config != null) {
                val attribute2List = intArrayOf(
                        EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                        EGL14.EGL_NONE
                )
                val context = EGL14.eglCreateContext(mEGLDisplay, config, tmpContext, attribute2List, 0)
                if (EGL14.eglGetError() == EGL14.EGL_SUCCESS) {
                    mEGLConfig  = config
                    mEGLContext = context
                    mGLVersion  = 2
                }
            } else {
                throw RuntimeException("Unable to find a suitable EGLConfig")
            }
        }

        //Confirm with
        val values = IntArray(1)
        EGL14.eglQueryContext(mEGLDisplay, mEGLContext, EGL14.EGL_CONTEXT_CLIENT_VERSION, values, 0)
        Timber.i("EGLContext created, client version = ${values[0]}")

    }

    override fun getCurrentContext(): EGLContext {
        return mEGLContext
    }

    /**
     * create an EGL surface associated with a Surface.]
     * <p>
     *     If this is destined for MediaCodec, the EGLConfig should have the "recordable" attribute.
     * @param surface EGL14 must be [Surface] or [SurfaceTexture]
     * @see releaseSurface
     */
    override fun createWindowSurface(surface: Any): EGLSurface {
        if (!((surface is Surface) || (surface is SurfaceTexture) )) {
            throw RuntimeException("invalid surface: $surface")
        }

        //Create window surface, and attach it to the Surface received.
        val surfaceAttribute = intArrayOf(EGL14.EGL_NONE)
        return EGL14.eglCreateWindowSurface(mEGLDisplay, mEGLConfig, surface, surfaceAttribute, 0)
    }

    override fun releaseSurface(eglSurface: EGLSurface) {
        EGL14.eglDestroySurface(mEGLDisplay, eglSurface)
    }


    /**
     *
     * @param width Int
     * @param height Int
     * @return EGLSurface?
     */
    override fun createOffscreenSurface(width: Int, height: Int): EGLSurface {
        val surfaceAttrs = intArrayOf(
                EGL14.EGL_WIDTH, width,
                EGL14.EGL_HEIGHT, height,
                EGL14.EGL_NONE
        )
        return EGL14.eglCreatePbufferSurface(mEGLDisplay, mEGLConfig, surfaceAttrs, 0)
    }


    /**
     * Makes our EGL context current, using the supplied "draw" and "read" surfaces.
     * @param drawSurface EGLSurface
     * @param readSurface EGLSurface
     */
    override fun makeCurrent(drawSurface: EGLSurface, readSurface: EGLSurface) {
        if (checkDisplay()) {
            if (!EGL14.eglMakeCurrent(mEGLDisplay, drawSurface, readSurface, mEGLContext)) {
                throw RuntimeException("eglMakeCurrent failed")
            }
        }
    }

    /**
     * Makes our EGL Context current, using the supplied surface for both "draw" and "read".
     * @param eglSurface EGLSurface
     */
    override fun makeCurrent(eglSurface: EGLSurface) {
        makeCurrent(eglSurface, eglSurface)
    }

    override fun makeNothingCurrent() {
        if (!EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)) {
            throw RuntimeException("eglMakeNothingCurrent failed")
        }
    }

    /**
     * Returns true if our context and the specified surface are current.
     * @param eglSurface EGLSurface
     * @return Boolean
     */
    override fun isCurrent(eglSurface: EGLSurface): Boolean {
        return mEGLContext == EGL14.eglGetCurrentContext() && eglSurface == EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW)
    }

    /**
     * Use this to "publish" the current frame.
     * @param eglSurface EGLSurface
     * @return Boolean
     */
    override fun swapBuffers(eglSurface: EGLSurface): Boolean {
        return EGL14.eglSwapBuffers(mEGLDisplay, eglSurface)
    }

    override fun setPresentationTime(eglSurface: EGLSurface, nanoseconds: Long) {
        EGLExt.eglPresentationTimeANDROID(mEGLDisplay, eglSurface, nanoseconds)
    }

    /**
     * 执行对Surface的查询指令.可以用来查询参数.
     * @param eglSurface EGLSurface
     * @param what 查询指定的某个参数.如[EGL14.EGL_WIDTH],[EGL14.EGL_HEIGHT]
     * @return Int
     */
    override fun querySurface(eglSurface: EGLSurface, what: Int): Int {
        val value = IntArray(1)
        EGL14.eglQuerySurface(mEGLDisplay, eglSurface, what, value, 0)
        return value[0]
    }

    override fun queryString(what: Int): String {
        return EGL14.eglQueryString(mEGLDisplay, what)
    }

    override fun queryGLVersion(): Int {
        return mGLVersion
    }

    override fun checkDisplay(): Boolean {
        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            throw RuntimeException("EGLDisplay not set up.")
        }
        return true
    }


    override fun release() {
        if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
            /**
             * Android 使用一个索引计数的EGLDisplay.
             * 因此每个[EGL14.eglInitialize]都需要一个对应的[EGL14.eglTerminate]
             */
            EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
            EGL14.eglDestroyContext(mEGLDisplay, mEGLContext)
            EGL14.eglReleaseThread()
            EGL14.eglTerminate(mEGLDisplay)
        }
        mEGLDisplay = EGL14.EGL_NO_DISPLAY
        mEGLContext = EGL14.EGL_NO_CONTEXT
        mEGLConfig  = null
    }


    /**
     * Finds a suitable EGLConfig
     * @param flags Int Bit flags from constructor.
     * @param version Int EGL version. Must be 2 or 3.
     */
    override fun getConfig(flags: Int, version: Int): EGLConfig? {
        var renderAbleType = EGL14.EGL_OPENGL_ES2_BIT
        if (version >= 3) {
            renderAbleType = renderAbleType.or(EGLExt.EGL_OPENGL_ES3_BIT_KHR)
        }

        /**
         * The actual surface is generally RGBA or RGBX, 因此情景模式中省略的透明度实际上没有帮助.
         * 这也能使在用 [GLES20.glReadPixels]读取进GL_RGBA缓存时带来巨大的性能.
         */
        val attributeList = intArrayOf(
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, renderAbleType,
                EGL14.EGL_NONE, 0,//placeholder for recordable [@-3]
                EGL14.EGL_NONE
        )
        if (flags.and(FLAG_RECORDABLE) != 0) {
            attributeList[attributeList.size - 3] = EGL_RECORDABLE_ANDROID
            attributeList[attributeList.size - 2] = 1
        }
        val configs = Array<EGLConfig?>(1){null}
        val numConfigs = IntArray(1)
        if (!EGL14.eglChooseConfig(mEGLDisplay, attributeList, 0, configs, 0, configs.size, numConfigs, 0)) {
            return null
        }

        return configs[0]
    }


}