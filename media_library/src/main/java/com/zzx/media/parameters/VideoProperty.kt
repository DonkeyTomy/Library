package com.zzx.media.parameters

import android.media.MediaRecorder

/**@author Tomy
 * Created by Tomy on 2018/6/13.
 */
data class VideoProperty(
        var width: Int,
        var height: Int,
        var frameRate: Int,
        var bitRate: Int,
        var audioProperty: AudioProperty?,
        var outputFormat: Int = MediaRecorder.OutputFormat.MPEG_4,
        var encoder: Int = MediaRecorder.VideoEncoder.H264

)