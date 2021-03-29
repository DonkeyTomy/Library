package com.zzx.media.parameters

import android.media.MediaRecorder

/**@author Tomy
 * Created by Tomy on 2018/6/13.
 */
data class AudioProperty(
        var sampleRate: Int = 8000,
        var channels: Int   = 2,
        var bitCount: Int   = 8,
        var bitRate: Int = sampleRate * channels * bitCount,
        var encoder: Int = MediaRecorder.AudioEncoder.AAC
)