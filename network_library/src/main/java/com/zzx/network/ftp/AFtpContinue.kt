package com.zzx.network.ftp

import com.zzx.network.ftp.bean.FileInfo
import com.zzx.network.ftp.bean.LoginInfo
import com.zzx.network.ftp.callback.ConnectStatusCallback
import com.zzx.network.ftp.callback.DataTransferListener
import com.zzx.network.ftp.callback.State
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import org.apache.commons.net.ftp.FTPReply
import org.reactivestreams.Subscription
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

/**@author Tomy
 * Created by Tomy on 2017/10/13.
 */
class AFtpContinue(var mLoginInfo: LoginInfo, var mConnectCallback: ConnectStatusCallback): IFtpContinue<FTPFile> {

    companion object {
        private const val CAPACITY = 10
    }

    private var mUploadListener: DataTransferListener? = null

    private var mDownloadListener: DataTransferListener? = null

    private var mFtpClient: FTPClient? = null

    private val mUploadList = lazy { return@lazy ArrayBlockingQueue<FileInfo>(CAPACITY) }

    private val mDownloadList = lazy { return@lazy ArrayBlockingQueue<FileInfo>(CAPACITY) }

    private var mUploadComplete     = AtomicBoolean(true)

    private var mDownloadComplete   = AtomicBoolean(true)

    init {
        if (Timber.treeCount() <= 0) {
            Timber.plant(Timber.DebugTree())
        }
        progressConnectAndLogin()
    }

    /**
     * 设置数据上传监听器
     * @see DataTransferListener
     * */
    override fun setUpdateDataTransferListener(uploadListener: DataTransferListener) {
        mUploadListener = uploadListener
    }

    /**
     * 设置数据下载监听器
     * @see DataTransferListener
     * */
    override fun setDownloadDataTransferListener(downloadloadListener: DataTransferListener) {
        mDownloadListener = downloadloadListener
    }

    override fun getCapacity(): Int {
        return CAPACITY
    }

    override fun getCurrentUploadSize(): Int {
        return mUploadList.value.size
    }

    override fun getCurrentDownloadSize(): Int {
        return mDownloadList.value.size
    }

    /**
     * 上传文件.
     * @return 返回队列添加是否成功.若超出容量限制则false.
     * @see getCapacity 获得队列容量.
     * @see getCurrentUploadSize  获得当前队列数量.
     * */
    override fun uploadFile(fileInfo: FileInfo): Boolean {
        val success = mUploadList.value.offer(fileInfo)
        Timber.e("success = $success; uploadComplete = ${mUploadComplete.get()}")
        if (success && mUploadComplete.get()) {
            mUploadComplete.set(false)
            startExecFileList(mUploadList.value, mUploadComplete, false)
        }
        return success
    }

    private fun addFileList(queue: ArrayBlockingQueue<FileInfo>, fileInfoList: List<FileInfo>): Int {
        val leftSize = CAPACITY - queue.size
        return if (leftSize >= fileInfoList.size) {
            queue.addAll(fileInfoList)
            0
        } else {
            (0 until leftSize).mapTo(queue) { fileInfoList[it] }
            fileInfoList.size - leftSize
        }
    }

    /**从[0]开始加入上传队列.
     * @return 返回未加入的队列的个数(超出队列容量).0代表全部加入
     * */
    override fun uploadFileList(fileInfoList: List<FileInfo>): Int {
        val size = addFileList(mUploadList.value, fileInfoList)
        Timber.e("uploadComplete = ${mUploadComplete.get()}")
        if (mUploadComplete.get()) {
            mUploadComplete.set(false)
            startExecFileList(mUploadList.value, mUploadComplete, false)
        }
        return size
    }

    override fun downloadFile(fileInfo: FileInfo): Boolean {
        val success = mDownloadList.value.offer(fileInfo)
        if (success && mDownloadComplete.get()) {
            mDownloadComplete.set(false)
            startExecFileList(mDownloadList.value, mDownloadComplete, true)
        }
        return success
    }

