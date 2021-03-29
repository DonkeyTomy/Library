package com.tomy.lib.ui.view.layout

import android.content.Context
import android.databinding.BindingAdapter
import android.databinding.DataBindingUtil
import android.databinding.ObservableInt
import android.graphics.drawable.AnimationDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import com.tomy.lib.ui.R
import com.tomy.lib.ui.bean.Time
import com.tomy.lib.ui.databinding.ViewTimeBinding
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

/**@author Tomy
 * Created by Tomy on 2018/11/3.
 */
class TimeLayout(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private var mDataBinding: ViewTimeBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
            R.layout.view_time, this, true)

    private var mTime: Time? = null

    private var mTimeCount = 0L

    private val mCalendar by lazy {
        Calendar.getInstance(TimeZone.getTimeZone("GMT+0"), Locale.getDefault())
    }

    private val mObservable by lazy {
        Observable.interval(1, 1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map {
                    mTimeCount += 1000
                    refreshTime()
                }
    }

    private var mDisposable: Disposable? = null

    init {
        mDataBinding.time = Time()
        mTime = mDataBinding.time
        refreshTime()
    }


    fun start() {
        if (mDisposable == null) {
            mDisposable = mObservable.subscribe()
        }
        (mDataBinding.animationColon.drawable as AnimationDrawable).start()
        (mDataBinding.animationColon1.drawable as AnimationDrawable).start()
    }

    fun pause() {
        (mDataBinding.animationColon.drawable as AnimationDrawable).stop()
        (mDataBinding.animationColon1.drawable as AnimationDrawable).stop()
        mDisposable?.dispose()
        mDisposable = null
    }

    fun resume() {
        start()
    }

    fun setTime(time: Long) {
        mTimeCount = time
        refreshTime()
    }

    fun stop() {
        pause()
        mTime?.apply {
            hourLeft.set(0)
            hourRight.set(0)
            minLeft.set(0)
            minRight.set(0)
            secLeft.set(0)
            secRight.set(0)
        }
        mTimeCount = 0
    }

    private fun refreshTime() {
        mCalendar.time = Date(mTimeCount)
        val hour= mCalendar.get(Calendar.HOUR_OF_DAY)
        val min = mCalendar.get(Calendar.MINUTE)
        val sec = mCalendar.get(Calendar.SECOND)
//        Timber.e("timeCount = $mTimeCount; hour = $hour; min = $min; sec = $sec; time = ${mCalendar.time}")
        mTime?.apply {
            hourLeft.set(hour / 10)
            hourRight.set(hour % 10)
            minLeft.set(min / 10)
            minRight.set(min % 10)
            secLeft.set(sec / 10)
            secRight.set(sec % 10)
        }
    }

    companion object {
        @BindingAdapter("hourLeft") @JvmStatic
        fun setTimeHourLeft(time: ImageView, hourLeft: Int) {
            when (hourLeft) {
                0 -> time.setImageResource(R.drawable.time_0)
                1 -> time.setImageResource(R.drawable.time_1)
                2 -> time.setImageResource(R.drawable.time_2)
                3 -> time.setImageResource(R.drawable.time_3)
                4 -> time.setImageResource(R.drawable.time_4)
                5 -> time.setImageResource(R.drawable.time_5)
                6 -> time.setImageResource(R.drawable.time_6)
                7 -> time.setImageResource(R.drawable.time_7)
                8 -> time.setImageResource(R.drawable.time_8)
                9 -> time.setImageResource(R.drawable.time_9)
            }
        }

        @BindingAdapter("hourRight") @JvmStatic
        fun setTimeHourRight(time: ImageView, hourRight: Int) {
            when (hourRight) {
                0 -> time.setImageResource(R.drawable.time_0)
                1 -> time.setImageResource(R.drawable.time_1)
                2 -> time.setImageResource(R.drawable.time_2)
                3 -> time.setImageResource(R.drawable.time_3)
                4 -> time.setImageResource(R.drawable.time_4)
                5 -> time.setImageResource(R.drawable.time_5)
                6 -> time.setImageResource(R.drawable.time_6)
                7 -> time.setImageResource(R.drawable.time_7)
                8 -> time.setImageResource(R.drawable.time_8)
                9 -> time.setImageResource(R.drawable.time_9)
            }
        }

        @BindingAdapter("minLeft") @JvmStatic
        fun setTimeMinLeft(time: ImageView, minLeft: Int) {
            when (minLeft) {
                0 -> time.setImageResource(R.drawable.time_0)
                1 -> time.setImageResource(R.drawable.time_1)
                2 -> time.setImageResource(R.drawable.time_2)
                3 -> time.setImageResource(R.drawable.time_3)
                4 -> time.setImageResource(R.drawable.time_4)
                5 -> time.setImageResource(R.drawable.time_5)
                6 -> time.setImageResource(R.drawable.time_6)
                7 -> time.setImageResource(R.drawable.time_7)
                8 -> time.setImageResource(R.drawable.time_8)
                9 -> time.setImageResource(R.drawable.time_9)
            }
        }

        @BindingAdapter("minRight") @JvmStatic
        fun setTimeMinRight(time: ImageView, minRight: Int) {
            when (minRight) {
                0 -> time.setImageResource(R.drawable.time_0)
                1 -> time.setImageResource(R.drawable.time_1)
                2 -> time.setImageResource(R.drawable.time_2)
                3 -> time.setImageResource(R.drawable.time_3)
                4 -> time.setImageResource(R.drawable.time_4)
                5 -> time.setImageResource(R.drawable.time_5)
                6 -> time.setImageResource(R.drawable.time_6)
                7 -> time.setImageResource(R.drawable.time_7)
                8 -> time.setImageResource(R.drawable.time_8)
                9 -> time.setImageResource(R.drawable.time_9)
            }
        }

        @BindingAdapter("secLeft") @JvmStatic
        fun setTimeSecLeft(time: ImageView, secLeft: Int) {
            when (secLeft) {
                0 -> time.setImageResource(R.drawable.time_0)
                1 -> time.setImageResource(R.drawable.time_1)
                2 -> time.setImageResource(R.drawable.time_2)
                3 -> time.setImageResource(R.drawable.time_3)
                4 -> time.setImageResource(R.drawable.time_4)
                5 -> time.setImageResource(R.drawable.time_5)
                6 -> time.setImageResource(R.drawable.time_6)
                7 -> time.setImageResource(R.drawable.time_7)
                8 -> time.setImageResource(R.drawable.time_8)
                9 -> time.setImageResource(R.drawable.time_9)
            }
        }

        @BindingAdapter("secRight") @JvmStatic
        fun setTimeSecRight(time: ImageView, secRight: Int) {
            when (secRight) {
                0 -> time.setImageResource(R.drawable.time_0)
                1 -> time.setImageResource(R.drawable.time_1)
                2 -> time.setImageResource(R.drawable.time_2)
                3 -> time.setImageResource(R.drawable.time_3)
                4 -> time.setImageResource(R.drawable.time_4)
                5 -> time.setImageResource(R.drawable.time_5)
                6 -> time.setImageResource(R.drawable.time_6)
                7 -> time.setImageResource(R.drawable.time_7)
                8 -> time.setImageResource(R.drawable.time_8)
                9 -> time.setImageResource(R.drawable.time_9)
            }
        }
    }



}

