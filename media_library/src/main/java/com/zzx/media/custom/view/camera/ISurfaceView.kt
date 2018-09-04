package com.zzx.media.custom.view.camera

/**@author Tomy
 * Created by Tomy on 2017/12/4.
 */
interface ISurfaceView<Surface, Camera> {

    fun initParams()

    fun startPreview()

    fun stopPreview()

    fun release()

    fun setCamera(camera: Camera)

    fun setRotation(rotation: Int)

    fun setPreviewSize(width: Int, height: Int)

    fun setLayoutParams(width: Int, height: Int)

    fun setStateCallback(stateCallback: StateCallback<Surface>)

    interface StateCallback<Surface> {

        fun onSurfaceCreate(surface: Surface?)

        fun onSurfaceSizeChange(surface: Surface?, width: Int, height: Int)

        fun onSurfaceDestroyed(surface: Surface?)

    }
}