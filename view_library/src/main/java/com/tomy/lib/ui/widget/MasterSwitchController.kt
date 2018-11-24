package com.tomy.lib.ui.widget

import android.support.v7.preference.Preference
import android.widget.Switch
import com.tomy.lib.ui.view.preference.MasterSwitchPreference

/**@author Tomy
 * Created by Tomy on 2018/11/21.
 *
 * The [MasterSwitchController] that is used to update the switch widget in the [MasterSwitchPreference]layout.
 */
class MasterSwitchController(val mPreference: MasterSwitchPreference): SwitchWidgetController(), Preference.OnPreferenceChangeListener {


    override fun isChecked(): Boolean {
        return mPreference.isChecked()
    }

    override fun setChecked(checked: Boolean) {
        mPreference.setChecked(checked)
    }

    override fun getSwitch(): Switch? {
        return mPreference.getSwitch()
    }

    override fun updateTitle(isChecked: Boolean) {
    }

    override fun startListening() {
        mPreference.onPreferenceChangeListener = this
    }

    override fun stopListening() {
        mPreference.onPreferenceChangeListener = null
    }

    override fun setEnabled(enabled: Boolean) {
        mPreference.isEnabled = enabled
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        return mListener?.onSwitchToggled(newValue as Boolean) == true
    }
}