package com.zzx.media.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils


/**@author Tomy
 * Created by Tomy on 2014/6/30.
 */
object ThumbnailUtil {

    private val TAG = "ThumbnailUtil: "

    /**获取指定路径名的图片的缩略图
     * @param imagePath 图片路径
     * @param width 指定缩略图的宽度
     * @param height 指定缩略图的高度
     */
    fun getImageThumbnail(imagePath: String, width: Int, height: Int): Bitmap? {
        if (width <= 0 || height <= 0) {
            return null
        }
        var bitmap: Bitmap?
        val options = BitmapFactory.Options()
        /**此变量设置为true,则表示在生成Bitmap时只根据原图来填充options属性,
         * 返回的Bitmap为null.
         * 此处只是为了获取图片宽高来计算缩放比.
         */
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(imagePath, options)
        options.inJustDecodeBounds = false
        options.inPreferredConfig = Bitmap.Config.RGB_565
        //计算缩放比
        val h = options.outHeight
        val w = options.outWidth
        val scaleWidth = w / width
        val scaleHeight = h / height
        //取缩放比例较小的一个
        var scale = if (scaleWidth < scaleHeight) {
            scaleWidth
        } else {
            scaleHeight
        }
        if (scale <= 0) {
            scale = 1
        }
        //缩放比例值:宽高缩放为 1/inSampleSize.
        options.inSampleSize = scale
        /**根据缩放比真正的生成缩略Bitmap.
         */
        try {
            bitmap = BitmapFactory.decodeFile(imagePath, options)
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT)
        } catch (e: Exception) {
            return null
        }
        return bitmap
    }

    /**获取指定路径名的图片的缩略图
     * @param data 图片路径
     * @param width 指定缩略图的宽度
     * @param height 指定缩略图的高度
     */
    fun getImageThumbnail(data: ByteArray, width: Int, height: Int): Bitmap? {
        if (width <= 0 || height <= 0) {
            return null
        }
        var bitmap: Bitmap? = null
        val options = BitmapFactory.Options()
        /**此变量设置为true,则表示在生成Bitmap时只根据原图来填充options属性,
         * 返回的Bitmap为null.
         * 此处只是为了获取图片宽高来计算缩放比.
         */
        options.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(data, 0, data.size, options)
        options.inJustDecodeBounds = false
        //计算缩放比
        val h = options.outHeight
        val w = options.outWidth
        val scaleWidth = w / width
        val scaleHeight = h / height
        //取缩放比例较小的一个
        var scale = if (scaleWidth < scaleHeight) {
            scaleWidth
        } else {
            scaleHeight
        }
        if (scale <= 0) {
            scale = 1
        }
        //缩放比例值:宽高缩放为 1/inSampleSize.
        options.inSampleSize = scale
        /**根据缩放比真正的生成缩略Bitmap.
         */
        val bitmap1: Bitmap?
        try {
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.size, options)
            bitmap1 = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT)
        } catch (e: Exception) {
            return null
        } finally {
            bitmap?.recycle()
        }
        return bitmap1
    }

    /**
     * 获取指定视频的缩略图
     * 先通过ThumbnailUtils.createVideoThumbnail来生成视频的缩略图.
     * 再根据extractThumbnail来生成指定宽高的缩略图
     *
     * 如果想要的缩略图的宽和高都小于MICRO_KIND,则类型要使用MICRO_KIND作为kind的值,这样会节省内存.
     * MediaStore.Images.Thumbnails.MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
     *
     * @param videoPath 视频路径
     * @param width 指定宽度
     * @param height 指定高度
     *
     */
    fun getVideoThumbnail(videoPath: String, width: Int, height: Int): Bitmap? {
        if (width <= 0 || height <= 0) {
            return null
        }
        var bitmap: Bitmap? = null
        var result: Bitmap? = null
        val retriever = MediaMetadataRetriever()
        try {
            /**此方法不能对正在录像的视频使用,否则报错.
             */
            retriever.setDataSource(videoPath)
            bitmap = retriever.getScaledFrameAtTime(-1, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, width, height)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        //        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Images.Thumbnails.MICRO_KIND);
        /*if (bitmap != null) {
            result = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT)
            bitmap.recycle()
        }*/
        return bitmap
    }

}
