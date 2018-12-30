package com.zzx.media.utils

import android.graphics.BitmapFactory
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

    fun getImageRation(imagePath: String): Size {
        val options = BitmapFactory.Options()
        /**此变量设置为true,则表示在生成Bitmap时只根据原图来填充options属性,
         * 返回的Bitmap为null.
         * 此处只是为了获取图片宽高来计算缩放比.
         */
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(imagePath, options)
        return Size(options.outWidth, options.outHeight)
    }

}