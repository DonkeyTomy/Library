package com.tomy.lib.ui.fragment

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
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
    var mContext: AppCompatActivity? = null
    var mUnBinder: Unbinder? = null

    private var mRootView: View? = null

    fun Context.showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onAttach(context: Activity?) {
        super.onAttach(context)
        mContext = context as AppCompatActivity
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
        createView(mRootView!!)
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
        viewDestroy()
        mUnBinder?.unbind()
        mUnBinder = null
        (mRootView!!.parent as ViewGroup).removeView(mRootView!!)
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        mRootView = null
    }
    /**
     * ???bindView??????.
     * ???????????????.
     * */
    abstract fun initView(root: View)

    /**
     *
     * ???[isBindView]bindView??????,???????????????View.
     * @param root View
     */
    open fun createView(root: View) {}

    /**
     * ??????????????????????????????
     * */
    open fun initMember() {}

    /**
     * ????????????
     * */
    open fun viewDestroy() {}

    /**
     * ???Activity??????????????????????????????
     * */
    open fun releaseMember() {}

    /**
     * ??????LayoutId.
     * */
    abstract fun getLayoutId(): Int

    /**
     * ??????????????????[ButterKnife]??????View.
     * */
    open fun isBindView(): Boolean {
        return true
    }
}