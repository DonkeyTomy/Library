package com.tomy.lib.ui.view.preference

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ListView
import com.tomy.lib.ui.R

/**@author Tomy
 * Created by Tomy on 2018/10/12.
 */
abstract class CustomListPreference(context: Context, attributeSet: AttributeSet): BaseListPreference(context, attributeSet) {


    override fun getDialogLayoutId(): Int {
        return R.layout.camera_setting_dialog
    }

    override fun bindDialogLayout(view: View) {
        bindListView(view.findViewById(R.id.list_view))
    }

    abstract fun bindListView(listView: ListView)
}