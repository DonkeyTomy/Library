package com.zzx.network.ftp.callback

/**@author Tomy
 * Created by Tomy on 2017/10/13.
 */
interface StatusCallback {

    fun onTransferStart(id: Int)

    fun onTransferFinish(id: Int)

}