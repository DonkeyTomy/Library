package com.tomy.lib.ui.view.preference

import android.content.Context
import android.preference.PreferenceManager
import android.preference.PreferenceScreen
import android.widget.ListView

/**@author Tomy
 * Created by Tomy on 2018/10/10.
 */
class TPreferenceManager(private var context: Context) {

    private val mPreferenceManagerCls = PreferenceManager::class.java

    private val mGetScreenMethod by lazy {
        mPreferenceManagerCls.getDeclaredMethod("getPreferenceScreen").apply {
            isAccessible = true
        }
    }

    private val mInflateMethod by lazy {
        mPreferenceManagerCls.getDeclaredMethod("inflateFromResource", Context::class.java, Integer.TYPE, PreferenceScreen::class.java).apply {
            isAccessible = true
        }
    }

    private val mSetPreferenceMethod by lazy {
        mPreferenceManagerCls.getDeclaredMethod("setPreferences", PreferenceScreen::class.java).apply {
            isAccessible = true
        }
    }

    private val mPreferenceManager by lazy {
        mPreferenceManagerCls.getDeclaredConstructor(Context::class.java).let {
            it.isAccessible = true
            it.newInstance(context)
        }
    }

    fun getPreferenceScreen(): PreferenceScreen {
        return mGetScreenMethod.invoke(mPreferenceManager) as PreferenceScreen
    }

    /**
     * 加载Preference的xml文件
     * @param xmlId Int.包含Preferences的xml文件.
     */
    fun inflatePreferenceScreen(xmlId: Int) {
        val screen: PreferenceScreen? = null
        val resultScreen = mInflateMethod.invoke(mPreferenceManager, context, xmlId, screen) as PreferenceScreen
        mSetPreferenceMethod.invoke(mPreferenceManager, resultScreen)
    }

    /**
     * [inflatePreferenceScreen]之后将得到的PreferenceScreen绑定到ListView中.
     * @param listView ListView
     * @see inflatePreferenceScreen
     */
    fun bindPreferences(listView: ListView) {
        val screen = getPreferenceScreen()
        screen.bind(listView)
    }

    fun removeAll() {
        try {
            getPreferenceScreen().removeAll()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}