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

import android.hardware.Camera
import java.lang.reflect.Method

object ExtendedFaceWrapper : Wrapper() {
    private const val CLASS_NAME = "com.qualcomm.qti.camera.ExtendedFace"
    private var mExtendFaceClass: Class<*>? = null
    fun isExtendedFaceInstance(`object`: Any?): Boolean {
        if (mExtendFaceClass == null) {
            try {
                mExtendFaceClass = Class.forName(CLASS_NAME)
            } catch (exception: Exception) {
                exception.printStackTrace()
                return false
            }
        }
        return mExtendFaceClass!!.isInstance(`object`)
    }

    private var method_getSmileDegree: Method? = null
    fun getSmileDegree(face: Camera.Face?): Int {
        var degree = 0
        try {
            if (method_getSmileDegree == null) {
                method_getSmileDegree = getMethod("getSmileDegree")
            }
            degree = method_getSmileDegree!!.invoke(face) as Int
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return degree
    }

    private var method_getSmileScore: Method? = null
    fun getSmileScore(face: Camera.Face?): Int {
        var score = 0
        try {
            if (method_getSmileScore == null) {
                method_getSmileScore = getMethod("getSmileScore")
            }
            score = method_getSmileScore!!.invoke(face) as Int
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return score
    }

    private var method_getBlinkDetected: Method? = null
    fun getBlinkDetected(face: Camera.Face?): Int {
        var blink = 0
        try {
            if (method_getBlinkDetected == null) {
                method_getBlinkDetected = getMethod("getBlinkDetected")
            }
            blink = method_getBlinkDetected!!.invoke(face) as Int
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return blink
    }

    private var method_getFaceRecognized: Method? = null
    fun getFaceRecognized(face: Camera.Face?): Int {
        var faces = 0
        try {
            if (method_getFaceRecognized == null) {
                method_getFaceRecognized = getMethod("getFaceRecognized")
            }
            faces = method_getFaceRecognized!!.invoke(face) as Int
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return faces
    }

    private var method_getGazeAngle: Method? = null
    fun getGazeAngle(face: Camera.Face?): Int {
        var angle = 0
        try {
            if (method_getGazeAngle == null) {
                method_getGazeAngle = getMethod("getGazeAngle")
            }
            angle = method_getGazeAngle!!.invoke(face) as Int
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return angle
    }

    private var method_getUpDownDirection: Method? = null
    fun getUpDownDirection(face: Camera.Face?): Int {
        var direction = 0
        try {
            if (method_getUpDownDirection == null) {
                method_getUpDownDirection = getMethod("getUpDownDirection")
            }
            direction = method_getUpDownDirection!!.invoke(face) as Int
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return direction
    }

    private var method_getLeftRightDirection: Method? = null
    fun getLeftRightDirection(face: Camera.Face?): Int {
        var direction = 0
        try {
            if (method_getLeftRightDirection == null) {
                method_getLeftRightDirection = getMethod("getLeftRightDirection")
            }
            direction = method_getLeftRightDirection!!.invoke(face) as Int
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return direction
    }

    private var method_getRollDirection: Method? = null
    fun getRollDirection(face: Camera.Face?): Int {
        var direction = 0
        try {
            if (method_getRollDirection == null) {
                method_getRollDirection = getMethod("getRollDirection")
            }
            direction = method_getRollDirection!!.invoke(face) as Int
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return direction
    }

    private var method_getLeftEyeBlinkDegree: Method? = null
    fun getLeftEyeBlinkDegree(face: Camera.Face?): Int {
        var degree = 0
        try {
            if (method_getLeftEyeBlinkDegree == null) {
                method_getLeftEyeBlinkDegree = getMethod("getLeftEyeBlinkDegree")
            }
            degree = method_getLeftEyeBlinkDegree!!.invoke(face) as Int
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return degree
    }

    private var method_getRightEyeBlinkDegree: Method? = null
    fun getRightEyeBlinkDegree(face: Camera.Face?): Int {
        var degree = 0
        try {
            if (method_getRightEyeBlinkDegree == null) {
                method_getRightEyeBlinkDegree = getMethod("getRightEyeBlinkDegree")
            }
            degree = method_getRightEyeBlinkDegree!!.invoke(face) as Int
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return degree
    }

    private var method_getLeftRightGazeDegree: Method? = null
    fun getLeftRightGazeDegree(face: Camera.Face?): Int {
        var degree = 0
        try {
            if (method_getLeftRightGazeDegree == null) {
                method_getLeftRightGazeDegree = getMethod("getLeftRightGazeDegree")
            }
            degree = method_getLeftRightGazeDegree!!.invoke(face) as Int
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return degree
    }

    private var method_getTopBottomGazeDegree: Method? = null
    fun getTopBottomGazeDegree(face: Camera.Face?): Int {
        var degree = 0
        try {
            if (method_getTopBottomGazeDegree == null) {
                method_getTopBottomGazeDegree = getMethod("getTopBottomGazeDegree")
            }
            degree = method_getTopBottomGazeDegree!!.invoke(face) as Int
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return degree
    }

    @Throws(Exception::class)
    private fun getMethod(name: String): Method? {
        if (mExtendFaceClass == null) {
            mExtendFaceClass = Class.forName(CLASS_NAME)
        }
        return mExtendFaceClass!!.getDeclaredMethod(name)
    }
}