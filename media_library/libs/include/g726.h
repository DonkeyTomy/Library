#ifndef __G726_CODEC_H_
#define __G726_CODEC_H_

#ifdef __cplusplus
extern "C" 
{
#include "libavcodec/avcodec.h"
}
#endif

typedef unsigned char BYTE;
typedef int (*G726_CALLBACK)(unsigned char* dat, int len, void* context);

typedef struct _G726_CONTEXT
{
	AVCodec *codec;
	AVCodecContext *c;
} G726_CONTEXT;

class CG726
{ 
public: 
    bool Open(bool bEncode, int sample_rate, int channels, int bits_per_sample, int bits_per_coded_sample);
    bool Decode(unsigned char * inbuf, const int inlen,unsigned char * outbuf, int* outlen);
	bool Encode(unsigned char * inbuf, const int inlen,unsigned char * outbuf, int* outlen);
    void Close();
	void SetOnFrameCallback(G726_CALLBACK onData, void* context) { m_context = context; m_onData = onData; }

public: 
    CG726(void);
    virtual ~CG726(void); 

private:
    G726_CONTEXT* m_hCodec;
    G726_CALLBACK m_onData;
	BYTE*		  m_buffer;
	int			  m_buflen;
	void*		  m_context;
	AVFrame*	  m_avfrm;
};

#endif //__G726_CODEC_H_