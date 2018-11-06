package com.tomy.lib.ui.fragment

import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import butterknife.ButterKnife
import butterknife.Unbinder
import timber.log.Timber

/**@author Tomy
 * Created by Tomy on 2018/5/30.
 */
abstract class BaseFragment: Fragment() {

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
        Timber.e("onCreateView")
        return mRootView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isBindView()) {
            mUnBinder = ButterKnife.bind(this, view)
        }
        initView(view)
    }

    override fun onResume() {
        super.onResume()
        resumeView()
    }

    override fun onPause() {
        super.onPause()
        pauseView()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        destroyView()
        mUnBinder?.unbind()
        mUnBinder = null
        (mRootView!!.parent as ViewGroup).removeView(mRootView!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        mRootView = null
    }
    /**
     * 在onResume()中调用.
     * */
    open fun resumeView() {
        Timber.e("resumeView()")
    }

    /**
     * 在[onViewCreated]中调用
     * @param root View
     */
    open fun initView(root: View) {
        Timber.e("initView()")
    }

    /**
     * 在[onDestroyView]中调用.
     */
    open fun destroyView() {
        Timber.e("destroyView()")
    }

    /**
     * 在[onAttach]中调用
     * 创建时初始化成员变量
     * */
    open fun initMember() {
        Timber.e("initMember()")
    }

    /**
     * [onPause]中调用
     * 布局销毁
     * */
    open fun pauseView() {
        Timber.e("pauseView()")
    }

    /**
     * [onDetach]中调用
     * 从Activity上分离时释放成员变量
     * */
    open fun releaseMember() {
        Timber.e("releaseMember()")
    }

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