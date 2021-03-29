package com.zzx.utils;

import android.util.Log;



/**
 * Created by donke on 2017/3/20.
 */

public class Values {

    private static final boolean DEBUG = true;
    private static final String TAG = "zzxUtils";

    public static void LOG_I(String tag, String msg) {
        if (DEBUG) {
            Log.i(TAG, tag + ": " + msg);
        }
    }
    public static void LOG_W(String tag, String msg) {
        if (DEBUG) {
           Log.w(TAG, tag + ": " + msg);
        }
    }
    public static void LOG_E(String tag, String msg) {
        if (DEBUG) {
            Log.e(TAG, tag + ": " + msg);
        }
    }
}
