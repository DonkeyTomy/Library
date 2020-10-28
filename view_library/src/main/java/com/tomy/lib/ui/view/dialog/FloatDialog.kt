package com.tomy.lib.ui.view.dialog

import android.app.DialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.Unbinder

/**@author Tomy
 * Created by Tomy on 2018/6/16.
 */
abstract class FloatDialog: DialogFragment() {

    private var mUnBinder: Unbinder? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    abstract fun initView()

    open fun isBindView(): Boolean {
        return true
    }

    abstract fun getLayoutId(): Int

}