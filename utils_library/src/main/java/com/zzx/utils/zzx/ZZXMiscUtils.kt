package com.zzx.utils.zzx

import android.content.Context
import android.provider.Settings
import timber.log.Timber
import java.io.*
import java.util.concurrent.Executors

/**@author Tomy
 * Created by Tomy on 2015-03-17.
 */
object ZZXMiscUtils {
    const val MISC = "/sys/devices/platform/zzx-misc/"
    const val BRIGHTNESS_PATH = "/sys/class/leds/lcd-backlight/brightness"
    const val ACC_PATH = MISC + "accdet_sleep"
    const val ACC_IGNORE_PATH = MISC + "close_acc_eint"
    const val ACC_RENEW = MISC + "accdet_renew"
    const val LAST_BRIGHTNESS = MISC + "bl_pwm_last"
    const val ASTER_PATH = "/sys/class/switch/astern_car/state"
    const val MUTE_PATH = MISC + "mute_flag_stats"
    const val RESET_TIMER_PATH = MISC + "reset_timer"

    const val SECOND_CAMERA_PATH = "/proc/driver/camsensor_sub"
    const val ASTERN_CAMERA = "0x00 0x06"
    const val ASTERN_RECORD = "0x00 0x05"

    const val RADAR_POWER = MISC + "radar_power"
    const val BT_POWER = MISC + "bt_power"
    const val BT_STATE = MISC + "bt_state"
    const val FM_ENABLE = MISC + "fmtx_enable"
    const val FM_FREQ = MISC + "fmtx_freq_hz"
    const val LOCAL_OUT = MISC + "speaker_power"
    const val AUDIO_OUT = MISC + "audio_sw_state"


    const val FLASH_PATH = MISC + "flash_stats"

    const val IR_CUT_PATH = MISC + "ir_cut_stats"

    const val IR_RED_PATH = MISC + "ir_led_stats"

    const val LASER_PATH = MISC + "lazer_stats"

    const val RGB_LED = MISC + "rgb_led_stats"

    const val GPS_PATH = "${MISC}gps_stats"

    const val USER_INFO_PATH = "${MISC}police_num_stats"

    const val PTT_SWITCH = "${MISC}ptt_exchange"

    const val AUTO_INFRARED = "${MISC}camera_light_state"

    const val LED_RED = "ff0000"
    const val LED_GREEN = "ff00"
    const val LED_BLUE = "ff"
    const val LED_YELLOW    = "ee2200"
    const val LED_DOWN  = "000000"

    const val BREATH_LIGHT  = " 1 2 2 2 2 2"
    const val NORMAL_LIGHT  = " 0 0 0 0 0 0"
    const val ONE_SHOT  = " 0 0 0 0 0 0"
//    const val ONE_SHOT      = " 1 2 1 0 0 0"


    const val OTG = MISC + "otg_en"

    const val OTG_PATH = MISC + "otg_en"

    const val USB_PATH = MISC + "usb_select"
    const val USB_POWER_PATH = MISC + "usb_power"
    const val GSENSOR = MISC + "gsensor_stats"
    const val GSENSOR_ENABLE = MISC + "gsensor_enable_stats"
    const val SPK_SYS: Byte = '0'.toByte()//系统喇叭出声
    const val SPK_BT: Byte = '1'.toByte()//蓝牙出声
    const val FMTX_SYS: Byte = '2'.toByte()//FM出声
    const val FMTX_BT: Byte = '3'.toByte()//蓝牙从FM出声
    const val AUX_SYS: Byte = '4'.toByte()//只有AUX输出
    const val AUX_BT: Byte = '5'.toByte()//只有AUX输出BT
    const val MUTE_ALL: Byte = '6'.toByte()//全部静音

    const val OPEN = "1"
    const val CLOSE = "0"

    private val FIXED_EXECUTOR = Executors.newFixedThreadPool(3)
    private val obj = Object()

