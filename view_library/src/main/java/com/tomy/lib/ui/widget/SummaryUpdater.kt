package com.tomy.lib.ui.widget

import android.content.Context
import android.text.TextUtils
import timber.log.Timber

/**@author Tomy
 * Created by Tomy on 2018/11/21.
 *
 * 概要更新类.
 * 1. [register]方法开始/停止接受Summary更新消息.如通过[Context.registerReceiver]/[Context.unregisterReceiver]方式
 * 2. [getSummary]方法实现需要显示的概要信息.
 * 3. [OnSummaryChangeListener]在概要需要刷新时回调.
 */
abstract class SummaryUpdater(protected val mContext: Context, var mListener: OnSummaryChangeListener? = null) {

    private var mSummary: String? = null

    interface OnSummaryChangeListener {
        /**
         * Called when summary changed
         * @param summary the new Summary
         */
        fun onSummaryChanged(summary: String?)
    }

    open fun notifyChangedIfNeeded() {
        val summary = getSummary()
        Timber.e("notifyChangedIfNeeded().summary = $summary")
        if (!TextUtils.equals(mSummary, summary)) {
            mSummary = summary
            mListener?.onSummaryChanged(summary)
        }
    }

    /**
     * Starts/stops receiving updates on the summary.
     * @param register true if we want to receive updates, false otherwise.
     */
    abstract fun register(register: Boolean)

    /**
     * Gets the summary. Subclass should checks latest conditions and update the summary accordingly.
     * @return the latest summary text.
     */
    abstract fun getSummary(): String?

}