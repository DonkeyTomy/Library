package com.zzx.utils

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri

import java.io.File

/**@author Tomy
 * Created by Tomy on 2017/3/20.
 */

class MediaScanUtils(context: Context, private val mPath: String) : MediaScannerConnection.MediaScannerConnectionClient {
    private val mConnection: MediaScannerConnection = MediaScannerConnection(context, this)

    init {
        mConnection.connect()
    }

    constructor(context: Context, file: File) : this(context, file.absolutePath)

    override fun onMediaScannerConnected() {
        mConnection.scanFile(mPath, null)
    }

    override fun onScanCompleted(path: String, uri: Uri) {
        mConnection.disconnect()
    }
}
