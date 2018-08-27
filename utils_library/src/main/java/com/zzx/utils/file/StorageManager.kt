package com.zzx.utils.file

import android.content.Context
import android.os.Build
import android.os.storage.StorageVolume
import android.support.annotation.RequiresApi
import timber.log.Timber

/**@author Tomy
 * Created by Tomy on 2018/6/28.
 */
object StorageManager {

    private fun getStorageManager(context: Context): android.os.storage.StorageManager {
        return context.getSystemService(Context.STORAGE_SERVICE) as android.os.storage.StorageManager
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getVolumePaths(context: Context): List<StorageVolume> {
        return getStorageManager(context).storageVolumes
    }

    fun checkExternalStorageMounted(context: Context): Boolean {
        val list = getVolumePaths(context)
        for (volume in list) {
            if (volume.isRemovable)
                return true
        }
        return false
    }

    fun getExternalStoragePath(context: Context): String {
        try {
            val list = getVolumePaths(context)
            val getPathMethod = StorageVolume::class.java.getDeclaredMethod("getPath")
            for (volume in list) {
                if (volume.isRemovable) {
                    return getPathMethod.invoke(volume) as String
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }


}