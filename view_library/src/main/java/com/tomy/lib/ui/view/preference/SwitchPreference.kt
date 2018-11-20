package com.tomy.lib.ui.view.preference

import android.content.Context
import android.support.v7.preference.PreferenceViewHolder
import android.util.AttributeSet
import android.widget.Switch
import com.tomy.lib.ui.R

/**@author Tomy
 * Created by Tomy on 2018/11/20.
 */
class SwitchPreference: TwoTargetPreference {

    private var mSwitch: Switch? = null

    private var mChecked = false

    private var mSwitchEnabled = true

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes)

    override fun getSecondTargetResId(): Int {
        return R.layout.preference_widget_switch
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val widgetView = holder.findViewById(android.R.id.widget_frame)
        widgetView?.setOnClickListener {
            if (mSwitch?.isEnabled != true) {
                return@setOnClickListener
            }
            setChecked(!mChecked)
            if (!callChangeListener(mChecked)) {
                setChecked(!mChecked)
            } else {
                persistBoolean(mChecked)
            }
        }
        mSwitch = holder.findViewById(R.id.switchWidget) as Switch?
        mSwitch?.apply {
            contentDescription = title
            isChecked = mChecked
            isEnabled = mSwitchEnabled
        }
    }

    fun setChecked(checked: Boolean) {
        mChecked = checked
        mSwitch?.isChecked = mChecked
    }

    fun isChecked(): Boolean {
        return mSwitch?.isEnabled == true && mSwitch?.isChecked == true
    }

    fun getSwitch(): Switch? {
        return mSwitch
    }
}