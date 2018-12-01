package com.tomy.lib.ui.view.layout

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.design.widget.TabLayout
import android.util.AttributeSet
import com.tomy.lib.ui.R

/**@author Tomy
 * Created by Tomy on 2018/11/29.
 * tabLayout.id = [android.R.id.tabhost]
 */
open class TabBarLayout(context: Context, attrs: AttributeSet): TabLayout(context, attrs) {
    private var mInfos: Array<CharSequence>? = null
    private var mIcons: Array<Drawable>? = null

    init {
        context.obtainStyledAttributes(attrs, R.styleable.TabBarLayout).apply {
            mInfos = getTextArray(R.styleable.TabBarLayout_infos)
            recycle()
        }
        refreshView()
    }

    fun setInfoList(infos: Array<CharSequence>) {
        mInfos = infos
    }

    fun setInfoList(arrayId: Int) {
        val infos = resources.getStringArray(arrayId)
        mInfos = Array(infos.size) {
            return@Array ""
        }
        for (i in 0 until infos.size) {
            mInfos?.set(i, infos[i])
        }
        refreshView()
    }

    fun refreshView() {
        mInfos?.forEach {
            addTab(newTab().apply {
                setCustomView(getLayoutId())
                text = it
            })
        }
    }

    //返回自定义的Tab的布局Id.
    open fun getLayoutId(): Int {
        return R.layout.tab_item_m9
    }

}