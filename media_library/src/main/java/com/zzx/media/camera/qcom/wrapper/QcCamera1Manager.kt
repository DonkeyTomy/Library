package com.zzx.media.camera.qcom.wrapper

import com.zzx.media.camera.v1.manager.Camera1Manager
import timber.log.Timber

/**@author Tomy
 * Created by Tomy on 2022/7/14.
 */
class QcCamera1Manager: Camera1Manager() {


    override fun setPictureBurstMode(pictureCount: Int) {
        Timber.w("setPictureContinuousMode. pictureCount = $pictureCount; mBurstMode = $mBurstMode")
        mContinuousShotCount = pictureCount
        if (!mBurstMode) {
            mBurstMode = true
            mParameters?.apply {
                set(BURST_MODE_QC, "on")
                set(BURST_SNAP_NUM, pictureCount)
            }
            setParameter()
//            restartPreview()
        } else {
            mParameters?.apply {
                set(BURST_MODE_QC, "off")
            }
            setParameter()
        }
    }

    override fun setPictureNormalMode() {
    }

    companion object {
        const val BURST_MODE_QC    = "long-shot"
        const val BURST_SNAP_NUM    = "num-snaps-per-shutter"
    }

}