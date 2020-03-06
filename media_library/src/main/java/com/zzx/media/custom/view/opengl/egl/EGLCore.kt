package com.zzx.media.custom.view.opengl.egl

import android.graphics.SurfaceTexture
import android.view.Surface

/**@author Tomy
 * Created by Tomy on 2019/11/13.
 */
interface EGLCore<eglContext, eglSurface, eglConfig> {

    fun initEGLContext(sharedContext: eglContext? = null, flags: Int = 0)

    fun getCurrentContext(): eglContext

    /**
     * create an EGL surface associated with a Surface.]
     * <p>
     *     If this is destined for MediaCodec, the EGLConfig should have the "recordable" attribute.
     * @param surface EGL14 must be [Surface] or [SurfaceTexture]
     * @see releaseSurface
     */
    fun createWindowSurface(surface: Any): eglSurface

    /**
     * 销毁指定的Surface.
     * PS: 若它是某个Context的CurrentSurface则实际上不会被销毁.
     * @param eglSurface eglSurface
     */
    fun releaseSurface(eglSurface: eglSurface)

    /**
     * 创建一个与离屏Buffer相关联的EGLSurface.
     * @param width Int
     * @param height Int
     * @return eglSurface
     */
    fun createOffscreenSurface(width: Int, height: Int): eglSurface


    /**
     * Makes our EGL context current, using the supplied(提供的) "draw" and "read" surfaces.
     * @param drawSurface EGLSurface
     * @param readSurface EGLSurface
     */
    fun makeCurrent(drawSurface: eglSurface, readSurface: eglSurface)

    fun makeCurrent(eglSurface: eglSurface)


    /**
     * Makes not context current.
     */
    fun makeNothingCurrent()

    /**
     * 提交时间戳给EGL
     * @param eglSurface eglSurface
     * @param nanoseconds Long 得是微妙.
     */
    fun setPresentationTime(eglSurface: eglSurface, nanoseconds: Long)


    /**
     * Returns true if our context and the specified surface are current.
     * @param eglSurface EGLSurface
     * @return Boolean
     */
    fun isCurrent(eglSurface: eglSurface): Boolean

    /**
     * "publish" th current frame.发射当前帧.
     * @param eglSurface eglSurface
     * @return Boolean
     */
    fun swapBuffers(eglSurface: eglSurface): Boolean

    /**
     * 执行对Surface的查询指令.可以用来查询参数.
     * @param what 查询指定的某个参数.
     * @return Int
     */
    fun querySurface(eglSurface: eglSurface, what: Int): Int

    fun queryString(what: Int): String

    fun queryGLVersion(): Int

    fun checkDisplay(): Boolean

    /**
     * 释放所持有的所有资源,特别是EGL Context.
     * 这个方法必须在EGLContext创建的同一个线程调用.
     * <P>
     *     完成后,当前没有任何Context.
     */
    fun release()

    fun getConfig(flags: Int, version: Int): eglConfig?

}