    override fun downloadFileList(fileInfoList: List<FileInfo>): Int {
        val size = addFileList(mDownloadList.value, fileInfoList)
        Timber.e("uploadComplete = ${mDownloadComplete.get()}")
        if (mDownloadComplete.get()) {
            mDownloadComplete.set(false)
            startExecFileList(mDownloadList.value, mDownloadComplete, true)
        }
        return size
    }

    override fun startExecFileList(fileList: ArrayBlockingQueue<FileInfo>, complete: AtomicBoolean, download: Boolean) {
        var subscription: Subscription? = null
        Flowable.create<FileInfo>({
            e: FlowableEmitter<FileInfo> ->
            run {
                Timber.e("startExecFileList.listSize = ${fileList.size}")
                while (fileList.isNotEmpty()) {
                    e.onNext(fileList.poll())
                }
                e.onComplete()
            }
        }, BackpressureStrategy.DROP)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.io())
                .map {
                    fileInfo: FileInfo ->
                    run {
                        if (download) {
                            execDownloadFile(fileInfo)
                        } else {
                            execUploadFile(fileInfo)
                        }
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    subscription?.request(1)
                }, {
                    error: Throwable? ->
                    error?.printStackTrace()
                    complete.set(true)
                }, {
                    Timber.e("onComplete(). fileList.size = ${fileList.size}")
                    subscription?.cancel()
                    if (fileList.size > 0) {
                        startExecFileList(fileList, complete, download)
                    } else {
                        complete.set(true)
                    }
                }, {
                    sub: Subscription? ->
                    subscription = sub
                    subscription?.request(1)
                })
    }

    /**
     * @see execDownloadFile
     * */
    private fun execUploadFile(fileInfo: FileInfo) {
        val file = File(fileInfo.localDir, fileInfo.localName)
        if (!file.exists() || file.isDirectory) {
            mUploadListener?.onTransferError(fileInfo.id, State.ERROR_FILE_NOT_EXISTS)
            return
        }
        if (fileInfo.remoteDir != "") {
            if (!mFtpClient!!.changeWorkingDirectory(fileInfo.remoteDir)) {
                if (!mFtpClient!!.makeDirectory(fileInfo.remoteDir)) {
                    mUploadListener?.onTransferError(fileInfo.id, State.ERROR_MK_DIR_FAILED)
                    return
                }
            }
        } else {
            fileInfo.remoteDir = mFtpClient!!.printWorkingDirectory()
        }

        mUploadListener?.onTransferStart(fileInfo.id)

        var size = 0
        var len: Long = 0
        val totalSize = file.length()
        mFtpClient!!.bufferSize = 10240
        val bufferSize = mFtpClient!!.bufferSize

        val localInput = FileInputStream(file)
        val remotePath = File(fileInfo.remoteDir, fileInfo.remoteName).absolutePath
        val output = mFtpClient!!.storeFileStream(remotePath)
        val buffer  = ByteArray(bufferSize)

        try {
            while(localInput.read(buffer).apply { size = this } != -1) {
                output!!.write(buffer, 0, size)
                mUploadListener?.onTransferLength(fileInfo.id, len.let {
                    len += size
                    return@let len}, totalSize)
            }
            mUploadListener?.onTransferFinish(fileInfo.id)
        } catch (e: Exception) {
            e.printStackTrace()
            mUploadListener?.onTransferError(fileInfo.id, State.ERROR_IO_ERROR_TRANSFER)
        }

        try {
            localInput.close()
            output?.flush()
            output?.close()
        } catch (e: Exception) {
            e.printStackTrace()
            mUploadListener?.onTransferError(fileInfo.id, State.ERROR_IO_ERROR_CLOSE)
        }
        mFtpClient?.completePendingCommand()
    }

    private fun execDownloadFile(fileInfo: FileInfo) {
        val file = FTPFile()
        file.name = File(fileInfo.remoteDir, fileInfo.remoteName).absolutePath
        mDownloadListener?.onTransferStart(fileInfo.id)
        val totalSize = file.size
        val localOutput = FileOutputStream(File(fileInfo.localDir, fileInfo.localName))
        val remoteInput = mFtpClient!!.retrieveFileStream(file.name)
        val buffer  = ByteArray(10240)
        var size = 0
        var len: Long = 0
        try {
            while(remoteInput.read(buffer).apply { size = this } != -1) {
                localOutput.write(buffer, 0, size)
                mDownloadListener?.onTransferLength(fileInfo.id, len.apply { len += size }, totalSize)
            }
            mDownloadListener?.onTransferFinish(fileInfo.id)
        } catch (e: Exception) {
            e.printStackTrace()
            mDownloadListener?.onTransferError(fileInfo.id, State.FAILED)
        }
        mFtpClient?.completePendingCommand()
    }

    override fun changeDir(dir: String): Boolean {
        return mFtpClient?.changeWorkingDirectory(dir) ?: false
    }

    override fun changeToParentDir(): Boolean {
        return mFtpClient?.changeToParentDirectory() ?: false
    }

    override fun listCurrentDir(): Array<FTPFile>? {
        return mFtpClient?.listFiles()
    }

    override fun listSpecialDir(dir: String): Array<FTPFile>? {
        return mFtpClient?.listFiles(dir)
    }

    override fun getCurrentDir(): String {
        return mFtpClient?.printWorkingDirectory() ?: null.toString()
    }

    /**
     * 主动调用此方法需要确保[mLoginInfo]已经设置.
     * */
    override fun progressConnectAndLogin() {
        Observable.just(mLoginInfo)
                .observeOn(Schedulers.io())
                .doOnNext {
                    if (mFtpClient == null) {
                        mFtpClient = FTPClient()
                        mFtpClient?.connectTimeout = 5000
                    }
                }.map {
                    info: LoginInfo -> Int
                        run {
                            try {
                                connect(info.ipAddress, info.ipPort)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                return@map State.ERROR_CONNECT
                            }
                            try {
                                mFtpClient?.enterLocalActiveMode()
                               login(info.userName, info.password)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                try {
                                    mFtpClient?.enterLocalPassiveMode()
                                    login(info.userName, info.password)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    return@map State.ERROR_LOGIN
                                }
                            }

                            val replyCode = mFtpClient?.replyCode

                            if (FTPReply.isPositiveCompletion(replyCode ?: -1)) {
                                return@map State.SUCCESS
                            } else {
                                mFtpClient?.disconnect()
                                return@map State.FAILED
                            }
                        }
                }
//                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Int> {
                        var mDisposable: Disposable? = null
                    override fun onComplete() {

                    }

                    override fun onSubscribe(d: Disposable) {
                        mDisposable = d
                    }

                    override fun onNext(t: Int) {
                        when(t) {
                            State.ERROR_CONNECT -> run {
                                mConnectCallback.onConnectStatus(false)
                                mDisposable?.dispose()
                            }
                            State.ERROR_LOGIN -> run {
                                mConnectCallback.onConnectStatus(true)
                                mConnectCallback.onLoginStatus(State.ERROR_LOGIN)
                                mDisposable?.dispose()
                            }
                            State.FAILED -> run {
                                mConnectCallback.onConnectStatus(true)
                                mConnectCallback.onLoginStatus(State.FAILED)
                                mDisposable?.dispose()
                            }
                            State.SUCCESS -> run {
                                mConnectCallback.onConnectStatus(true)
                                mConnectCallback.onLoginStatus(State.SUCCESS)
                                mDisposable?.dispose()
                            }
                        }
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                    }

                })

    }


    override fun connect(ipAddress: String, port: Int) {
        mFtpClient?.connect(ipAddress, port)
    }

    override fun disconnect() {
        mFtpClient?.disconnect()
    }

    override fun login(name: String, pwd: String) {
        mFtpClient?.login(name, pwd)
    }

    override fun loginOut() {
        mFtpClient?.logout()
    }

    override fun isFileExists(filePath: String): Boolean {
        return false
    }

    override fun isDirectoryExists(dirPath: String): Boolean {
        return false
    }

}