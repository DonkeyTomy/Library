package com.zzx.media.camera.mtk

import android.annotation.SuppressLint
import android.hardware.Camera
import com.zzx.media.camera.v1.manager.Camera1Manager
import timber.log.Timber
import java.lang.reflect.Method

/**@author Tomy
 * Created by Tomy on 2018/4/5.
 */
@SuppressLint("PrivateApi")
open class MtkCamera1Manager: Camera1Manager() {

    protected val mMtkSetContinuousSpeedMethod: Method by lazy {
        Camera::class.java.getDeclaredMethod("setContinuousShotSpeed", Integer::class.java)
    }

    protected val mMtkCancelContinuousMethod: Method by lazy {
        Camera::class.java.getDeclaredMethod("cancelContinuousShot")
    }
    override fun cancelContinuousShot() {
        try {
            mMtkCancelContinuousMethod.invoke(mCamera)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * @param speed Int : the speed set for continuous shot(xx fps)
     */
    override fun setContinuousShotSpeed(speed: Int) {
        try {
            mMtkSetContinuousSpeedMethod.invoke(mCamera, speed)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /**
     * 设置成高速连拍模式
     * */
    /**
     * 设置成高速连拍模式
     * */
    override fun setPictureBurstMode(pictureCount: Int) {
        Timber.w("setPictureContinuousMode. pictureCount = $pictureCount; mBurstMode = $mBurstMode")
        mContinuousShotCount = pictureCount
        if (!mBurstMode) {
            mBurstMode = true
            mParameters?.apply {
                set(CAP_MODE, CAP_MODE_CONTINUOUS)
                set(BURST_NUM, pictureCount)
                set(MTK_CAM_MODE, CAMERA_MODE_MTK_PRV)
            }
            setParameter()
            restartPreview()
        } else {
            mParameters?.apply {
                set(BURST_NUM, pictureCount)
            }
            setParameter()
        }
    }

    /**
     * @see setPictureBurstMode
     */
    override fun setPictureNormalMode() {
        if (mBurstMode) {
            mParameters?.apply {
                set(CAP_MODE, CAP_MODE_NORMAL)
                set(BURST_NUM, 1)
                set(MTK_CAM_MODE, CAMERA_MODE_NORMAL)
            }
            setParameter()
            mBurstMode = false
        }
    }

    companion object {
        const val CAP_MODE  = "cap-mode"
        const val CAP_MODE_NORMAL   = "normal"
        const val CAP_MODE_CONTINUOUS = "continuousshot"
        const val BURST_NUM = "burst-num"
        const val MTK_CAM_MODE = "mtk-cam-mode"
        const val CAMERA_MODE_NORMAL    = 0
        const val CAMERA_MODE_MTK_PRV   = 1
        const val CAMERA_MODE_MTK_VDO   = 2
        const val CAMERA_MODE_MTK_VT    = 3
    }

}