package com.tomy.lib.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.tomy.lib.ui.R
import timber.log.Timber

/**@author Tomy
 * Created by Tomy on 2018/12/2.
 */
class CustomSeekBar(context: Context, attrs: AttributeSet): LinearLayout(context, attrs) {

    private val MAX_COUNT = 5

    /**
     * SeekBar的总进度
     */
    private var mMaxProgress = 0

    /**
     * 每个子图之间的间隔
     */
    private var mItemSpace   = 0

    /**
     * 当前进度多少
     */
    private var mProgress = 0

    private var mProgressChangedListener: OnProgressChangedListener? = null

    private val mIconId: Int

    init {
        context.obtainStyledAttributes(attrs, R.styleable.CustomSeekBar).apply {
            mIconId = getResourceId(R.styleable.CustomSeekBar_seekBarItem, 0)
            mMaxProgress    = getInt(R.styleable.CustomSeekBar_seekBarMax, MAX_COUNT)
            mItemSpace       = getInt(R.styleable.CustomSeekBar_seekBarSpace, 0)
            mProgress    = getInt(R.styleable.CustomSeekBar_seekBarProgress, 0)
            recycle()
        }
        Timber.e("mProgress = $mProgress")
        val hor = orientation == HORIZONTAL
        val space = mItemSpace / 2
        for (i in 0 until mMaxProgress) {
            val icon = ImageView(context).apply {
                setImageResource(mIconId)
                isSelected   = i < mProgress
                setOnClickListener {
                    setSeekBarProgress(i + 1)
                }
            }
            addView(icon, i, LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                if (hor) {
                    marginStart = space
                    marginEnd   = space
                } else {
                    topMargin   = space
                    bottomMargin= space
                }
            })
        }
    }

    fun setSeekBarProgressMax(max: Int) {
        mMaxProgress = max
    }

    fun getSeekBarProgress(): Int {
        return mProgress
    }

    fun getSeekBarMaxProgress(): Int {
        return mMaxProgress
    }

    fun setSeekBarProgress(progress: Int, needNotify: Boolean = true) {
        Timber.e("progress = $progress")
        mProgress = when {
            progress < 0 -> 0
            progress > mMaxProgress -> mMaxProgress
            else -> progress
        }
        for (i in 0 until mMaxProgress) {
            getChildAt(i).isSelected = i < mProgress
        }
        if (needNotify) {
            mProgressChangedListener?.onProgressChanged(mProgress)
        }
    }

    fun increase() {
        setSeekBarProgress(++mProgress)
    }

    fun decrease() {
        setSeekBarProgress(--mProgress)
    }

    fun setOnProgressChangedListener(listener: OnProgressChangedListener?) {
        mProgressChangedListener = listener
    }

    interface OnProgressChangedListener {
        fun onProgressChanged(progress: Int)
    }

}