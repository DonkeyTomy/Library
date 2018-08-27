package com.zzx.utils.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;


/**@author Tomy
 * Created by Tomy on 2014/6/30.
 */
public class ThumbnailUtil {

    private static final String TAG = "ThumbnailUtil: ";

    /**获取指定路径名的图片的缩略图
     * @param imagePath 图片路径
     * @param width 指定缩略图的宽度
     * @param height 指定缩略图的高度
     * */
    public static Bitmap getImageThumbnail(String imagePath, int width, int height) {
        if (width <= 0 || height <= 0) {
            return null;
        }
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        /**此变量设置为true,则表示在生成Bitmap时只根据原图来填充options属性,
         * 返回的Bitmap为null.
         * 此处只是为了获取图片宽高来计算缩放比.
         * */
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        options.inJustDecodeBounds = false;
        //计算缩放比
        int h = options.outHeight;
        int w = options.outWidth;
        int scaleWidth  = w / width;
        int scaleHeight = h / height;
        int scale = 1;
        //取缩放比例较小的一个
        if (scaleWidth < scaleHeight) {
            scale = scaleWidth;
        } else {
            scale = scaleHeight;
        }
        if (scale <= 0) {
            scale = 1;
        }
        //缩放比例值:宽高缩放为 1/inSampleSize.
        options.inSampleSize = scale;
        /**根据缩放比真正的生成缩略Bitmap.
         * */
        Bitmap bitmap1 = null;
         try {
            bitmap = BitmapFactory.decodeFile(imagePath, options);
            bitmap1 = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        } catch (Exception e) {
            return null;
        } finally {
             if (bitmap != null) {
                 bitmap.recycle();
             }
         }
        return bitmap1;
    }

    /**获取图片像素数据的缩略图
     * @param data 图片像素数据
     * @param width 指定缩略图的宽度
     * @param height 指定缩略图的高度
     * */
    public static Bitmap getImageThumbnail(byte[] data, int width, int height) {
        if (width <= 0 || height <= 0) {
            return null;
        }
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        /**此变量设置为true,则表示在生成Bitmap时只根据原图来填充options属性,
         * 返回的Bitmap为null.
         * 此处只是为了获取图片宽高来计算缩放比.
         * */
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);
        options.inJustDecodeBounds = false;
        //计算缩放比
        int h = options.outHeight;
        int w = options.outWidth;
        int scaleWidth  = w / width;
        int scaleHeight = h / height;
        int scale = 1;
        //取缩放比例较小的一个
        if (scaleWidth < scaleHeight) {
            scale = scaleWidth;
        } else {
            scale = scaleHeight;
        }
        if (scale <= 0) {
            scale = 1;
        }
        //缩放比例值:宽高缩放为 1/inSampleSize.
        options.inSampleSize = scale;
        /**根据缩放比真正的生成缩略Bitmap.
         * */
        Bitmap bitmap1 = null;
        try {
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
            bitmap1 = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        } catch (Exception e) {
            return null;
        } finally {
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
        return bitmap1;
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
     * */
    public static Bitmap getVideoThumbnail(String videoPath, int width, int height) {
        if (width <= 0 || height <= 0) {
            return null;
        }
        Bitmap bitmap = null;
        Bitmap result = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            /**此方法不能对正在录像的视频使用,否则报错.
             * */
            retriever.setDataSource(videoPath);
            bitmap = retriever.getFrameAtTime(-1);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Images.Thumbnails.MICRO_KIND);
        if (bitmap != null) {
            result = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
            bitmap.recycle();
        }
        return result;
    }
}
