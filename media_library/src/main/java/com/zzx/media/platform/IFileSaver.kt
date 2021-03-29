/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 *
 * MediaTek Inc. (C) 2014. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package com.zzx.media.platform

import android.location.Location
import java.io.File

interface IFileSaver {

    /*val waitingDataSize: Long

    val isEnoughSpace: Boolean

    val availableSpace: Long*/

    enum class FILE_TYPE {
        JPEG,
        RAW,
        VIDEO,
        PIPVIDEO,
        PANORAMA,
        SLOWMOTION,
        REFOCUSIMAGE
    }

    interface OnFileSaveListener {

        fun onQueueStatus(full: Boolean)

        fun onFileSaved(request: ISaveRequest)

        fun onSaveDone()

    }

    /**
     * init fileSaver.
     *
     * @param fileType
     * file type(eg:mpo, jpg)
     */
    fun init(fileType: FILE_TYPE, outputFileFormat: Int, resolution: String, rotation: Int)

    fun unInit()

    /**
     * whether dng capturing is enabled.
     * @param isEnable true or false.
     */
    fun setRawFlagEnabled(isEnable: Boolean)

    /**
     * save file.
     *
     * @param photoData
     * the data of photo
     * @param file
     * file name include extension
     * @param date
     * capture date
     * @param location
     * the capture location
     * @param tag
     * add for Refocus image database
     * @param listener
     * receives callback after save finish.
     * @return true or false
     */
    fun savePhotoFile(photoData: ByteArray?, file: File?, date: Long = 0,
                      location: Location? = null, tag: Int = 0, listener: OnFileSaveListener? = null): Boolean

    /**
     * save the dng image.
     * @param dngData
     * the dng data buffer.
     * @param width
     * the width of the dng.
     * @param height
     * the height of the dng.
     * @param fileName
     * the dng file name.
     * @param date
     * the dng captured data.
     * @param location
     * the dng captured location.
     * @param tag
     * add for Refocus image database.
     * @param listener
     * the listener after saving the dng.
     * @return whether it succeed for saving.
     */
    fun saveRawFile(dngData: ByteArray, width: Int, height: Int,
                    fileName: String, date: Long, location: Location, tag: Int,
                    listener: OnFileSaveListener): Boolean

    fun saveVideoFile(location: Location, tempPath: String, duration: Long,
                      tag: Int, listener: OnFileSaveListener): Boolean

    fun waitDone()
}
