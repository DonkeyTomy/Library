package com.zzx.network.ftp.callback

import android.support.annotation.IntDef

/**@author Tomy
 * Created by Tomy on 2017/10/15.
 */
class State {

    companion object {
        const val FAILED: Int  = -1
        const val SUCCESS: Int = 0

        const val ERROR_CONNECT: Int            = -2
        const val ERROR_LOGIN: Int              = -3
        const val ERROR_IO_ERROR_TRANSFER: Int  = -4
        const val ERROR_MK_DIR_FAILED: Int      = -5
        const val ERROR_FILE_NOT_EXISTS: Int    = -6
        const val ERROR_IO_ERROR_CLOSE: Int     = -7
    }

    @IntDef(State.FAILED, State.SUCCESS)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Status

    @IntDef(State.FAILED, State.SUCCESS)
    @Retention(AnnotationRetention.SOURCE)
    annotation class ErrorCode

}

