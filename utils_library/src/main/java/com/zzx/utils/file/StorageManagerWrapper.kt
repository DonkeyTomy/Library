@file:Suppress("UNCHECKED_CAST")

package com.zzx.utils.file

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.support.annotation.RequiresApi
import android.text.TextUtils
import io.reactivex.Observable
import timber.log.Timber
import java.util.concurrent.TimeUnit


/**@author Tomy
 * Created by Tomy on 2018/6/28.
 */
@RequiresApi(Build.VERSION_CODES.N)
@SuppressLint("PrivateApi")
object StorageManagerWrapper {

    /*************** StorageVolume *****************/
    private val mGetPathMethod by lazy {
        StorageVolume::class.java.getDeclaredMethod("getPath")
    }

    private val mStorageVolumeGetIdMethod by lazy {
        StorageVolume::class.java.getDeclaredMethod("getId")
    }

    private val mStorageVolumeGetUUIDMethod by lazy {
        StorageVolume::class.java.getDeclaredMethod("getUuid")
    }
    /***************************************/


    /*************** StorageManager *****************/
    private val mGetVolumeStateMethod by lazy {
        StorageManager::class.java.getDeclaredMethod("getVolumeState", String::class.java)
    }

    private val mFindDiskByIdMethod by lazy {
        StorageManager::class.java.getDeclaredMethod("findDiskById", String::class.java)
    }

    private val mFindVolumeByIdMethod by lazy {
        StorageManager::class.java.getDeclaredMethod("findVolumeById", String::class.java)
    }

    private val mGetDiskMethod by lazy {
        StorageManager::class.java.getDeclaredMethod("getDisks")
    }
    private val mWipeAdoptableDisksMethod by lazy {
        StorageManager::class.java.getDeclaredMethod("wipeAdoptableDisks")
    }
    /***************************************/

    /*************** DiskInfo *****************/
    private val mDiskInfoClass by lazy {
        Class.forName("android.os.storage.DiskInfo")
    }

    private val mDiskInfoGetIdMethod by lazy {
        mDiskInfoClass.getDeclaredMethod("getId")
    }
    /***************************************/


    /*************** VolumeInfo *****************/
    private val mVolumeInfoClass by lazy {
        Class.forName("android.os.storage.VolumeInfo")
    }

    private val mVolumeGetId by lazy {
        mVolumeInfoClass.getMethod("getId")
    }

    private val mVolumeGetDiskId by lazy {
        mVolumeInfoClass.getMethod("getDiskId")
    }
    /***************************************/


