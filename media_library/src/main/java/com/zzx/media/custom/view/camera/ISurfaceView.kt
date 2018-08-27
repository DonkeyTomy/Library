package com.zzx.media.custom.view.camera

import android.hardware.Camera

/**@author Tomy
 * Created by Tomy on 2017/12/4.
 */
interface ISurfaceView {

    fun initParams()

    fun startPreview()

    fun stopPreview()

    fun release()

    fun setCamera(camera: Camera)

    fun setPreviewSize(width: Int, height: Int)

    fun setLayoutParams(width: Int, height: Int)
}