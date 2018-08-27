package com.zzx.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Message;
import android.os.StatFs;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import static android.content.Context.ACTIVITY_SERVICE;

/**@author Tomy
 * Created by Tomy on 13-12-26.
 */
public class SystemUtil {

    public static final String MOBILE = "MOBILE";
    public static final String CLASS_SYSTEM_PROPERTIES = "android.os.SystemProperties";
    public static final String METHOD_GET = "get";
    public static final String METHOD_SET = "set";
    private static final String TIME_FORMAT = "yyyyMMkk_ddmmss";
    /**
     * 配置是否打开本地Log记录.
     * 只有在唤醒后打开(包括上电及后台调用拍照录像功能),休眠前需要关闭.
     * */
    public static boolean DEBUG = true;
    public static final String VERSION_CODE = "zzx.software.version";
    public static final String MODEL_CODE = "zzx.product.model";

    /**
     * Flag parameter for {@link #uninstallPackage} to indicate that you want the
     * package deleted for all users.
     *
     */
    public static final int DELETE_ALL_USERS = 0x00000002;

    public static void uninstallPackage(Context context, String pkgName) {
        PackageManager mManager = context.getPackageManager();
        try {
            Class packageClass = mManager.getClass();
            Method[] methods = packageClass.getMethods();
            for (Method method : methods) {
                if (method.getName().equals("deletePackage")) {
                    method.setAccessible(true);
                    method.invoke(mManager, pkgName, null, DELETE_ALL_USERS);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final String NETWORK_MODE = "preferred_network_mode";
    private static final int NETWORK_MODE_GSM_ONLY = 1; // GSM only
    private static final int NETWORK_MODE_WCDMA_PREF = 0; //GSM/WCDMA (WCDMA preferred)

    /**
     * 设置网络模式
     */
    public static void setPreferredNetworkType(Context context, int mode) {
        //<uses-permission android:name="android.permission.WRITE_SECURE_SETTINGS"/>
        Settings.Global.putInt(context.getContentResolver(), NETWORK_MODE, mode);
        // change mode
        /*Intent intent = new Intent(CHANGE_NETWORK_MODE);
        intent.putExtra(NEW_NETWORK_MODE, mode);
        context.sendBroadcast(intent);*/
    }

    private static final int EVENT_SET_NETWORK_MODE_DONE = 102;
    private static final String CHANGE_NETWORK_MODE = "com.zzx.phone.CHANGE_NETWORK_MODE";
    private static final String NEW_NETWORK_MODE = "com.zzx.phone.NEW_NETWORK_MODE";

    public static void setAirPlaneMode(Context context, boolean enabled) {
        try {
            Settings.Global.putInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON,
                    enabled ? 1 : 0);
            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
            intent.putExtra("state", enabled);
            context.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int value = intent.getIntExtra(NEW_NETWORK_MODE, -1);
            if (value == -1) {
                return;
            }
            setPreferredNetworkType(value, null);
        }
    }

    void setPreferredNetworkType(int networkType, Message response) {
    }

    /*public static String getSystemProperties(String field) {
        String value = "";
        try {
            Class<?> classType = Class.forName(CLASS_SYSTEM_PROPERTIES);
            Method getMethod = classType.getDeclaredMethod(METHOD_GET, String.class);
            value = (String) getMethod.invoke(classType, field);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }*/

    /*public static void setSystemProperties(String key, String val) {
        try {
            Class<?> classType = Class.forName(CLASS_SYSTEM_PROPERTIES);
            Method setMethod = classType.getDeclaredMethod(METHOD_SET, String.class, String.class);
            setMethod.invoke(classType, key, val);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    public static void goToHome(Context context) {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        context.startActivity(homeIntent);
    }

    public static Object getMethod(Object object, String method) {
        Object lteDbm = new Object();
        try {
            Method getLteDbm = object.getClass().getMethod(method);
            lteDbm = getLteDbm.invoke(object);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lteDbm;
    }

    public static void setMobileData(Context context, boolean enabled) {
        if (context == null)
            return;
        if (isMobileDataEnabled(context)) {
            if (enabled)
                return;
        } else if (!enabled) {
            return;
        }
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        Method setMethod;
        try {
            setMethod = TelephonyManager.class.getDeclaredMethod("setDataEnabled", Boolean.TYPE);
            setMethod.setAccessible(true);
            setMethod.invoke(manager, enabled);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isWifiApEnabled(Context context) {
        try {
            WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            Method isApEnabled = manager.getClass().getMethod("isWifiApEnabled");
            return (boolean) isApEnabled.invoke(manager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @SuppressLint("HardwareIds")
    public static String getIMEI(Context context) {
        if (context == null) {
            return "";
        }
        String imei;
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return "";
            }
            imei = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        } catch (Exception e) {
            e.printStackTrace();
            imei = "";
        }
        return imei != null ? imei : "";
    }

    private static final String CLASS_TELEPHONY = "com.mediatek.telephony.TelephonyManagerEx";
    private static final String CLASS_SIM = "com.mediatek.telephony.SimInfoManager";
    private static final String METHOD_GET_TELEPHONY = "getDefault";
    private static final String METHOD_SET_ROAM = "setDataRoamingEnabled";
    private static final String METHOD_SET_ROAM_SIM = "setDataRoaming";

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
    public static String getICCID(Context context) {
        if (context == null) {
            return "";
        }
        String iccid;
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return "";
            }
            iccid = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getSimSerialNumber();
        } catch (Exception e) {
            e.printStackTrace();
            iccid = "";
        }
        return iccid != null ? iccid : "";
    }

    public static boolean isMobileNetworkConnected(Context context) {
        if (isNetworkConnected(context)) {
            int networkType = getNetworkType(context);
            return ConnectivityManager.TYPE_MOBILE == networkType;
        }
        return false;
    }

    public static boolean isMobileDataEnabled(Context context) {
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Method getMobileDataEnabled = manager.getClass().getMethod("getDataEnabled");
            return (boolean) getMobileDataEnabled.invoke(manager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isWifiNetworkConnected(Context context) {
        if (isNetworkConnected(context)) {
            int networkType = getNetworkType(context);
            return ConnectivityManager.TYPE_WIFI == networkType;
        }
        return false;
    }

    public static boolean isNetworkConnected(Context context) {
        NetworkInfo networkInfo = getActiveNetwork(context);
        if (null != networkInfo) {
            return networkInfo.isAvailable() && networkInfo.isConnected();
        }
        return false;
    }

    @SuppressLint("PrivateApi")
    public static void closeRecentTask(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        PackageManager pm = context.getPackageManager();
        Method removeTask;
        try {
            List<ActivityManager.RecentTaskInfo> list = manager.getRecentTasks(20, ActivityManager.RECENT_IGNORE_UNAVAILABLE | ActivityManager.RECENT_WITH_EXCLUDED);
            if (list != null && list.size() > 0) {
                ActivityInfo homeInfo = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).resolveActivityInfo(pm, 0);
                removeTask = ActivityManager.class.getDeclaredMethod("removeTask", Integer.TYPE);
                for (ActivityManager.RecentTaskInfo taskInfo : list) {
                    Intent intent = new Intent(taskInfo.baseIntent);
                    if (taskInfo.origActivity != null) {
                        intent.setComponent(taskInfo.origActivity);
                    }
                    if (homeInfo != null) {
                        if (homeInfo.packageName.equals(intent.getComponent().getPackageName())
                                && homeInfo.name.equals(intent.getComponent().getClassName())) {
                            continue;
                        }
                    }
                    removeTask.invoke(manager, taskInfo.persistentId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static NetworkInfo getActiveNetwork(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return manager.getActiveNetworkInfo();
    }

    public static int getNetworkType(Context context) {
        NetworkInfo networkInfo = getActiveNetwork(context);
        return networkInfo.getType();
    }

    public static final String CLASS_SMS_MANAGER = "com.android.internal.telephony.SmsApplication";
    public static final String METHOD_SET_DEFAULT = "setDefaultApplication";
    /**设置默认短信收发应用.
     * @param pkgName 要设置的默认短信的包名.
     * */
    public static void setDefaultSms(Context context, String pkgName) {
        /*try {
            Class<?> smsClass = Class.forName(CLASS_SMS_MANAGER);
            Method method = smsClass.getMethod(METHOD_SET_DEFAULT, String.class, Context.class);
            method.invoke(null, pkgName, context);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        /*Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
        startActivity(intent);*/
    }

    public static void setTimeOut(Context context, int sec) {
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, sec * 1000);
    }

    /**
     *@param msgId R.string.xxx. If this is set, param msg will be ignore.
     * */
    public static void makeToast(Context context, int msgId, String msg) {
        if (msgId != 0) {
            Toast.makeText(context, context.getString(msgId), Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static long getFlashSize() {
        /** StatFs获取该路径文件标识在Unix文件系统中包含的所有信息 **/
        File dataDir = Environment.getDataDirectory();
        StatFs statFs = new StatFs(dataDir.getPath());
        long blockSize = statFs.getBlockSize();//文件系统中每个块的大小,单位是byte
        long blockCount = statFs.getBlockCount();//块的个数
        long freeSize = statFs.getFreeBlocks();//包括预留的,应用不可用的块的个数
        long availbleSize = statFs.getAvailableBlocks();//只包含应用可用的块的个数
        return blockSize * blockCount;
    }

    /**@param free true则表示只获得应用可用的大小,反之返回所有大小.
     * 单位为MB.
     * */
    public static float getExternalStorageSize(boolean free) {
        try {
            long size;
            File sdcardDir = new File("/sdcard");
            StatFs statFs = new StatFs(sdcardDir.getAbsolutePath());
            long blockSize = statFs.getBlockSize();//文件系统中每个块的大小,单位是byte
            long blockCount = statFs.getBlockCount();//块的个数
            long availableSize = statFs.getAvailableBlocks();//只包含应用可用的块的个数
            if (!free) {
                size = blockSize * blockCount;
            } else {
                size = blockSize * availableSize;
            }
            return size / 1024.0f / 1024.0f;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    public static int getFreeStoragePercent() {
        try {
            File sdcardDir = new File("/sdcard");
            StatFs statFs = new StatFs(sdcardDir.getAbsolutePath());
            float blockCount = statFs.getBlockCount();//块的个数
            float availableSize = statFs.getAvailableBlocks();//只包含应用可用的块的个数
            float percent = availableSize / blockCount;
            if (percent > 0.01)
                return (int) (percent * 100);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static boolean isFileExists(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    private static FileWriter mLogWriter;

    private static final Object mObjectLock = new Object();

    public static void writeLog(String dir, String msg) {
        SimpleDateFormat formatter = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
        String time = formatter.format(System.currentTimeMillis());
        try {
            if (mLogWriter == null) {
                formatter = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);
                File log = new File(dir, formatter.format(System.currentTimeMillis()));
                if (!log.exists()) {
                    log.createNewFile();
                }
                mLogWriter = new FileWriter(log, true);
            }
            synchronized (mObjectLock) {
                mLogWriter.write(time + ": ");
                mLogWriter.write(msg + "\n");
                mLogWriter.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (mLogWriter != null) {
                try {
                    mLogWriter.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                mLogWriter = null;
            }
        }
    }

    public static void releaseLog() {
        synchronized (mObjectLock) {
            DEBUG = false;
            if (mLogWriter != null) {
                try {
                    mLogWriter.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                mLogWriter = null;
            }
        }
    }

    public static void writeLog(Context context, String msg) {
        if (false) {
            File file = context.getCacheDir();
            writeLog(file.getAbsolutePath(), msg);
        }
    }

    public static void writeLogReboot(Context context, String msg) {
        File file = context.getCacheDir();
        writeLog(file.getAbsolutePath(), msg);
    }

    /**
     * 写Log可能会导致休眠失败.
     * */
    public static void writeLog(Context context, String TAG, String msg) {
        if (DEBUG) {
            File file = context.getCacheDir();
            writeLog(file.getAbsolutePath(), TAG + msg);
        }
    }
}
