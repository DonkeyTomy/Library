package com.zzx.utils.file

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.StatFs
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.support.annotation.RequiresApi
import android.support.v4.content.FileProvider
import timber.log.Timber
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import android.provider.MediaStore.MediaColumns
import android.provider.MediaStore



/**@author Tomy
 * Created by Tomy on 2015/7/30.
 */
object FileUtil {

    fun sortDirTime(path: String, dec: Boolean = true): ArrayList<File> {
        return sortDirTime(File(path), dec)
    }

    /**@param dec if true 则递减排序,反之递增.
     */
    fun sortDirTime(dir: File, dec: Boolean = true): ArrayList<File> {
        val list = ArrayList<File>()

        if (!dir.exists() || !dir.isDirectory) {
            return list
        }
        val files = dir.listFiles()
        if (files == null || files.isEmpty()) {
            return list
        }
        Collections.addAll(list, *files)
        list.sortWith(Comparator { file1, file2 ->
            if (file1.lastModified() < file2.lastModified()) {
                if (dec) -1 else 1
            } else {
                if (dec) 1 else -1
            }
        })
        return list
    }

    fun checkDirExist(dir: File, needCreate: Boolean = false): Boolean {
        val exist = dir.exists() && dir.isDirectory
        if (!exist) {
            if (needCreate) {
                return dir.mkdirs()
            }
        }
        return exist
    }

    fun checkDirExist(dirPath: String, needCreate: Boolean = false): Boolean {
        return checkDirExist(File(dirPath), needCreate)
    }

    fun checkFileExist(filePath: String): Boolean {
        return checkFileExist(File(filePath))
    }

    fun checkFileExist(file: File): Boolean {
        return file.exists() && file.isFile
    }

    fun getDirFileCount(dir: File): Int {
        if (!checkDirExist(dir)) {
            return 0
        }
        return dir.listFiles().size
    }

    fun getDirFreeSpace(dir: File): Long {
        val stateFs = StatFs(dir.absolutePath)
        return stateFs.availableBlocksLong * stateFs.blockSizeLong
    }

    fun getDirFreeSpaceByMB(dir: File): Long {
        return getDirFreeSpace(dir) / 1000 / 1000
    }

    fun getDirFreeSpaceByMB(dir: String): Long {
        return getDirFreeSpace(File(dir)) / 1000 / 1000
    }

    fun getDirTotalSpace(dir: File): Long {
        return dir.totalSpace
    }

    fun getStorageList(): Array<String> {
        val array = Array(2) {
            ""
        }
        var process: Process? = null
        var inputStream: InputStream? = null
        try {
            process = Runtime.getRuntime().exec("mount")
            inputStream = process.inputStream
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String? = null
            while (reader.readLine().apply { line = this } != null) {
                if (line!!.contains("sdcardfs") && line!!.contains("storage")) {
                    if (line!!.contains("data")) {
                        val internalStorage = getStoragePath(line!!)
                        array[0] = internalStorage
                    } else if (line!!.contains("mnt")) {
                        val externalStorage = getStoragePath(line!!)
                        array[1] = externalStorage
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
                process?.destroy()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return array
    }

    fun getExternalStoragePath(): String {
        return getStorageList()[1]
    }

    private fun getStoragePath(line: String): String {
        val list = line.split(" ")
        for (element in list) {
            if (element.contains("storage")) {
                return element
            }
        }
        return ""
    }

    private fun getStorageManager(context: Context): StorageManager {
        return context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
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

    fun deleteFile(file: File) {
        try {
            val isFile = file.isFile
            val cmd = if (isFile) "rm ${file.absolutePath}" else "rm -rf ${file.absolutePath}"
            Timber.e(cmd)
            val process = Runtime.getRuntime().exec(cmd)
            process.waitFor()
            process.destroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteFile(file: String) {
        deleteFile(File(file))
    }

    fun openAssignFile(context: Context, file: File) {
        try {
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getItemContentUri(context, file.absolutePath)
            } else {
                Uri.fromFile(file)
            }
            val intent = Intent(Intent.ACTION_VIEW).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setDataAndType(uri, "image/*")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getItemContentUri(context: Context, mAbsolutePath: String): Uri? {
        val projection = arrayOf(MediaColumns._ID)
        val where = MediaColumns.DATA + " = ?"
        val baseUri = MediaStore.Files.getContentUri("external")
        var c: Cursor? = null
        val provider = "com.android.providers.media.MediaProvider"
        var itemUri: Uri? = null
        context.grantUriPermission(provider, baseUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            c = context.contentResolver.query(baseUri,
                    projection,
                    where,
                    arrayOf(mAbsolutePath), null)
            if (c != null && c.moveToNext()) {
                val type = c.getInt(c.getColumnIndexOrThrow(MediaColumns._ID))
                if (type != 0) {
                    val id = c.getLong(c.getColumnIndexOrThrow(MediaColumns._ID))
                    itemUri = Uri.withAppendedPath(baseUri, id.toString())
                }
            }
        } catch (e: Exception) {
        } finally {
            c?.close()
        }
        return itemUri
    }

    fun openAssignFolder(context: Context, dir: File, authority: String) {
        if (!checkDirExist(dir)) {
            return
        }
        try {
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                FileProvider.getUriForFile(context, authority, dir)
            } else {
                Uri.fromFile(dir)
            }
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setDataAndType(uri, "file/*")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    const val PACKAGE_NAME_FILE    = "com.mediatek.filemanager"
    const val CLASS_NAME_FILE      = "com.mediatek.filemanager.FileManagerOperationActivity"
    const val INTENT_EXTRA_SELECT_PATH = "select_path"

    fun openMtkFolder(context: Context, dir: File) {
        if (!checkDirExist(dir, true)) {
            return
        }
        val intent = Intent().apply {
            setClassName(PACKAGE_NAME_FILE, CLASS_NAME_FILE)
            putExtra(INTENT_EXTRA_SELECT_PATH, dir.absolutePath)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
