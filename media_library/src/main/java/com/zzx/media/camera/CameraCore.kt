package com.zzx.media.camera

/**@author Tomy
 * Created by Tomy on 2019/11/26.
 * 保存Camera的状态参数及对象.
 */
class CameraCore<camera> {

    private var mCamera: camera? = null

    fun getCamera(): camera? {
        return mCamera
    }



    companion object {
        const val STATUS_RELEASE    = 0x0
        const val STATUS_IDLE       = 0x1
        const val STATUS_OPENING    = 0x2
        const val STATUS_CLOSING    = 0x4
        const val STATUS_CAPTURING  = 0x4
    }
}