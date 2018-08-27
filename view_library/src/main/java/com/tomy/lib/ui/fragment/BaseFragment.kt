package com.tomy.lib.ui.fragment

import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.tomy.lib.ui.R

/**@author Tomy
 * Created by Tomy on 2018/5/30.
 */
abstract class BaseFragment: Fragment() {

    var mContext: Context? = null

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

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater!!.inflate(getLayoutId(), container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewDestroy()
    }


    /**
     * 初始化布局.
     * */
    abstract fun initView(root: View)

    /**
     * 创建时初始化成员变量
     * */
    abstract fun initMember()

    /**
     * 布局销毁
     * */
    abstract fun viewDestroy()

    /**
     * 从Activity上分离时释放成员变量
     * */
    abstract fun releaseMember()

    /**
     * 获得LayoutId.
     * */
    abstract fun getLayoutId(): Int

}