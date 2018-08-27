package com.zzx.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;

/**@author Tomy
 * Created by Tomy on 2015-03-17.
 */
public class ZZXMiscUtils {
    public static final String MISC = "/sys/devices/platform/zzxcomm_dev/";
    public static final String BRIGHTNESS_PATH  = "/sys/class/leds/lcd-backlight/brightness";
    public static final String ACC_PATH = MISC + "accdet_sleep";
    public static final String ACC_IGNORE_PATH = MISC + "close_acc_eint";
    public static final String ACC_RENEW    = MISC + "accdet_renew";
    public static final String LAST_BRIGHTNESS  = MISC + "bl_pwm_last";
    public static final String ASTER_PATH   = "/sys/class/switch/astern_car/state";
    public static final String MUTE_PATH    = MISC + "mute_flag_stats";
    public static final String RESET_TIMER_PATH = MISC + "reset_timer";

    public static final String SECOND_CAMERA_PATH   = "/proc/driver/camsensor_sub";
    public static final String ASTERN_CAMERA    = "0x00 0x06";
    public static final String ASTERN_RECORD    = "0x00 0x05";

    public static final String RADAR_POWER  = MISC + "radar_power";
    public static final String BT_POWER = MISC + "bt_power";
    public static final String BT_STATE = MISC + "bt_state";
    public static final String FM_ENABLE    = MISC + "fmtx_enable";
    public static final String FM_FREQ      = MISC + "fmtx_freq_hz";
    public static final String LOCAL_OUT    = MISC + "speaker_power";
    public static final String AUDIO_OUT    = MISC + "audio_sw_state";
    public static final String OTG  = MISC + "otg_en";

    public static final String OTG_PATH = MISC + "otg_en";

    public static final String USB_PATH = MISC + "usb_select";
    public static final String USB_POWER_PATH = MISC + "usb_power";
    public static final String LIGHT    = MISC + "rgb_led_stats";
    public static final String GSENSOR    = MISC + "gsensor_stats";
    public static final String GSENSOR_ENABLE = MISC +"gsensor_enable_stats";
    public static final byte SPK_SYS    = '0';//系统喇叭出声
    public static final byte SPK_BT     = '1';//蓝牙出声
    public static final byte FMTX_SYS   = '2';//FM出声
    public static final byte FMTX_BT    = '3';//蓝牙从FM出声
    public static final byte AUX_SYS    = '4';//只有AUX输出
    public static final byte AUX_BT     = '5';//只有AUX输出BT
    public static final byte MUTE_ALL   = '6';//全部静音
    public static final byte OPEN   = '1';
    public static final byte CLOSE  = '0';

    /**切换到本地声道
     * */
    public static void setLocalOut() {
        write(AUDIO_OUT, SPK_SYS);
    }

    /**切换到蓝牙声道
     * */
    public static void setBTOut() {
        write(AUDIO_OUT, SPK_BT);
    }

    /**切换至FM声道
     * */
    public static void setFMOut() {
        write(AUDIO_OUT, FMTX_SYS);
    }

    /**切换到AUX声道
     * */
    public static void setAUXOut() {
        write(AUDIO_OUT, AUX_SYS);
    }

    /**打开FM供电.需要开机设置打开.
     * */
    public static void openFM() {
        write(FM_ENABLE, OPEN);
    }

    /**关闭FM供电
     * */
    public static void closeFM() {
        write(FM_ENABLE, CLOSE);
    }

    /**打开雷达供电
     * */
    public static void openRadar() {
        write(RADAR_POWER, OPEN);
    }

    /**关闭雷达供电
     * */
    public static void closeRadar() {
        write(RADAR_POWER, CLOSE);
    }

    /**打开蓝牙供电.需要开机设置打开
     * */
    public static void openBTPower() {
        write(BT_POWER, OPEN);
    }

    /**关闭蓝牙供电
     * */
    public static void closeBTPower() {
        write(BT_POWER, CLOSE);
    }

    public static void openBTOUT() {
        write(BT_STATE, OPEN);
    }

    public static void closeBTOUT() {
        write(BT_STATE, CLOSE);
    }

    /**切换到后录摄像(即后视自动录像),此处的切换只是底层切换视频流,上层仍需打开后摄像头.
     * */
    public static void toggleBackRecord() {
        write(SECOND_CAMERA_PATH, ASTERN_RECORD);
    }

    /**切换到倒车后视,同上
     * */
    public static void toggleBackCamera() {
        write(SECOND_CAMERA_PATH, ASTERN_CAMERA);
    }

    public static void openLOCAL() {
        write(LOCAL_OUT, OPEN);
    }

    public static void closeLOCAL() {
        write(LOCAL_OUT, CLOSE);
    }

