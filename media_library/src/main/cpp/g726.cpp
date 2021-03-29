#include <g726.h>

#if(0)
void ff_log_callback(void*avcl, int level, const char*fmt, va_list vl)  
{  
    char log[1024];  
    vsnprintf_s(log,sizeof(log),fmt,vl);  
    OutputDebugStringA(log);  
} 
#else
//void ff_log_callback(void*avcl, int level, const char*fmt, va_list vl);
bool _ffLoaded;
#endif

CG726::CG726(void) :
m_hCodec(NULL),
m_context(NULL),
m_onData(NULL),
m_avfrm(NULL)
{
#if(0)
	static bool _ffLoaded = false;
#else
	if(!_ffLoaded )
	{
		avcodec_register_all();
//		av_log_set_callback(ff_log_callback);
		_ffLoaded = true;
    }
#endif
} 
 
CG726::~CG726(void)
{
    if(m_hCodec != NULL)
    {
        Close();
    }
}

bool CG726::Open(bool bEncode, int sample_rate, int channels, int bits_per_sample, int bits_per_coded_sample)
{
    if(m_hCodec != NULL)
    {
        return false;
    }

	G726_CONTEXT* avContext = (G726_CONTEXT*)malloc(sizeof(G726_CONTEXT));
	memset(avContext, 0, sizeof(G726_CONTEXT));

	if(bEncode)
	{
		avContext->codec = avcodec_find_encoder(AV_CODEC_ID_ADPCM_G726);
	}
	else
	{
		avContext->codec = avcodec_find_decoder(AV_CODEC_ID_ADPCM_G726);
	}
	if (!avContext->codec)
	{
		return false;
	}

	avContext->c = avcodec_alloc_context3(avContext->codec);
	if (!avContext->c)
	{
		goto L_FAILED;
	}

	avContext->c->codec_type = AVMEDIA_TYPE_AUDIO;
	avContext->c->sample_rate = sample_rate;
	avContext->c->channels = channels;
	avContext->c->bits_per_raw_sample = bits_per_sample;
	avContext->c->bits_per_coded_sample = bits_per_coded_sample;
	if(bEncode)
	{
		avContext->c->bit_rate = sample_rate * bits_per_coded_sample;
		avContext->c->sample_fmt = AV_SAMPLE_FMT_S16;
	}
	if (avcodec_open2(avContext->c, avContext->codec, NULL) < 0)
	{
		goto L_FAILED;
	}

	m_buflen = sample_rate*channels*bits_per_sample*2/8;
	m_buffer = (BYTE*)malloc(m_buflen);
	if(m_buffer == NULL)
	{
		goto L_FAILED;
	}

	m_avfrm = av_frame_alloc();

	m_hCodec =  avContext;

    return true;

L_FAILED:
	if(avContext != NULL)
	{
		free(avContext);
	}
	if(m_buffer != NULL)
	{
		free(m_buffer);
		m_buffer = NULL;
	}

	return false;
}

bool CG726::Decode(unsigned char * inbuf, const int inlen,unsigned char * outbuf, int* outlen)
{ 
    if(m_hCodec == NULL)
    {
        return false;
    }

	G726_CONTEXT* avContext = m_hCodec;

	int len;

	int got_packet = 0;
	int nOutLen = 0;
	AVPacket avpkt;

	av_init_packet(&avpkt);
	avpkt.data = inbuf;
	avpkt.size = inlen;

	while (avpkt.size > 0)
	{
        av_frame_unref(m_avfrm);
		len = avcodec_decode_audio4(avContext->c, m_avfrm, &got_packet, &avpkt);
        if (len<0)
        {
            break;
        }
        if(got_packet > 0)
        {
            int data_size = av_samples_get_buffer_size(NULL, avContext->c->channels,
							m_avfrm->nb_samples,
                            avContext->c->sample_fmt, 1);
			if(m_onData != NULL)
			{
				m_onData(m_avfrm->data[0], data_size, m_context);
			}
			if(outbuf != NULL)
			{
//				memcpy(outbuf + nOutLen, m_avfrm->data[0], data_size);
				memcpy(outbuf + nOutLen, m_avfrm->data[0], (size_t) m_avfrm->linesize[0]);
			}
			nOutLen += data_size;
        }
        avpkt.size -= len;
        avpkt.data += len;
	}

	if(outlen != NULL) *outlen = nOutLen;

	return true;
} 

bool CG726::Encode(unsigned char * inbuf, const int inlen,unsigned char * outbuf, int* outlen)
{ 
    if(m_hCodec == NULL)
    {
        return false;
    }

	G726_CONTEXT* avContext = m_hCodec;

	int len, nLeft = inlen;
	int got_packet = 0;
	int nOutLen = 0;
	if(outlen != NULL) *outlen = 0;

	AVPacket avpkt;
	BYTE*	 pdata = inbuf;

	av_init_packet(&avpkt);
	avpkt.data = m_buffer;
	avpkt.size = m_buflen;

	while (nLeft > 0)
	{
		av_frame_unref(m_avfrm);
		m_avfrm->nb_samples = nLeft/(avContext->c->channels*av_get_bytes_per_sample(avContext->c->sample_fmt));

		if(avcodec_fill_audio_frame(m_avfrm, avContext->c->channels, avContext->c->sample_fmt, pdata, nLeft, 1) < 0)
		{
			return false;
		}

 		len = avcodec_encode_audio2(avContext->c, &avpkt, m_avfrm, &got_packet);
        if (len<0)
        {
            break;
        }
        if(got_packet > 0)
        {
			if(m_onData != NULL)
			{
				m_onData(m_buffer, avpkt.size, m_context);
			}
			if(outbuf != NULL)
			{
				memcpy(outbuf + nOutLen, m_buffer, (size_t) avpkt.size);
			}
			nOutLen += avpkt.size;
        }
        nLeft = 0;
	}

	if(outlen != NULL) *outlen = nOutLen;

	return true;
} 

void CG726::Close()
{ 
    if(m_hCodec != NULL)
    {
		G726_CONTEXT* avContext = m_hCodec;

		if(m_avfrm != NULL)
		{
			av_frame_free(&m_avfrm);
			m_avfrm = NULL;
		}

		avcodec_close(avContext->c);
		av_free(avContext->c);
		free(avContext);
		m_hCodec = NULL;
	}

	if(m_buffer != NULL)
	{
		free(m_buffer);
		m_buffer = NULL;
	}
}
