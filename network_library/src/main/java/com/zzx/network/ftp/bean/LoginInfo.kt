package com.zzx.network.ftp.bean

/**@author Tomy
 * Created by Tomy on 2017/10/13.
 */
data class LoginInfo(
        val ipAddress: String,
        val ipPort: Int,
        var userName: String,
        var password: String
)