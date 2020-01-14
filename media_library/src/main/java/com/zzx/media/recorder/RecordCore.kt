package com.zzx.media.recorder

import java.util.concurrent.atomic.AtomicInteger

/**@author Tomy
 * Created by Tomy on 2020/1/6.
 */
class RecordCore {

    private val mStatus = AtomicInteger(IDLE)

    fun isIDLE() = mStatus.get() == IDLE

    fun isRecording() = mStatus.get().and(RECORDING) == RECORDING

    fun isLooping() = mStatus.get().and(LOOPING) == LOOPING

    fun startLoop() {
        mStatus.set(mStatus.get().or(LOOPING))
    }

    fun stopLoop() {
        mStatus.set(mStatus.get().and(LOOPING.inv()))
    }

    fun startRecord() {
        mStatus.set(mStatus.get().or(RECORDING))
    }

    fun stopRecord() {
        mStatus.set(mStatus.get().and(RECORDING.inv()))
    }

    companion object {
        const val IDLE = 0x0
        const val LOOPING   = 0x01
        const val RECORDING = 0x02
    }

}