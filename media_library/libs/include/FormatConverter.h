//
// Created by donke on 2017/7/5.
//

#ifndef LIBRARY_FORMAT_CONVERT_H
#define LIBRARY_FORMAT_CONVERT_H
#ifdef __cplusplus
extern "C"
{
#include "libavcodec/avcodec.h"
#include "libswscale/swscale.h"
}
#endif

class FormatConverter
{
public:
    unsigned char * formatConvert(const unsigned char * dataSrc);
private:
    int mWidth;
    int mHeight;
    SwsContext * mSwsContext;
    AVFrame mAvFrame;
public:
    FormatConverter(int width, int height, int typeFrom, int typeTo);
    virtual ~FormatConverter(void);
};
#endif //LIBRARY_FORMAT_CONVERT_H
