package com.zzx.media.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.util.Size

/**@author Tomy
 * Created by Tomy on 2018/11/2.
 * 用于获取多媒体文件信息.
 */
object MediaInfoUtil {

    /**
     * 获得视频的分辨率
     */
    @Synchronized
    fun getVideoRatio(videoPath: String): Size {
        return MediaMetadataRetriever().run {
            setDataSource(videoPath)
            val width   = extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH).toInt()
            val height  = extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT).toInt()
            Size(width, height)
        }
    }

    @Synchronized
    fun getVideoDuration(videoPath: String): Long {
        return MediaMetadataRetriever().run {
            setDataSource(videoPath)
            extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLong()
        }
    }

    @Synchronized
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

    @Synchronized
    fun saveImageToDatabase(context: Context, info: MediaInfo): Uri? {
        val values = ContentValues(14)
        values.apply {
            put(MediaStore.Images.ImageColumns.TITLE, info.mTitle)
            put(MediaStore.Images.ImageColumns.DISPLAY_NAME, info.mDisplayName)
            put(MediaStore.Images.ImageColumns.DATE_TAKEN, info.mDateTaken)
            put(MediaStore.Images.ImageColumns.MIME_TYPE, info.mMimeType)
            put(MediaStore.Images.ImageColumns.DATA, info.mData)
            put(MediaStore.Images.ImageColumns.SIZE, info.mDataSize)
            if (info.mHasLocation) {
                put(MediaStore.Images.ImageColumns.LATITUDE, info.mLatitude)
                put(MediaStore.Images.ImageColumns.LONGITUDE, info.mLongitude)
            }
            put(MediaStore.Images.ImageColumns.ORIENTATION, info.mOrientation)
            put(MediaStore.Images.ImageColumns.WIDTH, info.mWidth)
            put(MediaStore.Images.ImageColumns.HEIGHT, info.mHeight)
        }
        try {
            return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    @Synchronized
    fun saveVideoToDatabase(context: Context, info: MediaInfo): Uri? {
        val values = ContentValues(14)
        values.apply {
            put(MediaStore.Video.VideoColumns.TITLE, info.mTitle)
            put(MediaStore.Video.VideoColumns.DISPLAY_NAME, info.mDisplayName)
            put(MediaStore.Video.VideoColumns.DATE_TAKEN, info.mDateTaken)
            put(MediaStore.Video.VideoColumns.MIME_TYPE, info.mMimeType)
            put(MediaStore.Video.VideoColumns.DATA, info.mData)
            put(MediaStore.Video.VideoColumns.SIZE, info.mDataSize)
            if (info.mHasLocation) {
                put(MediaStore.Video.VideoColumns.LATITUDE, info.mLatitude)
                put(MediaStore.Video.VideoColumns.LONGITUDE, info.mLongitude)
            }
            put(MediaStore.Video.VideoColumns.WIDTH, info.mWidth)
            put(MediaStore.Video.VideoColumns.HEIGHT, info.mHeight)
            put(MediaStore.Video.VideoColumns.DURATION, info.mDuration)
        }
        try {
            return context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    @Synchronized
    fun saveAudioToDatabase(context: Context, info: MediaInfo): Uri? {
        val values = ContentValues(14)
        values.apply {
            put(MediaStore.Audio.AudioColumns.TITLE, info.mTitle)
            put(MediaStore.Audio.AudioColumns.DISPLAY_NAME, info.mDisplayName)
            put(MediaStore.Audio.AudioColumns.MIME_TYPE, info.mMimeType)
            put(MediaStore.Audio.AudioColumns.DATA, info.mData)
            put(MediaStore.Audio.AudioColumns.SIZE, info.mDataSize)
            put(MediaStore.Audio.AudioColumns.DURATION, info.mDuration)
        }
        try {
            return context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    @Synchronized
    fun deleteDatabase(context: Context, filePath: String, uri: Uri) {
        try {
            context.contentResolver.delete(uri, "${MediaStore.MediaColumns.DATA} = ?", arrayOf(filePath))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    data class MediaInfo(
            val mTitle: String,
            val mDisplayName: String,
            val mDateTaken: Long,
            val mMimeType: String,
            val mData: String,
            val mDataSize: Long,
            val mWidth: Int = 0,
            val mHeight: Int= 0,
            val mDuration: Long = 0,
            val mHasLocation: Boolean = false,
            val mLatitude: Long     = 0,
            val mLongitude: Long    = 0,
            val mOrientation: Int = 0

    )

}