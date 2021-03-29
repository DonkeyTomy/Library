//
// Created by donke on 2017/7/5.
//

#include "FormatConverter.h"

FormatConverter::FormatConverter(int width, int height, int typeFrom, int typeTo) :
mWidth(width),
mHeight(height),
mSwsContext(NULL) {
    mSwsContext = sws_getContext(width, height, AV_PIX_FMT_RGBA,
                                 width, height, AV_PIX_FMT_YUV420P,
                                 SWS_BICUBIC, NULL, NULL, NULL);
    avpicture_alloc((AVPicture*)(&mAvFrame), AV_PIX_FMT_YUV420P, width, height);

}

unsigned char* FormatConverter::formatConvert(const unsigned char *dataSrc) {
    const uint8_t *rgbSrc[3] = {NULL, NULL, NULL};
    const int rgbStride[3]    = {3 * mWidth, 0, 0};
    sws_scale(mSwsContext, rgbSrc, rgbStride, 0, mHeight, mAvFrame.data, mAvFrame.linesize);
    return (unsigned char *) mAvFrame.data;
}

FormatConverter::~FormatConverter() {
    sws_freeContext(mSwsContext);
    mSwsContext = NULL;
}