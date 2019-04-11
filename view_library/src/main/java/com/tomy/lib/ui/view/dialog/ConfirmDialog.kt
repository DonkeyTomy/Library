package com.tomy.lib.ui.view.dialog

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.tomy.lib.ui.R

/**@author Tomy
 * Created by Tomy on 2015-03-11.
 */
class ConfirmDialog @JvmOverloads constructor(context: Context, theme: Int = R.style.dialog) : Dialog(context, theme) {
    init {
        setContentView(R.layout.confirm_dialog)
        setCanceledOnTouchOutside(true)
        val attributes = window.attributes.apply {
            width   = WindowManager.LayoutParams.MATCH_PARENT
            height  = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.CENTER
        }
        window.attributes = attributes
    }

    fun setPositiveListener(listener: View.OnClickListener) {
        findViewById<View>(R.id.btn_ok).setOnClickListener(listener)
    }

    fun setNegativeListener(listener: View.OnClickListener) {
        findViewById<View>(R.id.btn_cancel).setOnClickListener(listener)
    }

    fun setMessage(msgId: Int) {
        (findViewById<View>(R.id.message) as TextView).setText(msgId)
    }
}
