package com.tomy.lib.ui.view.preference

import android.app.Dialog
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tomy.lib.ui.R

/**@author Tomy
 * Created by Tomy on 2018/12/2.
 */
abstract class BaseDialogPreference: MasterTwoTargetPreference {

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes)

    private var mContentView: View? = null

    private val mDialog by lazy {
        Dialog(context, R.style.CustomDialogTheme).apply {
            setContentView(mContentView)
        }
    }

    override fun onClick() {
        showDialog()
    }

    fun getDialog(): Dialog {
        return mDialog
    }

    private fun showDialog() {
        if (mContentView == null) {
            mContentView = onCreateDialogView()
            onDialogViewCreated(mContentView!!)
        }
        onBindDialogView(mContentView!!)
        mDialog.show()
    }

    open fun onCreateDialogView(): View {
        return LayoutInflater.from(context).inflate(getDialogLayoutId(), null, false).apply {
            if (getSecondWidgetLayoutId() > 0) {
                findViewById<ViewGroup>(android.R.id.widget_frame)?.apply {
                    addView(LayoutInflater.from(context).inflate(getSecondWidgetLayoutId(), this, false))
                }
            }
        }
    }

    abstract fun onDialogViewCreated(view: View)


    fun dismissDialog() {
        mDialog.dismiss()
    }


    abstract fun onBindDialogView(view: View)

    abstract fun getDialogLayoutId(): Int

    open fun getSecondWidgetLayoutId() = 0

}