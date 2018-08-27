package com.zzx.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.Toast;


import java.io.File;
import java.io.IOException;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;


/**
 *
 */
public class FTPContinue {

    private static final String TAG = "FTPContinue: ";
    public static final String BUS_EVENT_FTP = "BusFtp";
    public static final int FTP_LOGIN_FAILED = -1;
    public static final int FTP_LOGIN_SUCCESS = 0;
    public static final String BUS_EVENT_HTTP_UPLOAD = "BusUpload";
    public static final int HTTP_UPLOAD_SUCCESS = 1;
    public static final int HTTP_UPLOAD_FAILED = -2;
    public FTPClient mFtpClient = null;
    public static final String FTP_DEFAULT_IP   = "218.17.161.5";
    public static final String FTP_IP_OTHER = "218.64.169.250";
    public static final int FTP_DEFAULT_PORT    = 8002;
    public static final String FTP_DEFAULT_USER = "zzxcomm";
    public static final String FTP_DEFAULT_PASS = "zzx123456";
    public static final String FTP_USER_OTHER = "vsftpd";
    public static final String FTP_PASS_OTHER = "vsftpd";
    private String ftpHost = "";
    private int ftpPort = 21;
    private String ftpUserName = "";
    private String ftpPassword = "";
    private String fileName = "";

    private int finishCount = 0;
    private Context mContext;
    private String mRemoteDir = "";
    private FTPFile[] ftpFiles = null;

    public FTPContinue(Context context) {
        this(context, null, FTP_DEFAULT_IP, FTP_DEFAULT_PORT, FTP_DEFAULT_USER, FTP_DEFAULT_PASS);
    }

    public FTPContinue(Context context, Handler handler, String hostIp, int hostPort, String userName, String password) {
        mContext    = context;
        mFtpClient = new FTPClient();
        mFtpClient.setAutoNoopTimeout(5 * 1000);
        //MTK
        mFtpClient.setPassive(true);
        //小米
//        mFtpClient.setPassive(false);
        ftpHost = hostIp;
        ftpPort = hostPort;
        ftpUserName = userName;
        ftpPassword = password;
        new Thread() {
            @Override
            public void run() {
                super.run();
                connect();
            }
        }.start();
	}

	private boolean connect() {

		try {
			mFtpClient.connect(ftpHost, ftpPort);
			mFtpClient.login(ftpUserName, ftpPassword);
		} catch (Exception e) {
			e.printStackTrace();
            try {
                mFtpClient.setPassive(true);
                mFtpClient.connect(ftpHost, ftpPort);
                mFtpClient.login(ftpUserName, ftpPassword);
            } catch (Exception e1) {
                e1.printStackTrace();
                EventBusUtils.Companion.postEvent(BUS_EVENT_FTP, FTP_LOGIN_FAILED);
                return false;
            }
		}
        EventBusUtils.Companion.postEvent(BUS_EVENT_FTP, FTP_LOGIN_SUCCESS);
		return true;
	}

