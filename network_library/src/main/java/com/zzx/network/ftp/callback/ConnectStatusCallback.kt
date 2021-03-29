package com.zzx.network.ftp.callback

import io.reactivex.annotations.NonNull

/**@author Tomy
 * Created by Tomy on 2017/10/13.
 */
interface ConnectStatusCallback {

    fun onConnectStatus(@NonNull success: Boolean)

    fun onLoginStatus(@State.Status code: Int)

}