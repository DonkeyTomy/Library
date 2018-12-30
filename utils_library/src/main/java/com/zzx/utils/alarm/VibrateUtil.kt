package com.zzx.utils.alarm

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator

/**@author Tomy
 * Created by Tomy on 2018/12/27.
 */
class VibrateUtil(context: Context) {

    private val mVibrator by lazy {
        context.getSystemService(Vibrator::class.java)
    }

    fun start() {
        mVibrator.cancel()
        mVibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 200, 200, 200, 200, 200), -1))
    }

}