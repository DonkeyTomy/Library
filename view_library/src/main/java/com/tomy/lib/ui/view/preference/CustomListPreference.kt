package com.tomy.lib.ui.view.preference

import android.content.Context
import android.os.Bundle
import android.preference.ListPreference
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View

/**@author Tomy
 * Created by Tomy on 2018/10/9.
 */
abstract class CustomListPreference(context: Context, attrSet: AttributeSet): ListPreference(context, attrSet) {

    private val mInflater by lazy {
        LayoutInflater.from(context)
    }

    override fun showDialog(state: Bundle?) {
        super.showDialog(state)
    }

    override fun onBindDialogView(view: View?) {
        super.onBindDialogView(view)
    }

    /**
     * @return Int 返回窗口的布局Id.
     */
    abstract fun getDialogLayoutId(): Int

    override fun onCreateDialogView(): View {
        val view = mInflater.inflate(getDialogLayoutId(), null)
        return view
    }

}