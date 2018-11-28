package com.tomy.lib.ui.view.preference

import android.content.Context
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceViewHolder
import android.util.AttributeSet
import com.tomy.lib.ui.R
import timber.log.Timber

/**@author Tomy
 * Created by Tomy on 2018/11/27.
 */
class MasterTwoTargetPreference: TwoTargetPreference, Preference.OnPreferenceClickListener {

    private var mSummaryEnabled = false

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes)

    init {
        onPreferenceClickListener = this
    }

    override fun getSecondTargetResId(): Int {
        return R.layout.preference_widget_go
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        Timber.e("onBindViewHolder.key = $key")
        /*if (!mSummaryEnabled) {
            holder.findViewById(android.R.id.title).apply {
                (layoutParams as ConstraintLayout.LayoutParams).bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            }
        }*/


        mListener?.onBindViewHolder(holder)
    }

    override fun onPreferenceClick(preference: Preference?): Boolean {
        Timber.e("onPreferenceClick(). key = $key")
        return true
    }

}