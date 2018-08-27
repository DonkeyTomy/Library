package com.zzx.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings.System;

/**@author Tomy
 * Created by Tomy on 2014-12-31.
 */
public class BrightnessUtils {
    private int mMaxLevel = 0;
    private ContentResolver mResolver;

    public BrightnessUtils(Context context) {
        mResolver = context.getContentResolver();
    }

    public BrightnessUtils(Context context, int totalLevel) {
        this(context);
        setTotalLevel(totalLevel);
    }

    public void setTotalLevel(int maxLevel) {
        mMaxLevel = maxLevel;
    }

    public void increase() {
        int level = getBrightnessLevel();
        if (mMaxLevel != 0 && level >= mMaxLevel) {
            return;
        }
        setBrightness(++level);
    }

    public void decrease() {
        int level = getBrightnessLevel();
        if (level > 0) {
            setBrightness(--level);
        }
    }

    public void setBrightnessMode(int mode) {
        try {
            int preMode = System.getInt(mResolver, System.SCREEN_BRIGHTNESS_MODE);
            if (preMode != mode) {
                System.putInt(mResolver, System.SCREEN_BRIGHTNESS_MODE, mode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setBrightness(int level) {
        setBrightnessMode(System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        int brightness;
        if (mMaxLevel == 0) {
            brightness = level;
        } else if (mMaxLevel <= level) {
            brightness = 255;
        } else {
            brightness = 255 / mMaxLevel * level;
        }
        System.putInt(mResolver, System.SCREEN_BRIGHTNESS, brightness);
    }

    public int getBrightness() {
        try {
            return System.getInt(mResolver, System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getBrightnessLevel() {
        int brightness = getBrightness();
        return mMaxLevel == 0 ? brightness : brightness / (255 / mMaxLevel);
    }
}
