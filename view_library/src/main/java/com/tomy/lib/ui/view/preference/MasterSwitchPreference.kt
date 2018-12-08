package com.tomy.lib.ui.view.preference

import android.content.Context
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceViewHolder
import android.util.AttributeSet
import android.widget.Switch
import com.tomy.lib.ui.R
import com.zzx.utils.rxjava.FlowableUtil
import io.reactivex.functions.Consumer
import timber.log.Timber

/**@author Tomy
 * Created by Tomy on 2018/11/20.
 */
open class MasterSwitchPreference: TwoTargetPreference {

    private var mSwitch: Switch? = null

    private var mChecked = false

    private var mSummaryEnabled = false

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes)

    override fun getSecondTargetResId(): Int {
        return R.layout.preference_widget_switch
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        Timber.e("onBindViewHolder.key = $key")
        /*if (!mSummaryEnabled) {
            holder.findViewById(android.R.id.title).apply {
                (layoutParams as ConstraintLayout.LayoutParams).bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            }
        }*/

        val widgetView = holder.findViewById(android.R.id.widget_frame)
        widgetView.setOnClickListener {
            Timber.e("widgetView.Click().isEnabled = ${mSwitch?.isEnabled}")
            if (mSwitch?.isEnabled != true) {
                return@setOnClickListener
            }
            mSwitch?.isEnabled = false
            setChecked(!mChecked)
            /*if (!callChangeListener(mChecked)) {
                setChecked(!mChecked)
            } else {
                persistBoolean(mChecked)
            }*/
        }
        mSwitch = holder.findViewById(R.id.switchWidget) as Switch?
        mSwitch?.apply {
            contentDescription = title
            isChecked = mChecked
//            isEnabled = mSwitchEnabled
        }
        mListener?.onBindViewHolder(holder)
    }

    fun setChecked(checked: Boolean) {
        Timber.e("setChecked.isChecked = $checked")
        mChecked = checked
        mSwitch?.apply {
            if (isChecked != checked) {
                isChecked = checked
            }
        }
    }

    fun setSwitchEnabled(enabled: Boolean) {
        Timber.e("enabled = $enabled")
        FlowableUtil.setMainThread(Consumer {
            mSwitch?.isEnabled = enabled
        })
    }

    fun isChecked(): Boolean {
        return mSwitch?.isEnabled == true && mSwitch?.isChecked == true
    }

    fun getSwitch(): Switch? {
        return mSwitch
    }

}