    private static void write(String path, String cmd) {
        FileWriter outputStream = null;
        try {
            outputStream    = new FileWriter(path);
            outputStream.write(cmd);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**重置强制关机计时器.若不重置计时,则会在休眠开始后2分钟后若休眠失败则强制关机
     * **/
    public static void resetTimer() {
        write(RESET_TIMER_PATH, OPEN);
    }

    private static void write(String path, byte cmd) {
        FileOutputStream outputStream = null;
        try {
            outputStream    = new FileOutputStream(path);
            outputStream.write(cmd);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**控制OTG功能
     * */
    public static void toggleOtg(boolean open) {
        write(OTG, open ? OPEN : CLOSE);
        write(USB_PATH, open ? OPEN : CLOSE);
        write(USB_POWER_PATH, open ? OPEN : CLOSE);
    }

    /**判断屏幕之前是否黑屏
     * */
    public static boolean isLastScreenOff() {
        BufferedReader reader = null;
        try {
            reader     = new BufferedReader(new FileReader(BRIGHTNESS_PATH));
            String brightness = reader.readLine();
            return brightness.equals("0");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**关屏
     * */
    public static void screenOff() {
        write(BRIGHTNESS_PATH, CLOSE);
    }
    public static String brightness;
    public static void screenlastOff(){
        BufferedReader reader = null;
        try {
            reader     = new BufferedReader(new FileReader(LAST_BRIGHTNESS));
            brightness = reader.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        write(LAST_BRIGHTNESS, CLOSE);
    }

    /**亮屏
     * */
    public static void screenOn() {
        BufferedReader reader = null;
        FileWriter outputStream = null;
        try {
            reader     = new BufferedReader(new FileReader(LAST_BRIGHTNESS));
            outputStream    = new FileWriter(BRIGHTNESS_PATH);
            String brightness1 = reader.readLine();
            outputStream.write(brightness1);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**重新检测ACC供电状态.
     * 就是让中间层重新发一遍ACC状态广播.某些特定情况下过滤或者屏蔽了某次ACC状态执行某些操作后要再次检测ACC状态.
     * */
    public static void accRenew() {
        write(ACC_RENEW, OPEN);
    }

    /**开始执行休眠操作.调用此方法就是底层开始执行进入休眠操作.
     * PS:
     * 休眠之前需要关闭所有影响休眠的操作.如下:
     * 1.  CPU锁.
     * 2.  GPS,Camera.
     * 3.  Wifi热点.等等模块.否则休眠失败.
     * */
    public static void setACCOff() {
        write(ACC_PATH, OPEN);
    }

    /**屏蔽ACC检测.即此时之后只会在休眠成功后才开始重新检测ACC.一般开始休眠时执行此步.
     * 这是为避免在执行休眠的过程中ACC不断的上下电导致不断的休眠唤醒.
     * */
    public static void setIgnoreACCOff() {
        write(ACC_IGNORE_PATH, OPEN);
    }

    public static void setAccOn() {
        write(ACC_PATH, CLOSE);
    }

    /**设置静音
     * */
    public static void setMuteState(boolean mute) {
        if (mute) {
            write(MUTE_PATH, OPEN);
        } else {
            write(MUTE_PATH, CLOSE);
        }

    }

    /**判断是否静音
     * */
    public static boolean isMute() {
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(MUTE_PATH);
            byte[] buffer = new byte[4];
            int count = inputStream.read(buffer);
            if (count > 0) {
                String mute = new String(buffer, 0, count);
                inputStream.close();
                return mute.contains("1");
            } else {
                inputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String read(String path) {
        String result = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(path));
            result = reader.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }
    /**打开呼吸灯(开始录像时调用.)
     * */
    public static void openBreathLight() {
        write(LIGHT, OPEN);
    }

    /**打开呼吸灯(休眠时时调用.)
     * */
    public static void openBreathLightAccOff() {
        write(LIGHT, (byte)'2');
    }

    /**关闭呼吸灯
     * */
    public static void closeBreathLight() {
        write(LIGHT, CLOSE);
    }
    /**
     * 震动唤醒
     */
    public static boolean  setGsensor() {
        BufferedReader reader = null;
        String gSensor ="0";
        File file = new File(GSENSOR);
        if (!file.exists()) {
            return false;
        }
        try {
            reader     = new BufferedReader(new FileReader(GSENSOR));
            gSensor = reader.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return gSensor.equals("1");
    }
    /**
     * 打开震动唤醒
     */
    public static void openGsenor(){
        write(GSENSOR_ENABLE,null);
    }
    /**
     * 关闭震动唤醒
     */
    public static void closeGsenor() {
        write(GSENSOR_ENABLE, null);
    }
}
