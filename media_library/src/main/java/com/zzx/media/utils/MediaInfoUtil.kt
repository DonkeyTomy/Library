package com.zzx.media.utils

import android.media.MediaMetadataRetriever
import android.util.Size

/**@author Tomy
 * Created by Tomy on 2018/11/2.
 * 用于获取多媒体文件信息.
 */
object MediaInfoUtil {

    /**
     * 获得视频的分辨率
     */
    fun getVideoRatio(videoPath: String): Size {
        return MediaMetadataRetriever().run {
            setDataSource(videoPath)
            val width   = extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH).toInt()
            val height  = extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT).toInt()
            Size(width, height)
        }
    }

}