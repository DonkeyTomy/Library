package com.tomy.lib.ui.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
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
        Timber.e("onCreateView")
        createView(mRootView!!)
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