package com.zzx.network.ftp

import com.zzx.network.ftp.bean.FileInfo
import com.zzx.network.ftp.callback.DataTransferListener
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

/**@author Tomy
 * Created by Tomy on 2017/10/17.
 */
interface IFtpContinue<File> {

    /**
     * 上传文件.
     * @return 返回队列添加是否成功.若超出容量限制则false.
     * @see getCapacity 获得队列容量.
     * @see getCurrentUploadSize  获得当前队列数量.
     * */
    fun uploadFile(fileInfo: FileInfo): Boolean

    /**从[0]开始加入上传队列.
     * @return 返回未加入的队列的个数(超出队列容量).0代表全部加入
     * */
    fun uploadFileList(fileInfoList: List<FileInfo>): Int

    fun downloadFile(fileInfo: FileInfo): Boolean

    /**从[0]开始加入下载队列.
     * @return 返回未加入的队列的个数(超出队列容量).0代表全部加入
     * */
    fun downloadFileList(fileInfoList: List<FileInfo>): Int

    fun startExecFileList(fileList: ArrayBlockingQueue<FileInfo>, complete: AtomicBoolean, download: Boolean)

    /**
     * 跳转到制定目录.
     * @return false 代表跳转失败
     * */
    fun changeDir(dir: String): Boolean

    /**
     * 跳转到当前目录的父目录.
     * @return false 代表跳转失败
     * */
    fun changeToParentDir(): Boolean

    /**
     * 获得当前目录下的所有文件包括文件夹.可能为null
     * */
    fun listCurrentDir(): Array<File>?

    /**
     * 获得指定录下的所有文件包括文件夹.可能为null
     * */
    fun listSpecialDir(dir: String): Array<File>?

    /***
     * @return 获取当前目录路径
     */
    fun getCurrentDir(): String

    /**
     * 自动后台执行connect 与 login操作.
     * @see connect
     * @see login
     * */
    fun progressConnectAndLogin()

    /***
     * @return 文件是否存在
     */
    fun isFileExists(filePath: String): Boolean

    /***
     * @return 目录是否存在
     */
    fun isDirectoryExists(dirPath: String): Boolean

    /**
     * 登出
     * */
    fun loginOut()

    /***
     * @param ipAddress FTP地址
     * @param port FTP端口，默认21
     */
    fun connect(ipAddress: String, port: Int = 21)

    /**
     * 登录
     * */
    fun login(name: String, pwd: String)

    fun disconnect()

    /***
     * @return 返回上传/下载队列的容量.超过容量的部分不添加.
     */
    fun getCapacity(): Int

    /**@see getCapacity 获得容量限制.
     * @return 返回当前上传队列中的数量.
     * */
    fun getCurrentUploadSize(): Int

    /**
     * @see getCapacity 获得容量限制
     * @return 返回当前下载队列中的数量
     * */
    fun getCurrentDownloadSize(): Int

    /**
     * 设置数据上传监听器
     * @see DataTransferListener
     * */
    fun setUpdateDataTransferListener(uploadListener: DataTransferListener)

    /**
     * 设置数据下载监听器
     * @see DataTransferListener
     * */
    fun setDownloadDataTransferListener(downloadloadListener: DataTransferListener)

}