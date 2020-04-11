package com.zzx.media.utils

import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.provider.MediaStore
import com.zzx.utils.MediaScanUtils
import com.zzx.utils.rxjava.fixedThread
import com.zzx.utils.zzx.DeviceUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**@author Tomy
 * Created by Tomy on 2018/6/14.
 * 用来获取及修改文件名.
 */
class FileNameUtils {

    companion object {
        private var mPrePictureTime: String = ""

        private var mCount = 0

        private val mTimeFormatter by lazy {
            SimpleDateFormat(TIME_FORMAT, Locale.getDefault())
        }

        private val mDateFormat by lazy {
            SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        }

        fun getDateDir(): String {
            return mDateFormat.format(Date())
        }


        fun getFileName(suffix: String, prefix: String = "", userName: String = ""): String {
            return "$userName$prefix${getFileNameWithoutSuffix()}$suffix"
        }

        fun getFileNameWithoutSuffix(): String {
            return mTimeFormatter.format(Date())
        }

        fun getPictureName(prefix: String = "", userName: String = ""): String {
            var pictureTime = getFileNameWithoutSuffix()
            if (pictureTime == mPrePictureTime) {
                mCount++
                mPrePictureTime = pictureTime
                pictureTime += "_$mCount"
            } else {
                mCount = 0
                mPrePictureTime = pictureTime
            }
            return "$userName$prefix$pictureTime$PICTURE"
        }

        fun getVideoName(): String {
            return getFileName(VIDEO_MP4, TYPE_VIDEO_PRE)
        }

        fun getTmpVideoName(userName: String = ""): String {
            return getFileName(FILE_TMP, TYPE_VIDEO_PRE, userName)
        }

        fun tmpFile2Video(filePath: String) {
            tmpFile2Video(File(filePath))
        }

        fun tmpFile2Video(file: File): File {
            val dstFile = File(file.parent, "${file.nameWithoutExtension}$VIDEO_MP4")
            file.renameTo(dstFile)
            return dstFile
        }

        fun tmpFileName2VideoName(file: File): File {
            return File(file.parent, "${file.nameWithoutExtension}$VIDEO_MP4")
        }


        fun tmpFile2ImpVideo(file: File): File {
            val dstFile = File(file.parent, "${file.nameWithoutExtension}$FILE_IMP$VIDEO_MP4")
            file.renameTo(dstFile)
            return dstFile
        }

        fun tmpFileName2VideoImpName(file: File): File {
            return File(file.parent, "${file.nameWithoutExtension}$FILE_IMP$VIDEO_MP4")
        }


        fun getTmpAudioName(userName: String = ""): String {
            return getFileName(FILE_TMP, TYPE_AUDIO_PRE, userName)
        }

        fun getAudioName(userName: String = ""): String {
            return getFileName(AUDIO_MP3, TYPE_AUDIO_PRE, userName)
        }

        fun tmpFile2Audio(file: File): File {
            val dstFile = File(file.parent, "${file.nameWithoutExtension}$AUDIO_SUB")
            file.renameTo(dstFile)
            return dstFile
        }

        fun tmpFileName2AudioName(file: File): File {
            return File(file.parent, "${file.nameWithoutExtension}$AUDIO_SUB")
        }


        fun tmpFile2ImpAudio(file: File): File {
            val dstFile = File(file.parent, "${file.nameWithoutExtension}$FILE_IMP$AUDIO_SUB")
            file.renameTo(dstFile)
            return dstFile
        }

        fun tmpFileName2AudioImpName(file: File): File {
            return File(file.parent, "${file.nameWithoutExtension}$FILE_IMP$AUDIO_SUB")
        }

        fun insertImage(context: Context, file: File) = fixedThread {
//            MediaStore.Images.Media.insertImage(context.contentResolver, file.absolutePath, file.name, null)
//            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://${file.absolutePath}")))
            MediaScanUtils(context, file)
        }

        fun getUserName(context: Context): String {
            return DeviceUtils.getUserNum(context)
        }

        const val TIME_FORMAT = "yyyyMMdd_HHmmss"
        const val DATE_FORMAT = "yyyyMMdd"

        const val FILE_TMP  = ".tmp"
        const val VIDEO_MP4 = ".mp4"
        const val AUDIO_MP3 = ".mp3"
        const val AUDIO_AAC = ".aac"
        const val AUDIO_SUB = AUDIO_MP3
        const val PICTURE   = ".jpg"
        const val FILE_IMP  = "_IMP"

        const val TYPE_VIDEO_PRE    = "0_"
        const val TYPE_PIC_PRE      = "1_"
        const val TYPE_AUDIO_PRE    = "2_"
        const val TYPE_PIC_CAP      = "Screen_"
    }

}