package com.zzx.media

/**@author Tomy
 * Created by Tomy on 2017/7/6.
 */

class FormatConvert(width: Int, height: Int, typeFrom: Int, typeTo: Int, bitPerFrom: Int, bitPerTo: Int) {

    init {
        init(width, height, typeFrom, typeTo, bitPerFrom, bitPerTo)
    }

    /**
     * @param width
     * @param height
     * @param typeFrom
     * @param typeTo
     * @return
     */
    private external fun init(width: Int, height: Int, typeFrom: Int, typeTo: Int, bitPerFrom: Int, bitPerTo: Int): Int

    external fun releaseConverter()

    external fun convert(data: ByteArray, out: ByteArray)
}
