/*
 * Copyright (c) 2017, The Linux Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above
 *    copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *  * Neither the name of The Linux Foundation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.zzx.media.camera.qcom.wrapper

import timber.log.Timber
import java.lang.reflect.Field

open class Wrapper {
    companion object {
        protected const val DEBUG = true
        protected const val TAG = "Wrapper"
        protected fun getFieldValue(field: Field?, def: Int): Int {
            var value = def
            if (field != null) {
                try {
                    value = field[null] as Int
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            }
            return value
        }

        @JvmStatic
        protected fun getFieldValue(field: Field?, def: String?): String? {
            var value = def
            if (field != null) {
                try {
                    value = field[null] as String
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            }
            return value
        }

        @JvmStatic
        protected fun getField(classInstance: Class<*>, name: String): Field? {
            Timber.d("getField:$classInstance field:$name")
            if (DEBUG) {
                Timber.e("$classInstance no $name")
                return null
            }
            var field: Field? = null
            try {
                field = classInstance.getField(name)
                Timber.d("getField:$classInstance $name")
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            return field
        }
    }
}