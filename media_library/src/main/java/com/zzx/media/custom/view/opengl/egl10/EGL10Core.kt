package com.zzx.media.custom.view.opengl.egl10

import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.EGLExt
import android.view.SurfaceHolder
import com.zzx.media.custom.view.opengl.egl.EGLCore
import timber.log.Timber
import javax.microedition.khronos.egl.*

/**@author Tomy
 * Created by Tomy on 2019/11/13.
 */
class EGL10Core: EGLCore<EGLContext, EGLSurface, EGLConfig> {

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

    private var mEGLContext = EGL10.EGL_NO_CONTEXT

    private var mEGLDisplay = EGL10.EGL_NO_DISPLAY

    private var mEGLConfig: EGLConfig? = null

    private var mGLVersion = -1

    private var mEGL: EGL10? = null

    constructor(egl: EGL10, eglDisplay: EGLDisplay, eglConfig: EGLConfig, sharedContext: EGLContext? = null) {
        mEGL = egl
        mEGLDisplay = eglDisplay
        mEGLConfig = eglConfig
        initEGLContext(sharedContext)
    }

    constructor(sharedContext: EGLContext? = null, flags: Int = 0) {
        initEGLContext(sharedContext, flags)
    }


    override fun initEGLContext(sharedContext: EGLContext?, flags: Int) {
        Timber.i("sharedContext = $sharedContext; NO_CONTEXT = ${EGL10.EGL_NO_CONTEXT}")
        val tmpContext = sharedContext ?: EGL10.EGL_NO_CONTEXT
        /*if (mEGLDisplay != EGL10.EGL_NO_DISPLAY) {
            throw RuntimeException("EGLDisplay already set up")
        }*/
        if (mEGL == null) {
            mEGL = EGLContext.getEGL() as EGL10
        }
        if (mEGLDisplay == EGL10.EGL_NO_DISPLAY) {
            mEGLDisplay = mEGL!!.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
        }
        if (mEGLDisplay == EGL10.EGL_NO_DISPLAY) {
            throw RuntimeException("unable to get EGL10 Display")
        }

        val version = IntArray(2)
        if (!mEGL!!.eglInitialize(mEGLDisplay, version)) {
            mEGLDisplay = null
            throw RuntimeException("unable to initialize EGL10")
        }

        //Try get a EGL_ES 3, if requested.
        if ((flags.and(FLAG_TRY_EGLES_3) != 0)) {
            val config = mEGLConfig ?: getConfig(flags, 3)
            if (config != null) {
                val attribute3List = intArrayOf(
                        EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,
                        EGL10.EGL_NONE
                )
                val context = mEGL!!.eglCreateContext(mEGLDisplay, config, tmpContext, attribute3List)
                if (mEGL!!.eglGetError() == EGL10.EGL_SUCCESS) {
                    mEGLConfig  = config
                    mEGLContext = context
                    mGLVersion  = 3
                }
            }
        }

        if (mEGLContext == EGL10.EGL_NO_CONTEXT) {
            val config = mEGLConfig ?: getConfig(flags, 2)
            if (config != null) {
                val attribute2List = intArrayOf(
                        EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                        EGL10.EGL_NONE
                )
                val context = mEGL!!.eglCreateContext(mEGLDisplay, config, tmpContext, attribute2List)
                if (mEGL!!.eglGetError() == EGL10.EGL_SUCCESS) {
                    mEGLConfig  = config
                    mEGLContext = context
                    mGLVersion  = 2
                }
            } else {
                throw RuntimeException("Unable to find a suitable EGLConfig")
            }
        }

       /* //Confirm with
        val values = IntArray(1)
        mEGL.eglQueryContext(mEGLDisplay, mEGLContext, EGL14.EGL_CONTEXT_CLIENT_VERSION, values)*/

    }

    override fun getCurrentContext(): EGLContext {
        return mEGLContext
    }

    /**
     * create an EGL surface associated with a Surface.]
     * <p>
     *     If this is destined for MediaCodec, the EGLConfig should have the "recordable" attribute.
     * @param surface EGL10 must be [SurfaceHolder] or [SurfaceTexture].
     * @see releaseSurface
     */
    override fun createWindowSurface(surface: Any): EGLSurface {
        if (!((surface is SurfaceHolder) || (surface is SurfaceTexture) )) {
            throw RuntimeException("invalid surface: $surface")
        }

        //Create window surface, and attach it to the Surface received.
        val surfaceAttribute = intArrayOf(EGL10.EGL_NONE)
        return mEGL!!.eglCreateWindowSurface(mEGLDisplay, mEGLConfig, surface, surfaceAttribute)
    }

