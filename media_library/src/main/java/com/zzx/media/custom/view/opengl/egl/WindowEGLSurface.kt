package com.zzx.media.custom.view.opengl.egl

import android.graphics.SurfaceTexture
import android.view.Surface

/**@author Tomy
 * Created by Tomy on 2019/11/9.
 */
class WindowEGLSurface<eglContext, eglSurface, eglConfig>: EGLSurfaceBase<eglContext, eglSurface, eglConfig> {

    private var mSurface: Any? = null
    private var mReleaseSurface = false

    constructor(eglCore: EGLCore<eglContext, eglSurface, eglConfig>, surface: Any, releaseSurface: Boolean): super(eglCore, false) {
        createWindowSurface(surface)
        mSurface    = surface
        mReleaseSurface = releaseSurface
    }

    constructor(eglCore: EGLCore<eglContext, eglSurface, eglConfig>, surfaceTexture: SurfaceTexture, isGL10: Boolean): super(eglCore, isGL10) {
        createWindowSurface(surfaceTexture)
    }

    override fun release() {
        super.release()
        mSurface?.apply {
            if (mSurface is Surface) {
                if (mReleaseSurface) {
                    (mSurface as Surface).release()
                }
                mSurface = null
            }
        }
    }

    /**
     * 使用新的EGLCore来创建EGLSurface.调用者必须已经调用[releaseEglSurface]来释放掉旧的EGLSurface.
     * <P>
     *     当我们想要更新EGLSurface关联到一个Surface上时可以调用此方法.
     *     例如,当我们想要与一个不同的EGLContext共享,而这只能通过拆除和重建该Context.(这是调用者执行的,而此方法只是为我们之前控制的Surface创建一个新的EGLSurface.)
     * <P>
     *     若上一个EGLSurface没有完全销毁,比如它仍被某个Context makeCurrent.此创建方法会因当前连接的Surface而报错.
     * 此方法目前只适用于[Surface]而不适用于[SurfaceTexture].
     * @param eglCore EGLCore<eglContext, eglSurface, eglConfig>
     */
    fun recreate(eglCore: EGLCore<eglContext, eglSurface, eglConfig>) {
        mSurface?.apply {
            if (mSurface is Surface) {
                mEGLCore = eglCore  //Switch to new Context
                createWindowSurface(this)   //Create new Surface
            }
        }

    }

}