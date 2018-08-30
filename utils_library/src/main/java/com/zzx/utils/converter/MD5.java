package com.zzx.utils.converter;


import com.zzx.utils.Values;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

public class MD5 {

	private static final char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    private static final String TAG = "MD5: ";

    public static String toHextString(byte[] b) {
		StringBuilder sb = new StringBuilder(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
			sb.append(HEX_DIGITS[b[i] & 0x0f]);
//            Values.LOGI(TAG, "" + HEX_DIGITS[(bufferTmp[i] & 0xf0) >>> 4] + HEX_DIGITS[bufferTmp[i] & 0x0f]);
        }
        Values.LOG_I(TAG, "MD5 = " + sb);
		return sb.toString();
	}

	private static String getString(byte[] b){
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < b.length; i ++) {
			sb.append(b[i]);
		}
		return sb.toString();
	}

	public static String md5SumMsg(String info) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			byte[] buffer = info.getBytes();
			md5.update(buffer, 0, buffer.length);
			byte[] buffer1 = md5.digest();
			return toHextString(buffer1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String md5Sum(String fileName){
		InputStream is = null;
		byte[] buffer = new byte[1024 * 10];
		int numRead = 0;
		MessageDigest md5;
		
		try {
			is = new FileInputStream(fileName);
			md5 = MessageDigest.getInstance("MD5") ;
			while((numRead = is.read(buffer)) > 0){
				md5.update(buffer, 0, numRead);
			}
			return toHextString(md5.digest());
		} catch (Exception e) {
			e.printStackTrace();
			return null ;
		} finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
