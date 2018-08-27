package com.zzx.utils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

import java.io.File;

/**@author Tomy
 * Created by Tomy on 2017/3/20.
 */

public class MediaScanUtils implements MediaScannerConnection.MediaScannerConnectionClient {

    private String mPath;
    private MediaScannerConnection mConnection;

    public MediaScanUtils(Context context, String mPath) {
        this.mPath = mPath;
        mConnection = new MediaScannerConnection(context, this);
        mConnection.connect();
    }

    public MediaScanUtils(Context context, File file) {
        this(context, file.getAbsolutePath());
    }

    @Override
    public void onMediaScannerConnected() {
        mConnection.scanFile(mPath, null);
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        mConnection.disconnect();
    }
}
