package com.zzx.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import com.zzx.utils.rxjava.FlowableUtil
import io.reactivex.functions.Consumer
import java.util.Locale

/**@author Tomy
 * Created by Tomy on 2014/6/13.
 */
object TTSToast {
    private val TAG = "TTSToast"
    private var mToast: Toast? = null
    private var mTTS: TextToSpeech? = null

    private var mContext: Context? = null

    @JvmStatic
    fun init(context: Context) {
        mContext = context
        if (mTTS == null) {
            mTTS = TextToSpeech(context, TextToSpeech.OnInitListener { status ->
                if (status != TextToSpeech.SUCCESS) {
                    return@OnInitListener
                }
                val result = mTTS!!.setLanguage(context.resources.configuration.locales[0])
                if (result != TextToSpeech.LANG_NOT_SUPPORTED && result != TextToSpeech.LANG_MISSING_DATA) {
                    mTTS!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String) {

                        }

                        override fun onDone(utteranceId: String) {

                        }

                        override fun onError(utteranceId: String) {
                        }

                        override fun onError(utteranceId: String?, errorCode: Int) {
                            onError(utteranceId!!)
                            mTTS = null
                        }

                    })
                }
            })
        }
        mToast = Toast.makeText(context, "", Toast.LENGTH_SHORT)
    }

    @JvmStatic
    fun release() {
        mTTS?.stop()
        mTTS?.shutdown()
        mTTS = null
        mContext = null
        mToast = null
    }

    @JvmStatic
    @JvmOverloads
    fun showToast(msg: String, needTTS: Boolean = false, show_time: Int = Toast.LENGTH_SHORT, show: Boolean = true) {
        if (show && show_time >= 0) {
            FlowableUtil.setMainThread(
                    Consumer {
                        mToast!!.setText(msg)
                        mToast!!.duration = show_time
                        mToast!!.show()
                    }
            )
        }
        try {
            if (needTTS) {
                mTTS?.speak(msg, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    @JvmOverloads
    fun showToast(msgId: Int, needTTS: Boolean = false, show_time: Int = Toast.LENGTH_SHORT, show: Boolean = true) {
        showToast(mContext!!.getString(msgId), needTTS, show_time, show)
    }

}
