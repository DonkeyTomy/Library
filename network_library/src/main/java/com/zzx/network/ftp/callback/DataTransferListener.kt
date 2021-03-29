package com.zzx.network.ftp.callback

/**@author Tomy
 * Created by Tomy on 2017/10/16.
 */
interface DataTransferListener : StatusCallback {

    fun onTransferLength(id: Int, len: Long, size: Long)

    fun onTransferError(id: Int, code: Int)

}