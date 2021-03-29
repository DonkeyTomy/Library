package com.zzx.media.custom.view.texture

import android.graphics.SurfaceTexture

/**@author Tomy
 * Created by Tomy on 2017/9/11.
 */
class MyTextureSurfaceRenderer(surfaceTexture: SurfaceTexture, width: Int, height: Int) : TextureSurfaceRenderer(surfaceTexture, width, height),
        SurfaceTexture.OnFrameAvailableListener {

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {

    }

    override fun draw(): Boolean {
        return true
    }

    override fun initEGLComponents() {
    }

    override fun deinitEGLComponents() {
    }

    override fun getSurfaceTexture(): SurfaceTexture? {
        return null
    }
}