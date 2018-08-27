package com.zzx.network.ftp.bean

import java.io.File

/**@author Tomy
 * Created by Tomy on 2017/10/17.
 */
data class FileInfo(
        var id: Int,
        var localName: String,
        var localDir: String,
        var remoteName: String = localName,
        var remoteDir: String = ""
) {
    constructor(id: Int, file: File): this(id, file.name, file.parent)
}