package com.tomy.lib.ui.widget

import android.content.Context
import timber.log.Timber

/**@author Tomy
 * Created by Tomy on 2018/11/23.
 */
abstract class BaseEnabler(var mContext: Context, var mSwitchWidget: SwitchWidgetController): SwitchWidgetController.OnSwitchChangeListener {

    private var mListeningToOnSwitchChange = false

    private var mStateMachineEvent = false


    init {
        mSwitchWidget.setListener(this)
        setupSwitchController()
    }

    open fun setupSwitchController() {
        Timber.e("setupSwitchController()")
    }

    open fun tearDownSwitchController() {
        Timber.e("tearDownSwitchController()")
        if (mListeningToOnSwitchChange) {
            mSwitchWidget.stopListening()
            mListeningToOnSwitchChange = false
        }
        mSwitchWidget.teardownView()
    }

    open fun resume() {
        Timber.e("resume()")
        if (!mListeningToOnSwitchChange) {
            mSwitchWidget.startListening()
            mListeningToOnSwitchChange = true
        }
    }

    open fun pause() {
        Timber.e("pause()")
        if (mListeningToOnSwitchChange) {
            mSwitchWidget.stopListening()
            mListeningToOnSwitchChange = false
        }
    }

    /**
     * 处理WiFi状态变化.
     * @param state Int当前的WiFi状态
     */
    open fun handleBluetoothStateChanged(state: Int) {
        Timber.e("handleBluetoothStateChanged()")
    }

    fun setSwitchBarChecked(checked: Boolean) {
        mStateMachineEvent = true
        mSwitchWidget.setChecked(checked)
        mStateMachineEvent = false
    }

    abstract fun switchToggled(isChecked: Boolean)

    override fun onSwitchToggled(isChecked: Boolean): Boolean {
        //Do nothing if called as a result of a state machine event.
        if (mStateMachineEvent) {
            return true
        }

        switchToggled(isChecked
        )
        return false
    }


}