package com.zzx.media.bean

import java.io.File

/**@author Tomy
 * Created by Tomy on 2018/10/23.
 */
data class SaveRequest(
        val data: ByteArray?,
        val file: File?
)