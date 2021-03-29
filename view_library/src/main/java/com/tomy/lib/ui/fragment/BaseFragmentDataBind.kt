package com.tomy.eva.fragment

import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

/**@author Tomy
 * Created by Tomy on 2017/10/11.
 */
abstract class BaseFragmentDataBind<in T : ViewDataBinding> : Fragment() {
    var mContext: Context? = null

    fun Context.showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onAttach(context: Activity?) {
        super.onAttach(context)
        mContext = context
    }

    override fun onDetach() {
        super.onDetach()
        mContext = null
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DataBindingUtil.inflate<T>(inflater!!, getLayoutId(), container, false)
        initData(binding)
        return binding.root
    }

    abstract fun getLayoutId(): Int
    abstract fun initData(dataBinding: T)
}