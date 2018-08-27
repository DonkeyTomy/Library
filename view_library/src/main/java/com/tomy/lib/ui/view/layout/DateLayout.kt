package com.tomy.lib.ui.view.layout

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.databinding.BindingAdapter
import android.databinding.DataBindingUtil
import android.databinding.ObservableInt
import android.graphics.drawable.AnimationDrawable
import android.os.Handler
import android.provider.Settings.System
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.tomy.lib.ui.R
import com.tomy.lib.ui.bean.Time
import com.tomy.lib.ui.databinding.DataContainerBinding
import java.util.*


/**@author Tomy
 * Created by Tomy on 2018/1/15.
 */
class DateLayout(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private var mDataBinding: DataContainerBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
            R.layout.data_container, this, true)
    private var mTimeReceiver = TimeReceiver()

    private var mTvWeekDay: TextView? = null
    private var mTvDate: TextView? = null
    private var mTime: Time? = null

    private var mPreHour: Int = 0

    private var mPreMin: Int = 0

    private var mPreDate: Int = 0

    private var mTimeFormat: Boolean = true

    init {
        mTvWeekDay = mDataBinding.tvWeekDay
        mTvDate = mDataBinding.tvDate
        mDataBinding.time = Time()
        mTime = mDataBinding.time
        isHour24Format()
        refreshTime()
    }

    fun refreshTime() {
        changeDate()
        changeTime()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val filter = IntentFilter(Intent.ACTION_TIME_CHANGED)
        filter.addAction(Intent.ACTION_TIME_TICK)
        context.registerReceiver(mTimeReceiver, filter)
        context.contentResolver.registerContentObserver(System.getUriFor(System.TIME_12_24), false, object : ContentObserver(Handler()) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                isHour24Format()
                changeTime()
            }
        })
        (mDataBinding.viewTime.root.findViewById<ImageView>(R.id.animation_colon).drawable as AnimationDrawable).start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        context.unregisterReceiver(mTimeReceiver)
        (mDataBinding.viewTime.root.findViewById<ImageView>(R.id.animation_colon).drawable as AnimationDrawable).stop()
    }

    private fun isHour24Format() {
        mTimeFormat = System.getString(context.contentResolver, System.TIME_12_24) == "24"
    }

    private fun changeTime() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(if (mTimeFormat) Calendar.HOUR_OF_DAY else Calendar.HOUR)
        val min = calendar.get(Calendar.MINUTE)
        if (mPreHour != hour) {
            mPreHour = hour
            setTime(mTime!!.hourLeft, hour / 10)
            setTime(mTime!!.hourRight, hour % 10)
        }
        if (mPreMin != min) {
            mPreMin = min
            setTime(mTime!!.minLeft, min / 10)
            setTime(mTime!!.minRight, min % 10)
        }
    }

    private fun setTime(timeView: ObservableInt, count: Int) {
        when (count) {
            0 -> timeView.set(R.drawable.time_0)
            1 -> timeView.set(R.drawable.time_1)
            2 -> timeView.set(R.drawable.time_2)
            3 -> timeView.set(R.drawable.time_3)
            4 -> timeView.set(R.drawable.time_4)
            5 -> timeView.set(R.drawable.time_5)
            6 -> timeView.set(R.drawable.time_6)
            7 -> timeView.set(R.drawable.time_7)
            8 -> timeView.set(R.drawable.time_8)
            9 -> timeView.set(R.drawable.time_9)
        }
    }


    @SuppressLint("SetTextI18n")
    private fun changeDate() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        if (mPreDate == year + month + dayOfMonth) {
            return
        }
        mPreDate = year + month + dayOfMonth
        when (dayOfWeek) {
            Calendar.MONDAY -> mTvWeekDay?.setText(R.string.monday)
            Calendar.TUESDAY -> mTvWeekDay?.setText(R.string.tuesday)
            Calendar.WEDNESDAY -> mTvWeekDay?.setText(R.string.wednesday)
            Calendar.THURSDAY -> mTvWeekDay?.setText(R.string.thursday)
            Calendar.FRIDAY -> mTvWeekDay?.setText(R.string.friday)
            Calendar.SATURDAY -> mTvWeekDay?.setText(R.string.saturday)
            Calendar.SUNDAY -> mTvWeekDay?.setText(R.string.sunday)
        }
        mTvDate?.text = "$year/$month/$dayOfMonth"
    }

    private inner class TimeReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            refreshTime()
        }
    }

}

@BindingAdapter("hourLeft")
fun setTimeHourLeft(timeHourLeft: ImageView, hourLeft: Int) {
    timeHourLeft.setImageResource(hourLeft)
}

@BindingAdapter("hourRight")
fun setTimeHourRight(timeHourRight: ImageView, hourRight: Int) {
    timeHourRight.setImageResource(hourRight)
}

@BindingAdapter("minLeft")
fun setTimeMinLeft(timeMinLeft: ImageView, minLeft: Int) {
    timeMinLeft.setImageResource(minLeft)
}

@BindingAdapter("minRight")
fun setTimeMinRight(timeMinRight: ImageView, minRight: Int) {
    timeMinRight.setImageResource(minRight)
}