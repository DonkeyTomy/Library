package com.zzx.media.platform

import android.location.Location
import android.net.Uri
import java.io.File

/**@author Tomy
 * Created by Tomy on 2019/9/28.
 */
abstract class RequestOperator(var mFile: File): ISaveRequest {

    var mFileType   = 0
    var mTitle: String? = null
    lateinit var mMimeType: String
    var mWidth  = 0
    var mHeight = 0
    var mOrientation    = 0

    var mDateTaken  = 0L
    var mDataSize   = 0L
    var mDuration   = 0L

    var mIgnoreThumbnail    = false

    var mData: ByteArray? = null
    var mUri: Uri? = null
    var mLocation: Location? = null
    var mListener: IFileSaver.OnFileSaveListener? = null

    override fun isIgnoreThumbnail(): Boolean {
        return mIgnoreThumbnail
    }

    override fun getFilePath(): String {
        return mFile.absolutePath
    }

    override fun getDataSize(): Int {
        return mData?.size ?: 0
    }

    override fun getUri(): Uri? {
        return mUri
    }

    override fun getLocation(): Location? {
        return mLocation
    }

    override fun getJpegRotation(): Int {
        return mOrientation
    }


    override fun releaseUri() {
        mUri = null
    }

    override fun setData(data: ByteArray?) {
        mData = data
    }

    override fun setSize(width: Int, height: Int) {
        mWidth  = width
        mHeight = height
    }

    override fun setDuration(duration: Long) {
        mDuration   = duration
    }

    override fun setTag(tag: Int) {
    }

    override fun setFile(file: File) {
        mFile   = file
    }

    override fun getFileName(): String {
        return mFile.name
    }

    override fun setListener(listener: IFileSaver.OnFileSaveListener?) {
        mListener   = listener
    }

    override fun getListener(): IFileSaver.OnFileSaveListener? {
        return mListener
    }

    override fun updateDataTaken(time: Long) {
        mDateTaken  = time
    }


    abstract fun saveToDatabase(r: RequestOperator)

    companion object {
        fun generateMimeType(pictureType: Int): String {
            return when (pictureType) {
                PICTURE_TYPE_MPO, PICTURE_TYPE_MPO_3D   -> MIME_TYPE_MPO
                PICTURE_TYPE_JPS    -> MIME_TYPE_JPS
                PICTURE_TYPE_RAW    -> MIME_TYPE_RAW
                else    -> MIME_TYPE_PIC
            }
        }

        const val PICTURE_TYPE_JPG = 0
        const val PICTURE_TYPE_MPO = 1
        const val PICTURE_TYPE_JPS = 2
        const val PICTURE_TYPE_MPO_3D = 3
        const val PICTURE_TYPE_RAW = 4

        const val MIME_TYPE_MPO = "image/mpo"
        const val MIME_TYPE_JPS = "image/x-jps"
        const val MIME_TYPE_RAW = "image/x-adobe-dng"
        const val MIME_TYPE_PIC = "image/jpeg"
    }
}