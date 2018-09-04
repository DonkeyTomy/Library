package com.zzx.media.utils

import android.content.Context
import android.provider.MediaStore
import com.zzx.utils.rxjava.fixedThread
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**@author Tomy
 * Created by Tomy on 2018/6/14.
 */
class MediaInfoUtils {

    companion object {
        private val mTimeFormatter by lazy {
            SimpleDateFormat(TIME_FORMAT, Locale.getDefault())
        }

        private val mDateFormat by lazy {
            SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        }

        fun getDateDir(): String {
            return mDateFormat.format(Date())
        }


        fun getFileName(suffix: String): String {
            return "${mTimeFormatter.format(Date())}$suffix"
        }

        fun getPictureName(): String {
            return getFileName(PICTURE)
        }

        fun getVideoName(): String {
            return getFileName(VIDEO_MP4)
        }

        fun getTmpFileName(): String {
            return getFileName(FILE_TMP)
        }

        fun getAudioName(): String {
            return getFileName(AUDIO_MP3)
        }

        fun tmpFile2Video(filePath: String) {

        }

        fun tmpFile2Video(file: File?) = fixedThread {
            file?.renameTo(File(file.parent, "${file.nameWithoutExtension}$VIDEO_MP4"))
        }

        fun insertImage(context: Context, file: File) = fixedThread {
            MediaStore.Images.Media.insertImage(context.contentResolver, file.absolutePath, file.name, null)
        }

        const val TIME_FORMAT = "yyyyMMdd-HHmmss"
        const val DATE_FORMAT = "yyyyMMdd"

        const val FILE_TMP  = ".tmp"
        const val VIDEO_MP4 = ".mp4"
        const val AUDIO_MP3 = ".mp3"
        const val AUDIO_AAC = ".aac"
        const val PICTURE   = ".png"
    }

}