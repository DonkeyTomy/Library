package com.tomy.lib.ui.bean;

/**@author Tomy
 * Created by Tomy on 2015-05-08.
 */
public class ApkInfo {
    public ApkInfo() {
        mPackageName    = "";
        mActivityName   = "";
        mApkName        = "";
    }
    public void clear() {
        mPackageName    = "";
        mActivityName   = "";
        mApkName        = "";
    }
    public String mApkName;
    public String mPackageName;
    public String mActivityName;
    public boolean mSystemFlag = false;
}
