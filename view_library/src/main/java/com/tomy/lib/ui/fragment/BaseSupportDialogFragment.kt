package com.tomy.lib.ui.fragment

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import butterknife.ButterKnife
import butterknife.Unbinder
import com.tomy.lib.ui.R

/**@author Tomy
 * Created by Tomy on 2018/8/6.
 */
abstract class BaseSupportDialogFragment: DialogFragment() {
    var mContext: Activity? = null
    var mUnBinder: Unbinder? = null

    private var mRootView: View? = null

    fun Context.showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onAttach(context: Activity?) {
        super.onAttach(context)
        mContext = context
        initMember()
    }

    override fun onDetach() {
        super.onDetach()
        releaseMember()
        mContext = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        if (mRootView == null) {
            mRootView = inflater.inflate(getLayoutId(), container, false)
        }
        return mRootView!!
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(mContext!!, R.style.CustomDialogTheme)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isBindView()) {
            mUnBinder = ButterKnife.bind(this, view)
        }
        initView(mRootView!!)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        viewDestroy()
        mUnBinder?.unbind()
        mUnBinder = null
        (mRootView!!.parent as ViewGroup).removeView(mRootView!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        mRootView = null
    }
    /**
     * 初始化布局.
     * */
    abstract fun initView(root: View)

    /**
     * 创建时初始化成员变量
     * */
    open fun initMember() {}

    /**
     * 布局销毁
     * */
    open fun viewDestroy() {}

    /**
     * 从Activity上分离时释放成员变量
     * */
    open fun releaseMember() {}

    /**
     * 获得LayoutId.
     * */
    abstract fun getLayoutId(): Int

    /**
     * 设置是否使用[ButterKnife]绑定View.
     * */
    open fun isBindView(): Boolean {
        return true
    }
}