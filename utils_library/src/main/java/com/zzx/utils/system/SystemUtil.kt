package com.zzx.utils.system

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.Environment
import android.os.Message
import android.os.StatFs
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.widget.Toast
import java.io.*
import java.lang.reflect.Method
import java.text.SimpleDateFormat
import java.util.*

/**@author Tomy
 * Created by Tomy on 13-12-26.
 */
class SystemUtil {

    internal inner class Receiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val value = intent.getIntExtra(NEW_NETWORK_MODE, -1)
            if (value == -1) {
                return
            }
            setPreferredNetworkType(value, null)
        }
    }

    internal fun setPreferredNetworkType(networkType: Int, response: Message?) {}

    companion object {

        const val MOBILE = "MOBILE"
        const val CLASS_SYSTEM_PROPERTIES = "android.os.SystemProperties"
        const val METHOD_GET = "get"
        const val METHOD_SET = "set"
        private const val TIME_FORMAT = "yyyyMMkk_ddmmss"
        /**
         * 配置是否打开本地Log记录.
         * 只有在唤醒后打开(包括上电及后台调用拍照录像功能),休眠前需要关闭.
         */
        var DEBUG = true
        const val VERSION_CODE = "zzx.software.version"
        const val MODEL_CODE = "zzx.product.model"

        /**
         * Flag parameter for [.uninstallPackage] to indicate that you want the
         * package deleted for all users.
         *
         */
        const val DELETE_ALL_USERS = 0x00000002

        fun uninstallPackage(context: Context, pkgName: String) {
            val mManager = context.packageManager
            try {
                val packageClass = mManager.javaClass
                val methods = packageClass.methods
                for (method in methods) {
                    if (method.name == "deletePackage") {
                        method.isAccessible = true
                        method.invoke(mManager, pkgName, null, DELETE_ALL_USERS)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        const val NETWORK_MODE = "preferred_network_mode"
        private const val NETWORK_MODE_GSM_ONLY = 1 // GSM only
        private const val NETWORK_MODE_WCDMA_PREF = 0 //GSM/WCDMA (WCDMA preferred)

        /**
         * 设置网络模式
         */
        fun setPreferredNetworkType(context: Context, mode: Int) {
            //<uses-permission android:name="android.permission.WRITE_SECURE_SETTINGS"/>
            Settings.Global.putInt(context.contentResolver, NETWORK_MODE, mode)
            // change mode
            /*Intent intent = new Intent(CHANGE_NETWORK_MODE);
        intent.putExtra(NEW_NETWORK_MODE, mode);
        context.sendBroadcast(intent);*/
        }

        private const val EVENT_SET_NETWORK_MODE_DONE = 102
        private const val CHANGE_NETWORK_MODE = "com.zzx.phone.CHANGE_NETWORK_MODE"
        private const val NEW_NETWORK_MODE = "com.zzx.phone.NEW_NETWORK_MODE"

        fun setAirPlaneMode(context: Context, enabled: Boolean) {
            try {
                Settings.Global.putInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON,
                        if (enabled) 1 else 0)
                val intent = Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED)
                intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING)
                intent.putExtra("state", enabled)
                context.sendBroadcast(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        fun getProcessName(pid: Int): String? {
            var reader: BufferedReader? = null
            try {
                reader = BufferedReader(FileReader("/proc/$pid/cmdline"))
                var processName = reader.readLine()
                if (!TextUtils.isEmpty(processName)) {
                    processName = processName.trim { it <= ' ' }
                }
                return processName
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            } finally {
                try {
                    reader?.close()
                } catch (exception: IOException) {
                    exception.printStackTrace()
                }

            }
            return null
        }

        @SuppressLint("PrivateApi")
        fun getSystemProperties(field: String): String {
            var value = ""
            try {
                val classType = Class.forName(CLASS_SYSTEM_PROPERTIES)
                val getMethod = classType.getDeclaredMethod(METHOD_GET, String::class.java)
                value = getMethod.invoke(classType, field) as String
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return value
        }

        @SuppressLint("PrivateApi")
        fun setSystemProperties(key: String, `val`: String) {
            try {
                @SuppressLint("PrivateApi")
                val classType = Class.forName(CLASS_SYSTEM_PROPERTIES)
                val setMethod = classType.getDeclaredMethod(METHOD_SET, String::class.java, String::class.java)
                setMethod.invoke(classType, key, `val`)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        fun goToHome(context: Context) {
            val homeIntent = Intent(Intent.ACTION_MAIN)
            homeIntent.addCategory(Intent.CATEGORY_HOME)
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            context.startActivity(homeIntent)
        }

        fun getMethod(`object`: Any, method: String): Any {
            var lteDbm = Any()
            try {
                val getLteDbm = `object`.javaClass.getMethod(method)
                lteDbm = getLteDbm.invoke(`object`)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return lteDbm
        }

        fun setMobileData(context: Context?, enabled: Boolean) {
            if (context == null)
                return
            if (isMobileDataEnabled(context)) {
                if (enabled)
                    return
            } else if (!enabled) {
                return
            }
            val manager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val setMethod: Method
            try {
                setMethod = TelephonyManager::class.java.getDeclaredMethod("setDataEnabled", java.lang.Boolean.TYPE)
                setMethod.isAccessible = true
                setMethod.invoke(manager, enabled)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        fun isWifiApEnabled(context: Context): Boolean {
            try {
                val manager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val isApEnabled = manager.javaClass.getMethod("isWifiApEnabled")
                return isApEnabled.invoke(manager) as Boolean
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return false
        }

        @SuppressLint("HardwareIds")
        fun getIMEI(context: Context?): String {
            if (context == null) {
                return ""
            }
            val imei = try {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return ""
                }
                (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).deviceId
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }

            return imei ?: ""
        }

        private const val CLASS_TELEPHONY = "com.mediatek.telephony.TelephonyManagerEx"
        private const val CLASS_SIM = "com.mediatek.telephony.SimInfoManager"
        private const val METHOD_GET_TELEPHONY = "getDefault"
        private const val METHOD_SET_ROAM = "setDataRoamingEnabled"
        private const val METHOD_SET_ROAM_SIM = "setDataRoaming"

        /*public static void setDataRoamingEnabled(Context context, boolean enabled) {
        try {
            Class<?> telClass = Class.forName(CLASS_TELEPHONY);
            Method getMethod = telClass.getMethod(METHOD_GET_TELEPHONY);
            Object telObject = getMethod.invoke(null);
            Method setRoamMethod = telClass.getDeclaredMethod(METHOD_SET_ROAM, Boolean.TYPE, Integer.TYPE);
            setRoamMethod.invoke(telObject, enabled, 0);

            Class<?> telClass1 = Class.forName(CLASS_SIM);
            Method setRoamMethod1 = telClass1.getDeclaredMethod(METHOD_SET_ROAM_SIM, Context.class, Integer.TYPE, Long.TYPE);
            setRoamMethod1.invoke(null, context, enabled ? 1 : 0, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/


        @SuppressLint("HardwareIds")
        fun getICCID(context: Context?): String {
            if (context == null) {
                return ""
            }
            var iccid: String?
            try {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return ""
                }
                iccid = (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).simSerialNumber
            } catch (e: Exception) {
                e.printStackTrace()
                iccid = ""
            }

            return iccid ?: ""
        }

        fun isMobileNetworkConnected(context: Context): Boolean {
            if (isNetworkConnected(context)) {
                val networkType = getNetworkType(context)
                return ConnectivityManager.TYPE_MOBILE == networkType
            }
            return false
        }

        fun isMobileDataEnabled(context: Context): Boolean {
            val manager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            try {
                val getMobileDataEnabled = manager.javaClass.getMethod("getDataEnabled")
                return getMobileDataEnabled.invoke(manager) as Boolean
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return false
        }

        fun isWifiNetworkConnected(context: Context): Boolean {
            if (isNetworkConnected(context)) {
                val networkType = getNetworkType(context)
                return ConnectivityManager.TYPE_WIFI == networkType
            }
            return false
        }

        fun isNetworkConnected(context: Context): Boolean {
            val networkInfo = getActiveNetwork(context)
            return if (null != networkInfo) {
                networkInfo.isAvailable && networkInfo.isConnected
            } else false
        }

        @SuppressLint("PrivateApi")
        fun closeRecentTask(context: Context) {
            val manager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            val pm = context.packageManager
            val removeTask: Method
            try {
                val list = manager.getRecentTasks(20, ActivityManager.RECENT_IGNORE_UNAVAILABLE or ActivityManager.RECENT_WITH_EXCLUDED)
                if (list != null && list.size > 0) {
                    val homeInfo = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).resolveActivityInfo(pm, 0)
                    removeTask = ActivityManager::class.java.getDeclaredMethod("removeTask", Integer.TYPE)
                    for (taskInfo in list) {
                        val intent = Intent(taskInfo.baseIntent)
                        if (taskInfo.origActivity != null) {
                            intent.component = taskInfo.origActivity
                        }
                        if (homeInfo != null) {
                            if (homeInfo.packageName == intent.component!!.packageName && homeInfo.name == intent.component!!.className) {
                                continue
                            }
                        }
                        removeTask.invoke(manager, taskInfo.persistentId)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        fun getActiveNetwork(context: Context): NetworkInfo? {
            val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return manager.activeNetworkInfo
        }

        fun getNetworkType(context: Context): Int {
            val networkInfo = getActiveNetwork(context)
            return networkInfo!!.type
        }

        const val CLASS_SMS_MANAGER = "com.android.internal.telephony.SmsApplication"
        const val METHOD_SET_DEFAULT = "setDefaultApplication"
        /**设置默认短信收发应用.
         * @param pkgName 要设置的默认短信的包名.
         */
        @SuppressLint("PrivateApi")
        fun setDefaultSms(context: Context, pkgName: String) {
            try {
                val classType = Class.forName(CLASS_SMS_MANAGER)
                val setMethod = classType.getDeclaredMethod(METHOD_SET_DEFAULT, String::class.java, Context::class.java)
                setMethod.invoke(classType, pkgName, context)
                /*Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
                    putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, pkgName)
                    context.startActivity(this)
                }*/
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun setTimeOut(context: Context, sec: Int) {
            Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, sec * 1000)
        }

        /**
         * @param msgId R.string.xxx. If this is set, param msg will be ignore.
         */
        fun makeToast(context: Context, msgId: Int, msg: String) {
            if (msgId != 0) {
                Toast.makeText(context, context.getString(msgId), Toast.LENGTH_SHORT).show()
            } else
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }

        /** StatFs获取该路径文件标识在Unix文件系统中包含的所有信息  *///文件系统中每个块的大小,单位是byte
        //块的个数
        //包括预留的,应用不可用的块的个数
        //只包含应用可用的块的个数
        val flashSize: Long
            get() {
                val dataDir = Environment.getDataDirectory()
                val statFs = StatFs(dataDir.path)
                val blockSize = statFs.blockSize.toLong()
                val blockCount = statFs.blockCount.toLong()
                val freeSize = statFs.freeBlocks.toLong()
                val availbleSize = statFs.availableBlocks.toLong()
                return blockSize * blockCount
            }

        /**@param free true则表示只获得应用可用的大小,反之返回所有大小.
         * 单位为MB.
         */
        fun getExternalStorageSize(free: Boolean): Float {
            try {
                val size: Long
                val sdcardDir = File("/sdcard")
                val statFs = StatFs(sdcardDir.absolutePath)
                val blockSize = statFs.blockSize.toLong()//文件系统中每个块的大小,单位是byte
                val blockCount = statFs.blockCount.toLong()//块的个数
                val availableSize = statFs.availableBlocks.toLong()//只包含应用可用的块的个数
                size = if (!free) {
                    blockSize * blockCount
                } else {
                    blockSize * availableSize
                }
                return size.toFloat() / 1024.0f / 1024.0f
            } catch (e: Exception) {
                e.printStackTrace()
                return 0f
            }

        }

        //块的个数
        //只包含应用可用的块的个数
        val freeStoragePercent: Int
            get() {
                try {
                    val sdcardDir = File("/sdcard")
                    val statFs = StatFs(sdcardDir.absolutePath)
                    val blockCount = statFs.blockCountLong
                    val availableSize = statFs.availableBlocksLong.toFloat()
                    val percent = availableSize / blockCount
                    if (percent > 0.01)
                        return (percent * 100).toInt()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                return 0
            }

        fun isFileExists(filePath: String): Boolean {
            val file = File(filePath)
            return file.exists()
        }

        private var mLogWriter: FileWriter? = null

        private val mObjectLock = Any()

        fun writeLog(dir: String, msg: String) {
            var formatter = SimpleDateFormat(TIME_FORMAT, Locale.getDefault())
            val time = formatter.format(System.currentTimeMillis())
            try {
                if (mLogWriter == null) {
                    formatter = SimpleDateFormat("yyyyMMdd", Locale.CHINA)
                    val log = File(dir, formatter.format(System.currentTimeMillis()))
                    if (!log.exists()) {
                        log.createNewFile()
                    }
                    mLogWriter = FileWriter(log, true)
                }
                synchronized(mObjectLock) {
                    mLogWriter!!.write("$time: ")
                    mLogWriter!!.write(msg + "\n")
                    mLogWriter!!.flush()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (mLogWriter != null) {
                    try {
                        mLogWriter!!.close()
                    } catch (e1: Exception) {
                        e1.printStackTrace()
                    }

                    mLogWriter = null
                }
            }

        }

        fun releaseLog() {
            synchronized(mObjectLock) {
                DEBUG = false
                if (mLogWriter != null) {
                    try {
                        mLogWriter!!.close()
                    } catch (e1: Exception) {
                        e1.printStackTrace()
                    }

                    mLogWriter = null
                }
            }
        }

        fun writeLog(context: Context, msg: String) {
            if (false) {
                val file = context.cacheDir
                writeLog(file.absolutePath, msg)
            }
        }

        fun writeLogReboot(context: Context, msg: String) {
            val file = context.cacheDir
            writeLog(file.absolutePath, msg)
        }

        /**
         * 写Log可能会导致休眠失败.
         */
        fun writeLog(context: Context, TAG: String, msg: String) {
            if (DEBUG) {
                val file = context.cacheDir
                writeLog(file.absolutePath, TAG + msg)
            }
        }
    }
}
