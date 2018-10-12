package com.tomy.lib.ui.manager

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.atomic.AtomicBoolean

/**@author Tomy
 * Created by Tomy on 2018/6/6.
 */
class FloatWinManager(private var mContext: Context, var mRootView: View, private var mWinType: Int = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY) {

    private lateinit var mWindowManager: WindowManager

    private lateinit var mParameter: WindowManager.LayoutParams

    private var mShowed: AtomicBoolean = AtomicBoolean(false)

    private var mMax = false

    private var mWidth  = 0
    private var mHeight = 0

    private var mDismissListener: OnDismissListener? = null

    init {
        initWindowManager()
    }

    private fun initWindowManager() {
        mWindowManager  = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        initParameters()
    }

    private fun initParameters() {
        mParameter      = WindowManager.LayoutParams()
        mParameter.x    = 0
        mParameter.y    = 0
        mParameter.gravity  = Gravity.CENTER
        mParameter.format = PixelFormat.RGBA_8888
        mParameter.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mParameter.type = mWinType
        } else {
            mParameter.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }
    }

    fun setSize(width: Int, height: Int) {
        mWidth  = width
        mHeight = height
    }

    fun showFloatWindow() {
        mParameter.alpha = 1f
        mParameter.flags = mParameter.flags.and(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv())
        updateView(mWidth, mHeight)
    }

    fun setOnWindowDismissListener(listener: OnDismissListener) {
        mDismissListener = listener
    }

    /**
     * @see showFloatWindow
     * */
    fun dismissWindow() {
        mParameter.alpha = 0f
        mParameter.flags = mParameter.flags.or(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        updateView(2, 2)
        mDismissListener?.onWindowDismiss()
    }

    fun removeFloatWindow() {
        Observable.just(Unit)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    mMax = false
                    if (mShowed.get()) {
                        mWindowManager.removeView(mRootView)
                        mShowed.set(false)
                    }
                }
    }

    private fun updateView(width: Int, height: Int) {
        setResolutionRation(width, height)
        Observable.just(Unit)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (!mShowed.get()) {
                        mWindowManager.addView(mRootView, mParameter)
                        mShowed.set(true)
                    } else {
                        mWindowManager.updateViewLayout(mRootView, mParameter)
                    }
                }
    }

    private fun setResolutionRation(width: Int, height: Int) {
        mParameter.width    = width
        mParameter.height   = height
    }

    private fun setKeepScreenOn(keep: Boolean) {
        if (keep) {
            mParameter.flags.apply {
                or(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        } else {
            mParameter.flags.apply {
                and(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON.inv())
            }
        }
    }

    interface OnDismissListener {
        fun onWindowDismiss()
    }

}