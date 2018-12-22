package com.tomy.lib.ui.fragment

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.tomy.lib.ui.R
import com.tomy.lib.ui.bean.ApkInfo
import com.tomy.lib.ui.view.dialog.ConfirmDialog
import com.tomy.lib.ui.view.layout.PagePointLayout
import com.zzx.utils.context.ContextUtil
import com.zzx.utils.rxjava.FlowableUtil
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function
import timber.log.Timber
import java.util.*

/**@author Tomy
 * Created by Tomy on 2015-01-10.
 */
abstract class BaseApkFragment : Fragment(), AdapterView.OnItemClickListener, ViewPager.OnPageChangeListener, AdapterView.OnItemLongClickListener, View.OnClickListener {
    private var mAdapter: ApkViewPagerAdapter? = null
    private var mManager: PackageManager? = null
    private var mList: MutableList<ApkInfo>? = null
    private var mPoint: PagePointLayout? = null
    private var mPosition = 0
    protected var mContext: Activity? = null
    private var mDialog: ConfirmDialog? = null
    private var mIndex = 0
    private var mReceiver: UninstallReceiver? = null
    private var mViewPager: ViewPager? = null
    private var mCurrent = 0

    override fun onAttach(activity: Activity) {
        mContext = activity
        mList = ArrayList()
        initDialog()
        mManager = activity.packageManager
        mAdapter = ApkViewPagerAdapter(activity)
        mAdapter!!.setOnItemClickListener(this)
        //        mAdapter.setOnItemLongClickListener(this);
        initUninstallReceiver()
        super.onAttach(activity)
    }

    private fun initUninstallReceiver() {
        val filter = IntentFilter(Intent.ACTION_PACKAGE_REMOVED)
        filter.addDataScheme("package")
        mReceiver = UninstallReceiver()
        mContext!!.registerReceiver(mReceiver, filter)
    }

    override fun onDetach() {
        mContext!!.unregisterReceiver(mReceiver)
        mContext = null
        super.onDetach()
    }

    private fun initDialog() {
        mDialog = ConfirmDialog(mContext)
        mDialog!!.setMessage(R.string.uninstall_apk_sure)
        mDialog!!.setPositiveListener(this)
        mDialog!!.setNegativeListener(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_apk_container, container, false)
        mPoint = view.findViewById(R.id.page_point)
        mViewPager = view.findViewById(R.id.apk_view_pager)
        mViewPager?.apply {
            setOnPageChangeListener(this@BaseApkFragment)
            adapter = mAdapter
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FlowableUtil.setBackgroundThreadMapMain<Unit>(
                Function {
                    searchApk()
                }, Consumer {
                    Timber.e("========== mAdapter?.notifyDataSetChanged() ==========")
                    mAdapter?.notifyDataSetChanged()
                }
        )
    }

    override fun onDestroyView() {
        mPoint = null
        mViewPager = null
        super.onDestroyView()
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val index = mPosition * 8 + position
        val info = mList!![index]
        ContextUtil.startOtherActivity(mContext!!, info.mPackageName, info.mActivityName)
    }

    override fun onItemLongClick(parent: AdapterView<*>, view: View, position: Int, id: Long): Boolean {
        mCurrent = position
        mIndex = mPosition * 12 + position
        if (!mDialog!!.isShowing)
            mDialog!!.show()
        return true
    }


    private fun searchApk() {
        Timber.e("========== searchApk start ==========")
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val list = mManager!!.queryIntentActivities(intent, PackageManager.GET_ACTIVITIES)
        for (info in list) {
            val activityInfo = info.activityInfo
            /* if (Settings.System.getInt(mContext.getContentResolver(), Values.PLUS_VERSION, Values.PLUS_VERSION_NORMAL) == Values.PLUS_VERSION_GREEN) {
                if (activityInfo.packageName.equals(Values.PACKAGE_NAME_MUSIC_KW)) {
                    continue;
                }
            }*/
            val packageName = activityInfo.packageName
            if (!checkPackageNeedShow(packageName) && (checkPackageNeedHide(packageName) || activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0)) {
                continue
            }
            Timber.e("packageName = ${activityInfo.packageName}; activityName = ${activityInfo.name}")
            val apkInfo = ApkInfo()
            apkInfo.mPackageName = activityInfo.packageName
            apkInfo.mActivityName = activityInfo.name
            apkInfo.mApkName = info.loadLabel(mManager).toString()
            mAdapter!!.addIcon(info.loadIcon(mManager))
            mAdapter!!.addName(apkInfo.mApkName)
            mList!!.add(apkInfo)
        }
        mAdapter!!.addFinish()
        Timber.e("========== searchApk finish ==========")
    }

    /**
     * @param packageName String
     * @return Boolean true表示该App不显示.
     */
    abstract fun checkPackageNeedHide(packageName: String): Boolean

    abstract fun checkPackageNeedShow(packageName: String): Boolean

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {
        mPoint!!.setPageIndex(position)
        mPosition = position
    }

    override fun onPageScrollStateChanged(state: Int) {

    }

    override fun onClick(v: View) {
        /*switch (v.getId()) {
            case R.id.btn_ok:
                uninstallPackage();
                break;
            case R.id.btn_cancel:
                break;
        }
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }*/
    }

    internal fun uninstallPackage() {
        val info = mList!![mIndex]
        val packageName = info.mPackageName
        try {
            val packageClass = mManager!!.javaClass
            val methods = packageClass.methods
            for (method in methods) {
                if (method.name == "deletePackage") {
                    method.isAccessible = true
                    method.invoke(mManager, packageName, null, 0x00000002)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private inner class UninstallReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                val pkgName = intent.dataString
                if (pkgName!!.contains(mList!![mIndex].mPackageName)) {
                    mList!!.removeAt(mIndex)
                    mAdapter!!.removeItem(mPosition, mCurrent)
                    mAdapter!!.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    companion object {
        val NAV_APK_SELECTED = "navApk"
    }
}
