package com.zzx.media.custom.view.opengl.egl

/**@author Tomy
 * Created by Tomy on 2019/11/9.
 */
class OffscreenEGLSurface<eglContext, eglSurface, eglConfig>(egl14Core: EGLCore<eglContext, eglSurface, eglConfig>, 
                                                             width: Int, height: Int, isEGL10: Boolean)
    : EGLSurfaceBase<eglContext, eglSurface, eglConfig>(egl14Core, isEGL10) {

    init {
        createOffscreenSurface(width, height)
    }



}