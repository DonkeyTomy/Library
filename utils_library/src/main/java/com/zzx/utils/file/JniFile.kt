package com.zzx.utils.file


/**@author Tomy
 * Created by Tomy on 2019/5/10.
 */
class JniFile {

    external fun open(path: String): Boolean

    external fun write(msg: String): Boolean

    external fun close()

    external fun  writeOnce(path: String, msg: String): Boolean

    companion object {
        init {
            System.loadLibrary("jni-lib")
        }
    }
}