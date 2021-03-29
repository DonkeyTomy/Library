package com.tomy.lib.ui.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import butterknife.ButterKnife
import butterknife.Unbinder
import timber.log.Timber

/**@author Tomy
 * Created by Tomy on 2018/7/1.
 */
abstract class BaseSupportFragment: Fragment() {

    var mContext: FragmentActivity? = null
    var mUnBinder: Unbinder? = null

    private var mRootView: View? = null

    fun Context.showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onAttach(context: Activity?) {
        super.onAttach(context)
        mContext = context as FragmentActivity
        initMember()
        Timber.v("onAttach ${this.javaClass.simpleName}")
    }

    override fun onDetach() {
        super.onDetach()
        Timber.v("onDetach ${this.javaClass.simpleName}")
        releaseMember()
        mContext = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        if (mRootView == null) {
            mRootView = inflater.inflate(getLayoutId(), container, false)
        }
        Timber.v("onCreateView ${this.javaClass.simpleName}")
        createView(mRootView!!)
        return mRootView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isBindView()) {
            mUnBinder = ButterKnife.bind(this, view)
        }
        Timber.v("onViewCreated ${this.javaClass.simpleName}")
        initView(view)
    }

    override fun onResume() {
        super.onResume()
        resumeView()
        Timber.v("onResume ${this.javaClass.simpleName}")
    }

    override fun onPause() {
        super.onPause()
        pauseView()
        Timber.v("onPause ${this.javaClass.simpleName}")
    }

    override fun onStop() {
        super.onStop()
        Timber.v("onStop ${this.javaClass.simpleName}")
    }

    override fun onDestroyView() {
        destroyView()
        mUnBinder?.unbind()
        mUnBinder = null
        (mRootView!!.parent as ViewGroup).removeView(mRootView!!)
        Timber.v("onDestroyView ${this.javaClass.simpleName}")
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.v("onDestroy ${this.javaClass.simpleName}")
        mRootView = null
    }
    /**
     * 初始化布局.
     * */
    open fun resumeView() {}

    /**
     * @param root 调用在bindView()之后
     */
    open fun initView(root: View) {}

    /**
     * @param root 调用在bindView()之前可追加View
     */
    open fun createView(root: View) {}

    open fun destroyView() {}

    /**
     * 创建时初始化成员变量
     * */
    open fun initMember() {}

    /**
     * 布局销毁
     * */
    open fun pauseView() {}

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