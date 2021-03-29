package com.zzx.utils.system

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import timber.log.Timber


/**@author Tomy
 * Created by Tomy on 2020/3/25.
 */
object PackageManagerWrapper {

    /**
     * @param filter IntentFilter
     * @param match Int [IntentFilter.MATCH_CATEGORY_EMPTY].etc
     * @param set Array<ComponentName> 要加入的默认包名类名合集
     * @param activity ComponentName 请求此设置的类名包名
     */
    @SuppressLint("BinaryOperationInTimber")
//    or(IntentFilter.MATCH_ADJUSTMENT_NORMAL)
    fun setPreferredActivity(context: Context, filter: IntentFilter, match: Int,
                             set: Array<ComponentName?>, activity:ComponentName) {
        /*Timber.tag("tomy").w("setPreferredActivity.set.size = ${set.size}")
        for (element in set) {
            Timber.tag("tomy").i("component = $element")
        }
        Timber.tag("tomy").i("filter = \n" +
                "{action[" + filter.getAction(0) + "}\n" +
                "openable = " + filter.hasCategory(Intent.CATEGORY_OPENABLE) + "; default = " + filter.hasCategory(Intent.CATEGORY_DEFAULT))

        val iterator = filter.typesIterator()
        while (iterator.hasNext()) {
            Timber.tag("tomy").i("type = %s", iterator.next())
        }
        Timber.tag("tomy").i("activity.component = $activity; bestMatch = $match")*/
        context.packageManager.addPreferredActivity(filter, match, set, activity)
    }

    fun setPreferredActivity(context: Context, action: String, categoryArray: Array<String>, type: String?, match: Int, isDefault: Boolean, activity: ComponentName) {
        val intent = Intent().apply {
            this.action  = action
            categoryArray.iterator().forEach {
                addCategory(it)
            }
            type?.let {
                setType(it)
            }
        }
        val resolverList = getResolverActivityList(context, intent)
        Timber.tag("tomy").e("list = ${resolverList.size}")
        val componentArray = arrayOfNulls<ComponentName>(resolverList.size)
        if (resolverList.isNotEmpty()) {
            for (i in 0 until resolverList.size) {
                componentArray[i] = ComponentName(resolverList[i].activityInfo.packageName, resolverList[i].activityInfo.name)
                Timber.tag("tomy").e("list = ${componentArray[i]}")
            }
        }
        IntentFilter().apply {
            addAction(action)
            categoryArray.iterator().forEach {
                addCategory(it)
            }
            if (isDefault) {
                addCategory(Intent.CATEGORY_DEFAULT)
            }
            type?.let {
                addDataType(it)
            }
            setPreferredActivity(context, this, match, componentArray, activity)
        }
    }

    fun clearPreferredActivity(context: Context, packageName: String) {
        context.packageManager.clearPackagePreferredActivities(packageName)
    }

    fun getResolverActivityList(context: Context, intent: Intent, shouldGetActivityMetaData: Boolean = false): MutableList<ResolveInfo> {
        Timber.tag("tomy").i("intent = $intent")
        return context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
//        return context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY.or(0x00800000).or(PackageManager.GET_RESOLVED_FILTER))
//        context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY.or(if (shouldGetActivityMetaData) PackageManager.GET_META_DATA else 0))
    }

}