/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tomy.lib.ui.widget

import android.widget.Switch

/*
 * A controller class for general switch widget handling. We have different containers that provide
 * different forms of switch layout. Provide a centralized control for updating the switch widget.
 */
abstract class SwitchWidgetController {

    protected var mListener: OnSwitchChangeListener? = null

    /**
     * Get the checked state for the switch.
     *
     * @return true if the switch is currently checked, false otherwise.
     */
    abstract fun isChecked(): Boolean


    /**
     * Set the checked state for the switch.
     *
     * @param checked whether the switch should be checked or not.
     */
    abstract fun setChecked(checked: Boolean)

    /**
     * Get the underlying switch widget.
     *
     * @return the switch widget.
     */
    abstract fun getSwitch(): Switch?

    /**
     * Interface definition for a callback to be invoked when the switch has been toggled.
     */
    interface OnSwitchChangeListener {
        /**
         * Called when the checked state of the Switch has changed.
         *
         * @param isChecked The new checked state of switchView.
         *
         * @return true to update the state of the switch with the new value.
         */
        fun onSwitchToggled(isChecked: Boolean): Boolean
    }

    /**
     * Perform any view setup.
     */
    fun setupView() {
        getSwitch()?.apply {
            setOnCheckedChangeListener { _, isChecked ->
                mListener?.onSwitchToggled(isChecked)
            }
        }

    }

    /**
     * Perform any view teardown.
     */
    fun teardownView() {
        getSwitch()?.apply {
            setOnCheckedChangeListener(null)
        }
    }

    /**
     * Set the callback to be invoked when the switch is toggled by the user (but before the
     * internal state has been updated).
     *
     * @param listener the callback to be invoked
     */
    fun setListener(listener: OnSwitchChangeListener) {
        mListener = listener
    }

    /**
     * Update the preference title associated with the switch.
     *
     * @param isChecked whether the switch is currently checked
     */
    abstract fun updateTitle(isChecked: Boolean)

    /**
     * Start listening to switch toggling.
     */
    abstract fun startListening()

    /**
     * Stop listening to switch toggling.
     */
    abstract fun stopListening()

    /**
     * Set the enabled state for the switch.
     *
     * @param enabled whether the switch should be enabled or not.
     */
    abstract fun setEnabled(enabled: Boolean)

}