package com.tomy.lib.ui.view.preference.core

import android.content.Context
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceGroup
import android.support.v7.preference.PreferenceScreen
import timber.log.Timber

/**@author Tomy
 * Created by Tomy on 2018/11/21.
 */
abstract class AbstractPreferenceController(val mContext: Context) {

    /**
     * @param screen
     */
    open fun displayPreference(screen: PreferenceScreen) {
        Timber.e("displayPreference().isAvailable = ${isAvailable()}")
        if (isAvailable()) {
            if (this is Preference.OnPreferenceChangeListener) {
                screen.findPreference(getPreferenceKey()).apply {
                    onPreferenceChangeListener = this as Preference.OnPreferenceChangeListener
                }
            }
        } else {
            removePreference(screen, getPreferenceKey())
        }
    }

    /**
     * @return Boolean true if this Preference is available(need display).
     */
    abstract fun isAvailable(): Boolean

    /**
     * @param preference Preference update current State of preference.(summary, switch state, etc).
     */
    open fun updateState(preference: Preference) {

    }

    /**
     * Handles preference tree click.
     * @param preference Preference the preference being clicked.
     * @return Boolean true if the click is handled.
     */
    fun handlePreferenceTreClick(preference: Preference): Boolean {
        return false
    }

    /**
     * @return String return the key of this Preference.
     */
    abstract fun getPreferenceKey(): String


    /**
     * removes the preference from PreferenceScreen.
     * @param screen PreferenceScreen
     * @param key String
     */
    protected fun removePreference(screen: PreferenceScreen, key: String) {
        findAndRemovePreference(screen, key)
    }

    private fun findAndRemovePreference(preferenceGroup: PreferenceGroup, key: String): Boolean {
        for (i in 0 until preferenceGroup.preferenceCount) {
            val preference = preferenceGroup.getPreference(i)
            val curKey = preference.key
            if (curKey != null && curKey == key) {
                return preferenceGroup.removePreference(preference)
            }
            if (preference is PreferenceGroup) {
                if (findAndRemovePreference(preference, key)) {
                    return true
                }
            }
        }
        return false
    }

    open fun onStart() {
        Timber.e("onStart().key = ${getPreferenceKey()}")
    }

    open fun onResume() {
        Timber.e("onResume().key = ${getPreferenceKey()}")
    }

    open fun onPause() {
        Timber.e("onPause()")
    }

    open fun onStop() {
        Timber.e("onStop()")
    }

}