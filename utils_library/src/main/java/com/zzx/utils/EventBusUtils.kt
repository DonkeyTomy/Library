package com.zzx.utils

import org.greenrobot.eventbus.EventBus
import java.util.*


/**@author Tomy
 * Created by Tomy on 2014/6/20.
 */
class EventBusUtils
/**EventBust有四种回调方法
 * @see .onEvent
 * @see .onEventBackgroundThread
 * @see .onEventMainThread
 * @see .onEventAsync
 */
private constructor() {



    private fun onEvent() {}
    private fun onEventMainThread() {}
    private fun onEventBackgroundThread() {}
    private fun onEventAsync() {}

    companion object {
        private var mBusMap: HashMap<String, EventBus>? = HashMap()
        private var mEventBusUtils = lazy {
            EventBusUtils()
        }

        fun getInstance() :EventBusUtils{
            return mEventBusUtils.value
        }

        /**添加EventBus
         * @param event 该EventBus的事件类型
         */
        fun addEventBus(event: String) {
            if (mBusMap == null) {
                getInstance()
            }
            if (mBusMap!![event] == null) {
                mBusMap!![event] = EventBus()
            }
        }

        /**获取该类型事件的EventBus,若当前没有则即时加入
         */
        fun getEventBus(event: String): EventBus {
            addEventBus(event)
            return mBusMap!![event]!!
        }

        /**清除该类型EventBus
         */
        fun removeEventBus(event: String) {
            if (mBusMap != null)
                mBusMap!!.remove(event)
        }

        /**清除所有Bus
         */
        fun release() {
            if (mBusMap != null) {
                mBusMap!!.clear()
                mBusMap = null
            }
        }

        /**注册事件
         */
        fun registerEvent(busEvent: String, observer: Any) {
            getEventBus(busEvent).register(observer)
        }

        /**注册Sticky事件
         */
        fun registerStickyEvent(busEvent: String, event: Any) {
            //        getEventBus(busEvent).registerSticky(event)
        }

        /**注销事件
         */
        fun unregisterEvent(busEvent: String, observer: Any) {
            getEventBus(busEvent).unregister(observer)
        }

        /**注销Sticky事件
         */
        fun removeStickyEvent(busEvent: String, event: Any) {
            getEventBus(busEvent).removeStickyEvent(event)
        }

        /**发送事件
         */
        fun postEvent(busEvent: String, event: Any) {
            getEventBus(busEvent).post(event)
        }

        fun postEvent(busEvent: String, event: Int) {
            getEventBus(busEvent).post(Integer.valueOf(event))
        }

        /**发送Sticky事件
         */
        fun postStickyEvent(busEvent: String, event: Any) {
            getEventBus(busEvent).postSticky(event)
        }

        /**发送Sticky事件
         */
        fun postStickyEvent(busEvent: String, event: Int) {
            getEventBus(busEvent).postSticky(Integer.valueOf(event))
        }
    }
}
