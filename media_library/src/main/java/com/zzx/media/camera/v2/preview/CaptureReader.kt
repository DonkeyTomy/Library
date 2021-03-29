package com.zzx.media.camera.v2.preview

import android.media.ImageReader
import android.os.Handler
import android.view.Surface

/**@author Tomy
 * Created by Tomy on 2018/4/3.
 */
class CaptureReader(var mWidth: Int,
                    var mHeight: Int,
                    var mFormat: Int) {

    private var mCaptureReader: ImageReader? = null

    init {
        mCaptureReader = ImageReader.newInstance(mWidth, mHeight, mFormat, 10)
    }

    fun getSurface(): Surface {
        return mCaptureReader!!.surface
    }

    fun setOnImageAvailableListener(availableListener: ImageReader.OnImageAvailableListener, handler: Handler) {
        mCaptureReader!!.setOnImageAvailableListener(availableListener, handler)
    }

}