    /**判断屏幕之前是否黑屏
     */
    val isLastScreenOff: Boolean
        get() {
            var reader: BufferedReader? = null
            try {
                reader = BufferedReader(FileReader(BRIGHTNESS_PATH))
                val brightness = reader.readLine()
                return brightness == "0"
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    reader?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
            return false
        }
    var brightness: String = ""

    /**判断是否静音
     */
    val isMute: Boolean
        get() {
            val inputStream: FileInputStream
            try {
                inputStream = FileInputStream(MUTE_PATH)
                val buffer = ByteArray(4)
                val count = inputStream.read(buffer)
                if (count > 0) {
                    val mute = String(buffer, 0, count)
                    inputStream.close()
                    return mute.contains("1")
                } else {
                    inputStream.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return false
        }

    /**切换到本地声道
     */
    fun setLocalOut() {
        write(AUDIO_OUT, SPK_SYS)
    }

    /**切换到蓝牙声道
     */
    fun setBTOut() {
        write(AUDIO_OUT, SPK_BT)
    }

    /**切换至FM声道
     */
    fun setFMOut() {
        write(AUDIO_OUT, FMTX_SYS)
    }

    /**切换到AUX声道
     */
    fun setAUXOut() {
        write(AUDIO_OUT, AUX_SYS)
    }

    /**打开FM供电.需要开机设置打开.
     */
    fun openFM() {
        write(FM_ENABLE, OPEN)
    }

    /**关闭FM供电
     */
    fun closeFM() {
        write(FM_ENABLE, CLOSE)
    }

    /**打开雷达供电
     */
    fun openRadar() {
        write(RADAR_POWER, OPEN)
    }

    /**关闭雷达供电
     */
    fun closeRadar() {
        write(RADAR_POWER, CLOSE)
    }

    /**打开蓝牙供电.需要开机设置打开
     */
    fun openBTPower() {
        write(BT_POWER, OPEN)
    }

    /**关闭蓝牙供电
     */
    fun closeBTPower() {
        write(BT_POWER, CLOSE)
    }

    fun openBTOUT() {
        write(BT_STATE, OPEN)
    }

    fun closeBTOUT() {
        write(BT_STATE, CLOSE)
    }

    /**切换到后录摄像(即后视自动录像),此处的切换只是底层切换视频流,上层仍需打开后摄像头.
     */
    fun toggleBackRecord() {
        write(SECOND_CAMERA_PATH, ASTERN_RECORD)
    }

    /**
     * 写GPS数据
     * @param gps String
     */
    fun writeGps(gps: String) {
        write(GPS_PATH, gps)
    }

    fun writeUserInfo(info: String) {
        write(USER_INFO_PATH, info)
    }

    /**切换到倒车后视,同上
     */
    fun toggleBackCamera() {
        write(SECOND_CAMERA_PATH, ASTERN_CAMERA)
    }

    fun openLOCAL() {
        write(LOCAL_OUT, OPEN)
    }

    fun closeLOCAL() {
        write(LOCAL_OUT, CLOSE)
    }

    /**
     * @param path String
     * @param cmd String
     */
    fun write(path: String, cmd: String)  {
        FIXED_EXECUTOR.execute {
            try {
//                synchronized(obj) {
//                    JniFile().writeOnce(path, cmd)
                    Timber.d("write: path = $path; cmd = $cmd; length = ${cmd.length}")
                    val file = File(path)
                    file.writeText(cmd)

                val regex = Regex("[\n\r]")
                val temp = file.readText().replace(regex, "")
                Timber.d("read: path = $path; temp = $temp;\n length = ${temp.length}")
                if (temp != cmd) {
                        Timber.w("write again: path = $path; cmd = $cmd")
                        file.writeText(cmd)
                    }
//                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setFlashState(open: Boolean) {
        write(FLASH_PATH, if (open) OPEN else CLOSE)
    }

    fun setLaserState(open: Boolean) {
        write(LASER_PATH, if (open) OPEN else CLOSE)
    }

    fun setIrRedState(open: Boolean) {
        if (open) {
            write(IR_CUT_PATH, OPEN)
            write(IR_RED_PATH, OPEN)
        } else {
            write(IR_RED_PATH, CLOSE)
            write(IR_CUT_PATH, CLOSE)
        }
    }

    fun setPttSwitch(enabled: Boolean) {
        write(PTT_SWITCH, if (enabled) OPEN else CLOSE)
    }

    fun setAutoInfrared(enabled: Boolean) {
        write(AUTO_INFRARED, if (enabled) OPEN else CLOSE)
    }

    /**重置强制关机计时器.若不重置计时,则会在休眠开始后2分钟后若休眠失败则强制关机
     */
    fun resetTimer() {
        write(RESET_TIMER_PATH, OPEN)
    }

    private fun write(path: String, cmd: Byte) {
        /*Observable.just(path)
                .subscribeOn(Schedulers.single())
                .subscribe {*/
//                    Timber.e("write: path = $path; cmd = $cmd. Thread = ${Thread.currentThread().name}")
                    var outputStream: FileOutputStream? = null
                    try {
                        outputStream = FileOutputStream(path)
                        outputStream.write(cmd.toInt())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        try {
                            outputStream?.close()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }
//                }

    }

    /**控制OTG功能
     */
    fun toggleOtg(open: Boolean) {
        write(OTG, if (open) OPEN else CLOSE)
        write(USB_PATH, if (open) OPEN else CLOSE)
        write(USB_POWER_PATH, if (open) OPEN else CLOSE)
    }

    /**
     * @see isLedEnabled
     * @param status String [LED_BLUE]/[LED_GREEN]/[LED_RED]/[LED_YELLOW]/[LED_DOWN]
     * @param breath Boolean
     * @param context Context?
     * @param oneShot Boolean
     */
    fun toggleLed(status: String, breath: Boolean = false, context: Context? = null, oneShot: Boolean = false) {
        if (breath || oneShot) {
            if (!isLedEnabled(context!!)) {
                return
            }
        }
        val state = when {
            breath -> "$status$BREATH_LIGHT"
            oneShot -> "$status$ONE_SHOT"
            else -> "$status$NORMAL_LIGHT"
        }
        write(RGB_LED, state)
    }

    fun isLedEnabled(context: Context): Boolean {
        return Settings.System.getInt(context.contentResolver, LED_ENABLED, 1) == 1
    }

    const val LED_ENABLED = "zzx_led_enabled"

    /**关屏
     */
    fun screenOff() {
        write(BRIGHTNESS_PATH, CLOSE)
    }

    fun screenlastOff() {
        var reader: BufferedReader? = null
        try {
            reader = BufferedReader(FileReader(LAST_BRIGHTNESS))
            brightness = reader.readLine()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                reader?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        write(LAST_BRIGHTNESS, CLOSE)
    }

    /**亮屏
     */
    fun screenOn() {
        var reader: BufferedReader? = null
        var outputStream: FileWriter? = null
        try {
            reader = BufferedReader(FileReader(LAST_BRIGHTNESS))
            outputStream = FileWriter(BRIGHTNESS_PATH)
            val brightness = reader.readLine()
            outputStream.write(brightness)
            outputStream.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                reader?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                outputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    /**重新检测ACC供电状态.
     * 就是让中间层重新发一遍ACC状态广播.某些特定情况下过滤或者屏蔽了某次ACC状态执行某些操作后要再次检测ACC状态.
     */
    fun accRenew() {
        write(ACC_RENEW, OPEN)
    }

    /**开始执行休眠操作.调用此方法就是底层开始执行进入休眠操作.
     * PS:
     * 休眠之前需要关闭所有影响休眠的操作.如下:
     * 1.  CPU锁.
     * 2.  GPS,Camera.
     * 3.  Wifi热点.等等模块.否则休眠失败.
     */
    fun setACCOff() {
        write(ACC_PATH, OPEN)
    }

    /**屏蔽ACC检测.即此时之后只会在休眠成功后才开始重新检测ACC.一般开始休眠时执行此步.
     * 这是为避免在执行休眠的过程中ACC不断的上下电导致不断的休眠唤醒.
     */
    fun setIgnoreACCOff() {
        write(ACC_IGNORE_PATH, OPEN)
    }

    fun setAccOn() {
        write(ACC_PATH, CLOSE)
    }

    /**设置静音
     */
    fun setMuteState(mute: Boolean) {
        if (mute) {
            write(MUTE_PATH, OPEN)
        } else {
            write(MUTE_PATH, CLOSE)
        }

    }

    fun read(path: String): String? {
        var result: String? = null
        var reader: BufferedReader? = null
        try {
            reader = BufferedReader(FileReader(path))
            result = reader.readLine()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                reader?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        return result
    }

    /**打开呼吸灯(开始录像时调用.)
     */
    fun openBreathLight() {
        write(RGB_LED, OPEN)
    }

    /**打开呼吸灯(休眠时时调用.)
     */
    fun openBreathLightAccOff() {
        write(RGB_LED, '2'.toByte())
    }

    /**关闭呼吸灯
     */
    fun closeBreathLight() {
        write(RGB_LED, CLOSE)
    }

    /**
     * 震动唤醒
     */
    fun setGsensor(): Boolean {
        var reader: BufferedReader? = null
        var gSensor = "0"
        val file = File(GSENSOR)
        if (!file.exists()) {
            return false
        }
        try {
            reader = BufferedReader(FileReader(GSENSOR))
            gSensor = reader.readLine()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                reader?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return gSensor == "1"
    }

    /**
     * 打开震动唤醒
     */
    fun openGsenor() {
        write(GSENSOR_ENABLE, 1)
    }

    /**
     * 关闭震动唤醒
     */
    fun closeGsenor() {
        write(GSENSOR_ENABLE, 0)
    }
}
