package com.tomy.lib.ui.view.preference

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.preference.ListPreference
import android.util.AttributeSet
import android.view.*
import com.tomy.lib.ui.R
import timber.log.Timber


/**@author Tomy
 * Created by Tomy on 2018/10/9.
 */
abstract class BaseListPreference(context: Context, attrSet: AttributeSet): ListPreference(context, attrSet) {
    /*private var mEntries: Array<CharSequence>
    private var mEntryValues: Array<CharSequence>
    private var mDefaultValue: String
    private var mValue: String? = null*/

    protected var mDialog: Dialog? = null

    private var mXOffset: Int = 250

    private var mYOffset: Int = 0

    init {
        /*val array = context.obtainStyledAttributes(attrSet, R.styleable.BaseListPreference)
        mEntries    = array.getTextArray(R.styleable.CustomDialogPreference_entries)
        mEntryValues     = array.getTextArray(R.styleable.CustomDialogPreference_entryValues)
        mDefaultValue   = array.getString(R.styleable.CustomDialogPreference_defaultValue)
        array.recycle()*/
    }

    private val mInflater by lazy {
        LayoutInflater.from(context)
    }

    fun getString(stringId: Int): String {
        return context.getString(stringId)
    }

    override fun showDialog(state: Bundle?) {
        mDialog = Dialog(context, R.style.CustomDialogTheme)
        val contentView = onCreateDialogView()
        contentView.setBackgroundResource(R.drawable.bg_camera_setting_dialog)
        onBindDialogView(contentView)
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        mDialog!!.setContentView(contentView)
        // Create the mDialog
        if (state != null) {
            mDialog?.onRestoreInstanceState(state)
        }
        val dialogWindow = mDialog!!.window
        /**设置成此Type是因为WindowManager设置成了{@link android.view.WindowManager.LayoutParams#TYPE_SYSTEM_ERROR},这里必须得比它高才能显示在它上面.
         * */
        dialogWindow.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR)
        val lp = dialogWindow.attributes
        lp.apply {
            width = contentView.measuredWidth
            height = WindowManager.LayoutParams.WRAP_CONTENT
            x = mXOffset
            y = mYOffset
            gravity = Gravity.TOP.or(Gravity.START)
        }

        dialogWindow.attributes = lp
        Timber.e("lp.width = ${lp.width}")
        try {
            mDialog?.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onCreateView(parent: ViewGroup?): View {
        return super.onCreateView(parent).apply {
            viewTreeObserver.addOnGlobalLayoutListener {
                val intArray = IntArray(2)
                getLocationOnScreen(intArray)
                Timber.e("measuredWidth = $measuredWidth; x = ${intArray[0]}, y = ${intArray[1]}")
                val xOffset = intArray[0] + measuredWidth + 10
                val yOffset = intArray[1]
                Timber.e("xOffset = $xOffset; yOffset = $yOffset")
                setDialogShowCoordinate(xOffset, yOffset)
            }
        }
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        bindDialogLayout(view)
    }

    /**
     * @return Int 返回窗口的布局Id.
     */
    abstract fun getDialogLayoutId(): Int

    abstract fun bindDialogLayout(view: View)

    fun setDialogShowCoordinate(xOffset: Int, yOffset: Int) {
        mXOffset    = xOffset
        mYOffset    = yOffset
    }

    override fun onCreateDialogView(): View {
        return mInflater.inflate(getDialogLayoutId(), null)
    }

}