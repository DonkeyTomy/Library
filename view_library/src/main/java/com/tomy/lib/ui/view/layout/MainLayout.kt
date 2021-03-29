package com.tomy.lib.ui.view.layout

import android.content.Context
import android.graphics.Rect
import android.support.annotation.Keep
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent

/**@author Tomy
 * Created by Tomy on 2018/6/9.
 */
class MainLayout(context: Context?, attributeSet: AttributeSet?, defStyle: Int) : ConstraintLayout(context, attributeSet, defStyle) {

    constructor(context: Context?): this(context, null, 0)
    constructor(context: Context?, attributeSet: AttributeSet?): this(context, attributeSet, 0)

    private var mPressedListener: OnKeyPressedListener? = null

    private var mOnTouchOutsideListener: OnTouchOutsideListener? = null

    private val mViewRect = lazy {
        val viewRect = Rect()
        val array = IntArray(2)
        getGlobalVisibleRect(viewRect)
        getLocationOnScreen(array)
        viewRect.apply {
            set(array[0], array[1], array[0] + right, array[1] + bottom)
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        return if (mPressedListener?.onKeyPressed(event) == true) true else super.dispatchKeyEvent(event)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev!!.action == MotionEvent.ACTION_DOWN) {
            if (mOnTouchOutsideListener != null) {
                if (!mViewRect.value.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    mOnTouchOutsideListener!!.onTouchOutside()
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    @Keep
    fun setOnKeyPressedListener(pressedListener: OnKeyPressedListener) {
        mPressedListener = pressedListener
    }

    @Keep
    fun setOnTouchOutsideListener(listener: OnTouchOutsideListener) {
        mOnTouchOutsideListener = listener
    }

    @Keep
    interface OnKeyPressedListener {
        fun onKeyPressed(event: KeyEvent?): Boolean
    }

    @Keep
    interface OnTouchOutsideListener {
        fun onTouchOutside()
    }

}