    override fun releaseSurface(eglSurface: EGLSurface) {
        mEGL!!.eglDestroySurface(mEGLDisplay, eglSurface)
    }


    /**
     *
     * @param width Int
     * @param height Int
     * @return EGLSurface?
     */
    override fun createOffscreenSurface(width: Int, height: Int): EGLSurface {
        val surfaceAttrs = intArrayOf(
                EGL10.EGL_WIDTH, width,
                EGL10.EGL_HEIGHT, height,
                EGL10.EGL_NONE
        )
        return mEGL!!.eglCreatePbufferSurface(mEGLDisplay, mEGLConfig, surfaceAttrs)
    }


    /**
     * Makes our EGL context current, using the supplied "draw" and "read" surfaces.
     * @param drawSurface EGLSurface
     * @param readSurface EGLSurface
     */
    override fun makeCurrent(drawSurface: EGLSurface, readSurface: EGLSurface) {
        if (checkDisplay()) {
            if (!mEGL!!.eglMakeCurrent(mEGLDisplay, drawSurface, readSurface, mEGLContext)) {
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
        if (!mEGL!!.eglMakeCurrent(mEGLDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT)) {
            throw RuntimeException("eglMakeNothingCurrent failed")
        }
    }

    /**
     * Returns true if our context and the specified surface are current.
     * @param eglSurface EGLSurface
     * @return Boolean
     */
    override fun isCurrent(eglSurface: EGLSurface): Boolean {
        return mEGLContext == mEGL!!.eglGetCurrentContext() && eglSurface == mEGL!!.eglGetCurrentSurface(EGL10.EGL_DRAW)
    }

    /**
     * Use this to "publish" the current frame.
     * @param eglSurface EGLSurface
     * @return Boolean
     */
    override fun swapBuffers(eglSurface: EGLSurface): Boolean {
        return mEGL!!.eglSwapBuffers(mEGLDisplay, eglSurface)
    }

    override fun setPresentationTime(eglSurface: EGLSurface, nanoseconds: Long) {
//        EGLExt.eglPresentationTimeANDROID(mEGLDisplay, eglSurface, nanoseconds)
    }

    /**
     * performs a simple surface query.
     * @param eglSurface EGLSurface
     * @param what 查询指定的某个参数.如[EGL10.EGL_WIDTH],[EGL10.EGL_HEIGHT]
     * @return Int
     */
    override fun querySurface(eglSurface: EGLSurface, what: Int): Int {
        val value = IntArray(1)
        mEGL!!.eglQuerySurface(mEGLDisplay, eglSurface, what, value)
        return value[0]
    }

    override fun queryString(what: Int): String {
        return mEGL!!.eglQueryString(mEGLDisplay, what)
    }

    override fun queryGLVersion(): Int {
        return mGLVersion
    }

    override fun checkDisplay(): Boolean {
        if (mEGLDisplay == EGL10.EGL_NO_DISPLAY) {
            throw RuntimeException("EGLDisplay not set up.")
        }
        return true
    }

    override fun release() {
        if (mEGLDisplay != EGL10.EGL_NO_DISPLAY) {
            mEGL!!.eglMakeCurrent(mEGLDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT)
            mEGL!!.eglDestroyContext(mEGLDisplay, mEGLContext)
            mEGL!!.eglTerminate(mEGLDisplay)
        }
        mEGLDisplay = EGL10.EGL_NO_DISPLAY
        mEGLContext = EGL10.EGL_NO_CONTEXT
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

        val attributeList = intArrayOf(
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_SURFACE_TYPE, renderAbleType,
                EGL10.EGL_NONE, 0,//placeholder for recordable
                EGL10.EGL_NONE
        )
        if (flags.and(FLAG_RECORDABLE) != 0) {
            attributeList[attributeList.size - 3] = EGL_RECORDABLE_ANDROID
            attributeList[attributeList.size - 2] = 1
        }
        val configs = Array<EGLConfig?>(1){null}
        val numConfigs = IntArray(1)
        if (!mEGL!!.eglChooseConfig(mEGLDisplay, attributeList, configs, 1, numConfigs)) {
            return null
        }

        return configs[0]
    }


}