    private fun getStorageManager(context: Context): StorageManager {
        return context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getVolumeList(context: Context): List<StorageVolume> {
        return getStorageManager(context).storageVolumes
    }

    fun getVolumePaths(context: Context): Array<String> {
        val getVolumePathsMethod = StorageManager::class.java.getDeclaredMethod("getVolumePaths")
        return getVolumePathsMethod.invoke(getStorageManager(context)) as Array<String>
    }


    fun mountStorage(context: Context) {
        try {
            if (FileUtil.checkExternalStorageMountable(context)) {
                val getMountMethod = StorageManager::class.java.getDeclaredMethod("mount", String::class.java)
                val id = getExternalStoragePathId(context)
                Timber.d("start mount storageID: $id")
                if (!TextUtils.isEmpty(id)) {
                    getMountMethod.invoke(getStorageManager(context), id)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun unmountStorage(context: Context) {
        try {
            if (FileUtil.checkExternalStorageMounted(context)) {
                val getUnmountMethod = StorageManager::class.java.getDeclaredMethod("unmount", String::class.java)
                val id = getExternalStoragePathId(context)
                Timber.d("start unmount storageID: $id")
                if (!TextUtils.isEmpty(id)) {
                    getUnmountMethod.invoke(getStorageManager(context), id)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun getExternalStoragePathId(context: Context): String {
        try {
            val getPathMethod = StorageVolume::class.java.getDeclaredMethod("getId")
            getExternalStorageVolume(context)?.let {
                return getPathMethod.invoke(it) as String
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun getExternalStoragePath(context: Context): String {
        try {
            getExternalStorageVolume(context)?.let {
                return getVolumePath(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun getExternalStorageVolume(context: Context): StorageVolume? {
        getVolumeList(context).forEach {
            if (it.isRemovable) {
                return it
            }
        }
        return null
    }

    fun getVolumePath(volume: StorageVolume): String {
        return mGetPathMethod.invoke(volume) as String
    }

    fun getVolumeState(context: Context, volumePath: String): String {
        return mGetVolumeStateMethod.invoke(getStorageManager(context), volumePath) as String
    }

    fun getDisks(context: Context) {
        val storageManager = getStorageManager(context)
        val diskList = mGetDiskMethod.invoke(storageManager) as List<*>
        diskList.forEach {
            val id = mDiskInfoGetIdMethod.invoke(it) as String
            Timber.e("diskId = $id; diskInfo = $it")
        }

        getVolumeList(context).forEach {
            val id = mStorageVolumeGetIdMethod.invoke(it) as String
            Timber.e("storageVolumeId = $id; path = ${getVolumePath(it)}")
            val diskInfo = mFindVolumeByIdMethod.invoke(storageManager, id)
            diskInfo?.apply {
                val diskId = mVolumeGetDiskId.invoke(diskInfo)
                Timber.e("storage DiskId = $diskId")
            }
        }
    }

    fun getExternalDiskId(context: Context): String? {
        getExternalStorageVolume(context)?.apply {
            val id = mStorageVolumeGetIdMethod.invoke(this) as String
            Timber.e("storageVolumeId = $id")
            val volumeInfo = mFindVolumeByIdMethod.invoke(getStorageManager(context), id)
            volumeInfo?.apply {
                val diskId = mVolumeGetDiskId.invoke(volumeInfo) as String
                Timber.e("storage DiskId = $diskId")
                return diskId
            }
        }
        return null
    }

    fun getExternalDiskUuid(context: Context): String? {
        try {
            getExternalStorageVolume(context)?.apply {
                return uuid
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }




    fun formatStorage(context: Context, preExecFun: () -> Int, delayInMill: Long = 500) {
//        Looper.prepare()
        /*context.sendBroadcast(Intent("StopRecord"))
        var messageDialog: Dialog? = null
        Observable.just(context)
                .observeOn(AndroidSchedulers.mainThread())
                .map {
                    messageDialog = ProgressDialog(context).apply {
                        setTitle("")
                        setMessage(context.getString(R.string.formatting))
                        setCanceledOnTouchOutside(false)
                        setCancelable(false)
                    }
                    messageDialog?.show()
                    TTSToast.showToast(R.string.formatting)
                }
                .delay(700, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .map {
                    if (FileUtil.checkExternalStorageMounted(context)) {
                        FileUtil.deleteFile(FileUtil.getExternalStoragePath(context))
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    messageDialog?.dismiss()
                }*/
//        Looper.loop()
        try {
            Observable.just(Unit)
                    .map {
                        preExecFun()
                    }
                    .delay(delayInMill, TimeUnit.MILLISECONDS)
                    .subscribe {
                        getExternalDiskId(context)?.let {
                            Intent().apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                setClassName("com.android.settings", "com.android.settings.deviceinfo.StorageWizardFormatProgress")
                                putExtra(EXTRA_DISK_ID, it)
                                putExtra(EXTRA_FORMAT_PRIVATE, false)
                                val uuid = getExternalDiskUuid(context)
                                Timber.e("uuid = $uuid")
                                uuid?.let {
                                    putExtra(EXTRA_FORGET_UUID, it)
                                }
                                context.startActivity(this)
                            }
                        }
                    }

            /*val intent = Intent(ACTION_FACTORY_RESET)
            intent.setPackage("android")
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            intent.putExtra(EXTRA_REASON, "OnlyWipeStorage")
            intent.putExtra(EXTRA_WIPE_EXTERNAL_STORAGE, true)
            intent.putExtra(EXTRA_WIPE_ESIMS, true)
            context.sendBroadcast(intent)*/
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    const val EXTRA_DISK_ID = "android.os.storage.extra.DISK_ID"
    const val EXTRA_FORMAT_PRIVATE = "format_private"
    const val EXTRA_FORGET_UUID = "forget_uuid"
    const val ACTION_FACTORY_RESET = "android.intent.action.FACTORY_RESET"
    const val EXTRA_REASON = "android.intent.extra.REASON"
    const val EXTRA_WIPE_EXTERNAL_STORAGE = "android.intent.extra.WIPE_EXTERNAL_STORAGE"
    const val EXTRA_WIPE_ESIMS = "com.android.internal.intent.extra.WIPE_ESIMS"
}