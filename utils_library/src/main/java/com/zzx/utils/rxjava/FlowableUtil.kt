package com.zzx.utils.rxjava

import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers

/**@author Tomy
 * Created by Tomy on 2018/6/18.
 */
object FlowableUtil {

    fun <out>setBackgroundThreadMapMain(map: Function<Unit, out>, mainThreadExec: Consumer<out>) {
        Flowable.just(Unit)
                .observeOn(Schedulers.newThread())
                .map(map)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mainThreadExec)
    }

    fun <C>setMainThreadMapBackground(mainThread: Function<Unit, C>, newThreadExec: Consumer<C>) {
        Flowable.just(Unit)
                .observeOn(AndroidSchedulers.mainThread())
                .map(mainThread)
                .observeOn(Schedulers.newThread())
                .subscribe(newThreadExec)
    }

    fun <C>setBackgroundThread(map: Function<Unit, C>, onNext: Consumer<C>) {
        Flowable.just(Unit)
                .observeOn(Schedulers.newThread())
                .map(map)
                .subscribe(onNext)
    }

    fun setMainThread(mainThreadExec: Consumer<Unit>) {
        Flowable.just(Unit)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mainThreadExec)
    }

    fun setBackgroundThread(onNext: Consumer<Unit>) {
        Flowable.just(Unit)
                .observeOn(Schedulers.newThread())
                .subscribe(onNext)
    }

}