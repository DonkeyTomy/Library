package com.tomy.lib.ui.bean

import android.databinding.ObservableInt

/**@author Tomy
 * Created by Tomy on 2018/1/16.
 */
data class Time(var hourLeft: ObservableInt, var hourRight: ObservableInt, var minLeft: ObservableInt, var minRight: ObservableInt,
                var secLeft: ObservableInt, var secRight: ObservableInt) {
    constructor(hourLeft: Int, hourRight: Int, minLeft: Int, minRight: Int, secLeft: Int, secRight: Int) : this(
            ObservableInt(hourLeft), ObservableInt(hourRight), ObservableInt(minLeft), ObservableInt(minRight), ObservableInt(secLeft), ObservableInt(secRight)
    )

    constructor() : this(
            ObservableInt(0), ObservableInt(0), ObservableInt(0), ObservableInt(0), ObservableInt(0), ObservableInt(0)
    )
}