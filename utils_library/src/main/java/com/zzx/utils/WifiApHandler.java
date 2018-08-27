package com.zzx.utils;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.Method;

public class WifiApHandler {
	private static final String TAG = "WifiApHandler: ";

	public static final String AP_INFO = "ap_info";
	public static final String AP_SSID = "ap_ssid";
	public static final String AP_PASS = "ap_pass";
	public static final String AP_SSID_DEFAULT = "T3-WIFI-AP";
	public static final String AP_PASS_DEFAULT = "12345678";

	public static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";
	public static final String EXTRA_WIFI_AP_STATE = "wifi_state";

	public static int WIFI_AP_UPDATE_CONFIG = 0x1001;
	public static int WIFI_AP_UPDATE_ERROR = 0x1002;
	public static int WIFI_AP_UPDATE_CHECKBOX = 0x1003;
	public static final int WIFI_AP_DISABLED = -1;
	public static final int WIFI_AP_ENABLED = 0;
	private WifiManager mWifiManager = null;

	private WifiConfiguration mWifiConfig = null;
	private Method mMethodSetWifiAp = null;
	public static final String METHOD_SET_WIFI_AP_ENABLE = "setWifiApEnabled";
	public static final String METHOD_IS_WIFI_AP_ENABLED = "isWifiApEnabled";

	public static class WifiApState {
		public static final int WIFI_AP_STATE_DISABLING = 10;
		public static final int WIFI_AP_STATE_DISABLED = 11;
		public static final int WIFI_AP_STATE_ENABLED = 13;
		public static final int WIFI_AP_STATE_ENABLING = 12;
		public static final int WIFI_AP_STATE_FAILED = 14;

	}

	public WifiApHandler(Context mContext) {
		mWifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		setConfigDefault();
		initMethod();
	}

	public void setWifiApConfiguration(WifiConfiguration configuration) {
		mWifiConfig = configuration;
	}

	public void setConfigDefault() {
		mWifiConfig = new WifiConfiguration();

		mWifiConfig.allowedAuthAlgorithms
				.set(WifiConfiguration.AuthAlgorithm.OPEN);
		mWifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
		mWifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
		mWifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
		mWifiConfig.allowedPairwiseCiphers
				.set(WifiConfiguration.PairwiseCipher.CCMP);
		mWifiConfig.allowedPairwiseCiphers
				.set(WifiConfiguration.PairwiseCipher.TKIP);
		mWifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
		mWifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
	}

	public void closeWifiAp() {
		try {
			if (isWifiApEnabled()) {
				mMethodSetWifiAp.invoke(mWifiManager, mWifiConfig, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*
		 * if (!mWifiManager.isWifiEnabled()) {
		 * mWifiManager.setWifiEnabled(true); }
		 */
	}

	public void setSSID(String ssid) {
		mWifiConfig.SSID = ssid;
	}

	public void setPassword(String password) {
		mWifiConfig.preSharedKey = password;
	}

	public void changeWifiApState(boolean isOpened) {
		if (isOpened) {
			closeWifiAp();
		} else {
			openWifiAp();
		}
	}

	public void initMethod() {
		try {
			if (mMethodSetWifiAp == null) {
				mMethodSetWifiAp = mWifiManager.getClass().getMethod(
						METHOD_SET_WIFI_AP_ENABLE, WifiConfiguration.class,
						Boolean.TYPE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void openWifiAp() {
		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
		}
		try {
			Log.d(TAG, "registerReceiver()+++" + mWifiConfig.SSID + "; "
					+ mWifiConfig.preSharedKey);
			if (!isWifiApEnabled()) {
				mMethodSetWifiAp.invoke(mWifiManager, mWifiConfig, true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isWifiApEnabled() {
		try {
			Method method = mWifiManager.getClass().getMethod(
					METHOD_IS_WIFI_AP_ENABLED);
			method.setAccessible(true);
			return (Boolean) method.invoke(mWifiManager);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
}
