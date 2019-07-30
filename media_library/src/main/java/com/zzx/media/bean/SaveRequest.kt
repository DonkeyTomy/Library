package com.zzx.media.bean

import java.io.File

/**@author Tomy
 * Created by Tomy on 2018/10/23.
 */
data class SaveRequest(
        val data: ByteArray?,
        val file: File?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SaveRequest

        if (data != null) {
            if (other.data == null) return false
            if (!data.contentEquals(other.data)) return false
        } else if (other.data != null) return false
        if (file != other.file) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data?.contentHashCode() ?: 0
        result = 31 * result + (file?.hashCode() ?: 0)
        return result
    }
}