package com.tomy.lib.ui.fragment

import android.support.constraint.ConstraintLayout.LayoutParams
import android.view.LayoutInflater
import android.view.View
import com.tomy.lib.ui.R
import kotlinx.android.synthetic.main.main_tab.*
import timber.log.Timber

/**@author Tomy
 * Created by Tomy on 2018/11/29.
 */
abstract class TabFragment: BaseSupportFragment() {


    override fun initView(root: View) {
        initTabLayout()
        initWidgetFrame()
    }

    private fun initTabLayout() {
        toolBar.apply {
            Timber.e("$this")
            layoutParams = layoutParams.apply {
                this as LayoutParams
                if (isTabLayoutTop()) {
                    topToTop = LayoutParams.PARENT_ID
                } else {
                    bottomToBottom = LayoutParams.PARENT_ID
                }
            }
        }
        tabBarLayout.setInfoList(getInfoArrayId())
    }

    private fun initWidgetFrame() {
        widgetFrame.apply {
            layoutParams = layoutParams.apply {
                this as LayoutParams
                if (isTabLayoutTop()) {
                    topToBottom     = R.id.toolBar
                    bottomToBottom  = LayoutParams.PARENT_ID
                } else {
                    bottomToTop     = R.id.toolBar
                    topToTop        = LayoutParams.PARENT_ID
                }
            }
        }
        val widgetLayoutId = getWidgetFrameLayoutId()
        if (widgetLayoutId > 0) {
            widgetFrame.addView(LayoutInflater.from(mContext).inflate(widgetLayoutId, widgetFrame, false))
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.main_tab
    }

    override fun isBindView(): Boolean {
        return false
    }

    /**
     * @return TabItem显示信息队列的id R.array.*
     */
    abstract fun getInfoArrayId(): Int

    /**
     * @return WidgetFrame的布局Id.
     */
    open fun getWidgetFrameLayoutId() = 0

    /**
     * @return 表明TabLayout是否在顶部.默认为true.
     */
    open fun isTabLayoutTop() = true
}