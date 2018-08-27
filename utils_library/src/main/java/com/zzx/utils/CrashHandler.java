package com.zzx.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 
 * 描述：捕捉全局异常
 * 陈伟斌
 * 2013-12-16
 */
public class CrashHandler implements UncaughtExceptionHandler {

	public static final String TAG = "CrashHandler";
    private static final String DEFAULT_MODEL = "/ftproot/T3";

    // CrashHandler实例
	private static CrashHandler INSTANCE = new CrashHandler();
	// 程序的Context对象
	private Context mContext;
	// 用来存储设备信息和异常信息
	private Map<String, String> infos = new HashMap<>();

	// 用于格式化日期作为日志文件名的
	private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
    private String LOG_DIR = "/sdcard/ppgou_log/";
    private File[] mErrorFiles;
    private int mFileCount = 0;
    private FTPContinue mFtpContinue;
    private int mEventCount = 0;
    private UncaughtExceptionHandler mDefaultHandler;

    /**
	 * 使用Properties来保存设备的信息和错误堆栈信息/
	 * private Properties mDeviceCrashInfo = new Properties();
	 * private static final String STACK_TRACE = "STACK_TRACE";
	 * 
	 * /** 保证只有一个CrashHandler实例
	 */
	private CrashHandler() {
	}

	/** 获取CrashHandler实例 ,单例模式 */
	public static CrashHandler getInstance() {
		return INSTANCE;
	}

	/**
	 * 初始化
	 * 
	 * @param context
	 */
	public void init(Context context) {
		mContext = context;
		// 获取系统默认的UncaughtException处理
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		// 设置该CrashHandler为程序的默认处理
		Thread.setDefaultUncaughtExceptionHandler(this);
        EventBusUtils.Companion.registerEvent(FTPContinue.BUS_EVENT_HTTP_UPLOAD, this);
	}

    public void release() {
        EventBusUtils.Companion.unregisterEvent(FTPContinue.BUS_EVENT_HTTP_UPLOAD, this);
    }

	/**
	 * 当UncaughtException发生时会转入该函数来处理
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
        try {
            handleException(ex);
            mDefaultHandler.uncaughtException(thread, ex);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	/**
	 * 自定义错误处收集错误信息 发错误报告等操作均在此完成.
	 * 
     * @see #saveCrashInfo2File(Throwable)
	 * @return true:如果处理了该异常信息;否则返回false.
	 */
	private boolean handleException(Throwable ex) throws Exception {
		if (ex == null) {
			return false;
		}
		saveCrashInfo2File(ex);
        sendErrorReportsToServer();
		ex.printStackTrace();
		return true;
	}

	/**
	 * 收集设备参数信息
	 * 
	 * @param ctx
	 */
	public void collectDeviceInfo(Context ctx) {
		try {
			PackageManager pm = ctx.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),
					PackageManager.GET_ACTIVITIES);
			if (pi != null) {
				String versionName = pi.versionName == null ? "null"
						: pi.versionName;
				String versionCode = pi.versionCode + "";
				infos.put("versionName", versionName);
				infos.put("versionCode", versionCode);
			}
		} catch (NameNotFoundException e) {
			Log.e(TAG, "an error occured when collect package info", e);
		}
		Field[] fields = Build.class.getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				infos.put(field.getName(), field.get(null).toString());
				Log.d(TAG, field.getName() + " : " + field.get(null));
			} catch (Exception e) {
				Log.e(TAG, "an error occured when collect crash info", e);
			}
		}
	}

	/**
	 * 在程序启动时调用 可以调用该函数来发送以前没有发送的报道
	 */
	public void sendPreviousReportsToServer() {
		sendErrorReportsToServer();
	}

	/**
	 * 把错误报告发送给服务器包含新产生的和以前没发送的报告
	 * 
	 */
	private void sendErrorReportsToServer() {
        if (mFtpContinue != null || !SystemUtil.isNetworkConnected(mContext)) {
            return;
        }
        try {
            mErrorFiles = getErrorReportFiles();
            if (mErrorFiles != null) {
                mFileCount = mErrorFiles.length;
                if (mFileCount > 0) {
                    File file = mErrorFiles[0];
                    mFtpContinue = new FTPContinue(mContext);
                    mFtpContinue.upload(file.getAbsolutePath(), DEFAULT_MODEL);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

    void onEvent(Integer event) {
        switch (event) {
            case FTPContinue.FTP_LOGIN_FAILED:
                releaseFtpContinue();
                break;
            case FTPContinue.HTTP_UPLOAD_FAILED:
                doFailed();
                break;
        }
    }

    private void releaseFtpContinue() {
        if (mFtpContinue != null) {
            mFtpContinue.release();
            mFtpContinue = null;
        }
    }

    private void doFailed() {
        try {
            if ((++mEventCount) >= mFileCount) {
                mEventCount = 0;
                mFileCount = 0;
                releaseFtpContinue();
                release();
            } else {
                File info = mErrorFiles[mEventCount];
                mFtpContinue.upload(info.getAbsolutePath(), DEFAULT_MODEL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void onEvent(String fileName) {
        try {
            File file = new File(fileName);
            if (file.exists()) {
                file.delete();
            }
            if ((++mEventCount) >= mFileCount) {
                mEventCount = 0;
                mFileCount = 0;
                if (mFtpContinue != null) {
                    mFtpContinue.release();
                    mFtpContinue = null;
                }
                release();
            } else {
                File info = mErrorFiles[mEventCount];
                mFtpContinue.upload(info.getAbsolutePath(), DEFAULT_MODEL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	/**
	 * 获取错误报告文件
	 * 
	 */
	private File[] getErrorReportFiles() {
		File filesDir = new File(LOG_DIR);
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".txt");
			}
		};
        if (filesDir.exists() && filesDir.isDirectory()) {
            return filesDir.listFiles(filter);
        } else {
            return null;
        }
	}

	/**
	 * 保存错误信息到文件中
	 * 
	 * @return 返回文件名称,便于将文件传送到服务器
	 */
	private String saveCrashInfo2File(Throwable ex) {

		/*StringBuffer sb = new StringBuffer();
		for (Map.Entry<String, String> entry : infos.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			sb.append(key + "=" + value + "\n");
		}*/

		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();
		String result = writer.toString();
//		sb.append(result);
		try {
			String time = formatter.format(new Date());
			String fileName = time + ".txt";
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				File dir = new File(LOG_DIR);
				if (!dir.exists() || !dir.isDirectory()) {
					dir.mkdirs();
				}
				FileOutputStream fos = new FileOutputStream(LOG_DIR + fileName);
				fos.write(result.getBytes());
				fos.close();
			}
			return fileName;
		} catch (Exception e) {
			Log.e(TAG, "an error occured while writing file...", e);
		}
		return null;
	}
}
