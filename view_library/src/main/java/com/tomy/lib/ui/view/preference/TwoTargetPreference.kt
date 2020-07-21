package com.tomy.lib.ui.view.preference

import android.content.Context
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceViewHolder
import android.util.AttributeSet
import android.view.View
import com.tomy.lib.ui.R

/**@author Tomy
 * Created by Tomy on 2018/11/20.
 */
open class TwoTargetPreference: Preference {

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes)

    protected var mListener: OnBindViewHolderListener? = null
    
    protected lateinit var mItemView: View

    init {
        layoutResource = getLayoutResourceId()
        val secondTargetId = getSecondTargetResId()
        if (secondTargetId != 0) {
            widgetLayoutResource = secondTargetId
        }
    }

    fun setOnBindViewHolderListener(listener: OnBindViewHolderListener) {
        mListener = listener
    }

    open fun getLayoutResourceId() = R.layout.preference_two_target

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        mItemView = holder.itemView
        val widgetFrame = holder.findViewById(android.R.id.widget_frame)
        if (widgetFrame != null) {
            widgetFrame.visibility = if (shouldHideSecondTarget()) View.GONE else View.VISIBLE
        }
    }

    fun shouldHideSecondTarget(): Boolean {
        return getSecondTargetResId() == 0
    }

    open fun getSecondTargetResId(): Int {
        return 0
    }

    interface OnBindViewHolderListener {
        fun onBindViewHolder(holder: PreferenceViewHolder)
    }


    open fun setWidgetEnabled(enabled: Boolean) {}
}