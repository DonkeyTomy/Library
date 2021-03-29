package com.tomy.lib.ui.view.layout

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.KeyEvent

/**@author Tomy
 * Created by Tomy on 2018/6/16.
 */
class MainLinearLayout(context: Context?, attributeSet: AttributeSet?, defStyle: Int) : ConstraintLayout(context, attributeSet, defStyle) {

    constructor(context: Context?): this(context, null, 0)
    constructor(context: Context?, attributeSet: AttributeSet?): this(context, attributeSet, 0)

    private var mPressedListener: OnKeyPressedListener? = null

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        return if (mPressedListener?.onKeyPressed(event) == true) true else super.dispatchKeyEvent(event)
    }

    fun setOnKeyPressedListener(pressedListener: OnKeyPressedListener) {
        mPressedListener = pressedListener
    }

    interface OnKeyPressedListener {
        fun onKeyPressed(event: KeyEvent?): Boolean
    }

}