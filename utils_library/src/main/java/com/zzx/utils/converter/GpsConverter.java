package com.zzx.utils.converter;

import java.io.File;
import java.io.FileWriter;

/**@author Tomy
 * Created by Tomy on 2014/7/8.
 */
public class GpsConverter {

    public static double convertToDegree(double gpsData) {
        try {
            String temp = String.valueOf(gpsData);
            String[] temp1 = temp.split("\\.");
            String limit = temp1[0];
            double fraction = Double.parseDouble("0." + temp1[1]);
            double fractionDes = fraction * 60;
            String temp2 = String.valueOf(fractionDes);
            String[] temp3 = temp2.split("\\.");
            /*double secTmp = Double.parseDouble(temp3[1].substring(0, temp3[1].length() >= 5 ? 5:temp3[1].length()));
            double sec = secTmp * 60;*/
            String min = temp3[0];
            if (min.length() == 1) {
                min = "0" + min;
            }
            String dest = limit + min + "." + temp3[1].substring(0, temp3[1].length() >= 4 ? 4:temp3[1].length());
            return Double.parseDouble(dest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0000.0000;
    }

    public static String getGpsString(double data) {
        String temp = String.valueOf(data);
        try {
            String[] temp1 = temp.split("\\.");
            String limit = temp1[0];//获得度数
            double fraction = Double.parseDouble("0." + temp1[1]);//将分加个"0."转换为double
            double fractionDes = fraction * 60;//分*60
            String temp2 = String.valueOf(fractionDes);//转换为字符串
            String[] temp3 = temp2.split("\\.");//分出分秒,temp3[0]即分数
            double secTmp = Double.parseDouble(temp3[1].substring(0, temp3[1].length() >= 4 ? 4:temp3[1].length()));//秒数取4位
            String sec = String.valueOf(secTmp * 60);//秒数*60
            if (sec.contains("."))//若*60后变成了*.*E7等,则去除.
                sec = sec.replace(".", "");
            return limit + "°" + temp3[0] + "′" + sec.substring(0, sec.length() >= 4 ? 4:sec.length()) + "″";
        } catch (Exception e) {
            e.printStackTrace();
        }
        FileWriter writer = null;
        try {
            File failedGps = new File("/storage/sdcard0/failedGps.txt");
            if (!failedGps.exists())
                failedGps.createNewFile();
            writer = new FileWriter(failedGps);
            writer.append(temp + "\n");
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "0";
    }
}