	public void download(String remoteDir, String remoteFileName, String localDirPath, ProgressBar progressBar) {
//        Values.LOGV(TAG, "remoteDir = " + remoteDir + "; remoteFileName = " + remoteFileName + "; localDirPath = " + localDirPath);
        fileName = remoteFileName;
        finishCount = 0;
		try {
            if (!mRemoteDir.equals(remoteDir)) {
                mFtpClient.changeDirectory(remoteDir);
                ftpFiles = mFtpClient.list();
                mRemoteDir = remoteDir;
            }
			for (FTPFile ftpFile : ftpFiles) {
                if (ftpFile.getName().contains(remoteFileName)) {
//                    Values.LOGW(TAG, "fileName = " + ftpFile.getName() + "; size = " + ftpFile.getSize());
                    new FtpDownLoadRunnable(remoteDir + ftpFile.getName(), localDirPath + ftpFile.getName(), ftpFile, progressBar)
                        .download();
                }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
				
	}

    public void upload(String localFilePath, String remoteDir) {
        new CmdUpload(localFilePath, remoteDir).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
	
	public void disconnect() {
        new Thread() {
            @Override
            public void run() {
                try {
                    if (mFtpClient != null && mFtpClient.isConnected()) {
                        mFtpClient.abortCurrentDataTransfer(true);
                        mFtpClient.logout();
                        mFtpClient.disconnect(true);
                        mFtpClient = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    if (mFtpClient != null) {
                        mFtpClient.disconnect(true);
                        mFtpClient = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                super.run();
            }
        }.start();
	}

    public void release() {
        disconnect();
    }

	public void abortDateDisconnect() {
		try {
			mFtpClient.abortCurrentDataTransfer(true);
//			if (mFtpClient != null && mFtpClient.isConnected()) {
//				 mFtpClient.abortCurrentDataTransfer(true);
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public boolean isConnected() {
        return mFtpClient != null && mFtpClient.isConnected();
    }

    class FtpDownLoadRunnable {
		private String remote = null;
		private String local = null;
		private FTPFile ftpFile = null;
        private ProgressBar progressBar = null;

		public FtpDownLoadRunnable(String remote, String localFile, FTPFile ftpFile, ProgressBar progressBar) {
			this.remote = remote;
			this.local = localFile;
			this.ftpFile = ftpFile;
		}

		public void download() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
			new CmdDownLoad(remote, local, ftpFile, progressBar).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

    private class UploadFTPDataTransferListener implements FTPDataTransferListener {
        private long tmpTotalTransferred = 0;
        private long totalTransferred = 0;
        private long fileSize = -1;
        private String fileName = null;

        private UploadFTPDataTransferListener(String fileName, long fileSize) {
            if (fileSize <= 0) {
                throw new RuntimeException(
                        "the size of file must be larger than zero.");
            }
            this.fileName = fileName;
            this.fileSize = fileSize;
        }

        @Override
        public void started() {

        }

        @Override
        public void transferred(int length) {
        }

        @Override
        public void completed() {
        }

        @Override
        public void aborted() {

        }

        @Override
        public void failed() {
        }
    }

	private class DownLoadFTPDataTransferListener implements FTPDataTransferListener {

		private int totalTransferred = 0;
		private long mFileSize = -1;
        private ProgressBar mProgressBar = null;
		
		public DownLoadFTPDataTransferListener(long fileSize, ProgressBar progressBar) {
			if (fileSize <= 0) {
				throw new RuntimeException("the size of file must be larger than zero.");
			}
            this.mProgressBar = progressBar;
			this.mFileSize = fileSize;
		}
		
		@Override
		public void aborted() {
			
		}
		
		@Override
		public void completed() {
            if (mProgressBar != null)
                setLoadProgress(mProgressBar.getMax());
        }

		@Override
		public void failed() {
		
		}
		
		@Override
		public void started() {
			
		}
		
		@Override
		public void transferred(int length) {
			totalTransferred += length;
			float percent = (float) totalTransferred / mFileSize;
            if (mProgressBar != null)
			    setLoadProgress((int) (percent * mProgressBar.getMax()));
		}

        public void setLoadProgress(int progress) {
            if (mProgressBar != null) {
                mProgressBar.setProgress(progress);
            }
        }
	}

	class CmdDownLoad extends AsyncTask<Void, Integer, Boolean> {
		private String remote   = null;
		private String local    = null;
		private FTPFile ftpFile = null;
        private ProgressBar progressBar;
		public CmdDownLoad(String remote, String local, FTPFile ftpFile, ProgressBar progressBar) {
			this.remote = remote;
			this.local  = local;
			this.ftpFile = ftpFile;
            this.progressBar = progressBar;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				File f = new File(local);
                if(f.exists()){
                    Values.LOG_E(TAG, "remote = " + remote + "; local = " + local + "; localSize = " + f.length() + "; remoteSize = " + ftpFile.getSize());
                    long localSize = f.length();
                    if (localSize >= ftpFile.getSize()) {
                        return true;
                    }
                    mFtpClient.download(remote, new File(local), localSize, new DownLoadFTPDataTransferListener(ftpFile.getSize(), progressBar));
                } else {
                    Values.LOG_E(TAG, "remote = " + remote + "; local = " + local);
                    f.getParentFile().mkdirs();
                    f.createNewFile();
                    mFtpClient.download(remote, f, 0);
                }
			} catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}
			return true;
		}

		protected void onPostExecute(Boolean result) {
            Values.LOG_E(TAG, "remote = " + remote + ";download finish");
            Toast.makeText(mContext, remote + "download finish", Toast.LENGTH_SHORT).show();
		}
	}

    class CmdUpload extends AsyncTask<String, Integer, Boolean> {
        private String mLocalFile   = null;
        private String mRemoteDir   = null;
        public CmdUpload(String localFileName, String remoteDir) {
            mLocalFile  = localFileName;
            mRemoteDir  = remoteDir;
        }

        @Override
        protected Boolean doInBackground(String... params) {
//            Values.LOGI(TAG, "CmdUpload.doInBackground.ThreadId = " + Thread.currentThread().getId());
            try {
                mFtpClient.createDirectory(mRemoteDir);
            } catch (Exception e) {
                e.printStackTrace();
            }
            File localFile = new File(mLocalFile);
            try {
                mFtpClient.changeDirectory(mRemoteDir);
                mFtpClient.upload(localFile, new UploadFTPDataTransferListener(localFile.getAbsolutePath(), localFile.length()));
            } catch (Exception e) {
                e.printStackTrace();
                EventBusUtils.Companion.postEvent(BUS_EVENT_HTTP_UPLOAD, HTTP_UPLOAD_FAILED);
                return false;
            }
            EventBusUtils.Companion.postEvent(BUS_EVENT_HTTP_UPLOAD, mLocalFile);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
        }
    }

}
