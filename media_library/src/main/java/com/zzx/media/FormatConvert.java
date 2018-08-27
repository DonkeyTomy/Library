package com.zzx.media;

/**@author Tomy
 * Created by Tomy on 2017/7/6.
 */

public class FormatConvert {

    public FormatConvert(int width, int height, int typeFrom, int typeTo) {
        init(width, height, typeFrom, typeTo);
    }

    /**
     * @param width
     * @param height
     * @param typeFrom
     * @param typeTo
     * @return
     */
    private native int init(int width, int height, int typeFrom, int typeTo);

    public native void release();

    public native byte[] convert(byte[] data);
}
