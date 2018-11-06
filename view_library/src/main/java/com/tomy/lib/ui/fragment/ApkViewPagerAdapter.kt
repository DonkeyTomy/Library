package com.tomy.lib.ui.fragment

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.support.v4.view.PagerAdapter
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.tomy.lib.ui.R
import timber.log.Timber
import java.util.*

/**@author Tomy
 * Created by Tomy on 2015-01-06.
 */
@Suppress("UNCHECKED_CAST")
class ApkViewPagerAdapter(private val mContext: Context) : PagerAdapter(), AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    /**
     * 每页Apk最多显示个数
     */
    private val COUNT_PAGE = 8

    /**
     * 每行显示个数.
     */
    private val COUNT_GRID_VIEW_COLUMNS = 2

    private val mInfoList by lazy {
        ArrayList<Int>()
    }
    private val mIconList by lazy {
        ArrayList<Int>()
    }
    private var mListener: AdapterView.OnItemClickListener? = null
    private val mNameList by lazy {
        ArrayList<ArrayList<String>>()
    }
    private val mDrawableList by lazy {
        ArrayList<ArrayList<Drawable>>()
    }
    private val nameList by lazy {
        ArrayList<String>()
    }
    private val iconList by lazy {
        ArrayList<Drawable>()
    }
    private var mItemLongClickListener: AdapterView.OnItemLongClickListener? = null
    private val mInflater: LayoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


    fun setOnItemClickListener(listener: AdapterView.OnItemClickListener) {
        mListener = listener
    }

    fun setOnItemLongClickListener(listener: AdapterView.OnItemLongClickListener) {
        mItemLongClickListener = listener
    }

    fun addIconList(iconListId: Int) {
        mIconList.add(iconListId)
    }

    fun addInfoList(infoListId: Int) {
        mInfoList.add(infoListId)
    }

    fun addName(name: String) {
        nameList.add(name)
        Timber.e("nameList.size = ${nameList.size}")
        if (nameList.size == COUNT_PAGE) {
            mNameList.add(nameList.clone() as ArrayList<String>)
            nameList.clear()
        }
    }

    fun addIcon(icon: Drawable) {
        iconList.add(icon)
        if (iconList.size == COUNT_PAGE) {
            mDrawableList.add(iconList.clone() as ArrayList<Drawable>)
            iconList.clear()
        }
    }

    fun addFinish() {
        if (iconList.isNotEmpty()) {
            mDrawableList.add(iconList.clone() as ArrayList<Drawable>)
            mNameList.add(nameList.clone() as ArrayList<String>)
            nameList.clear()
            iconList.clear()
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        Timber.e("instantiateItem.position = $position")
        val gridView = mInflater.inflate(R.layout.grid_view, container, false) as GridView
        gridView.apply {
            selector = ColorDrawable(android.R.color.transparent)
            numColumns = COUNT_GRID_VIEW_COLUMNS
            val adapter = GridViewAdapter(mNameList[position], mDrawableList[position])
            this.adapter = adapter
            gravity = Gravity.CENTER_HORIZONTAL
            onItemClickListener = this@ApkViewPagerAdapter
            horizontalSpacing = 1
            verticalSpacing = 20
            onItemLongClickListener = this@ApkViewPagerAdapter
        }

        container.addView(gridView)
        return gridView
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        mListener?.onItemClick(parent, view, position, id)
    }

    override fun onItemLongClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long): Boolean {
        mItemLongClickListener?.onItemLongClick(parent, view, position, id)
        return true
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun getCount(): Int {
        Timber.e("getCount = ${mNameList.size}")
        return mNameList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    fun removeItem(index: Int, current: Int) {
        mDrawableList[index].removeAt(current)
        mNameList[index].removeAt(current)
    }

    inner class GridViewAdapter : BaseAdapter {
        private var mInfoArray: Array<String>?  = null
        private var mIconArray: TypedArray?     = null
        private var mInfoList: List<String>?    = null
        private var mIconList: List<Drawable>?  = null

        constructor(infoId: Int, iconId: Int) {
            val res = mContext.resources
            mInfoArray = res.getStringArray(infoId)
            mIconArray = res.obtainTypedArray(iconId)
        }

        constructor(infoList: List<String>, iconList: List<Drawable>) {
            mInfoList = infoList
            mIconList = iconList
            Timber.e("mInfoList.size = ${mIconList?.size}")
        }

        internal inner class ViewHolder {
            var mIvIcon: ImageView? = null
            var mTvInfo: TextView? = null
        }

        override fun getCount(): Int {
            Timber.e("mInfoList.size = ${mInfoList?.size}")
            return mInfoList?.size ?: mInfoArray?.size ?: 0
        }

        override fun getItem(position: Int): String {
            return mInfoList!![position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, view: View?, parent: ViewGroup): View {
            var convertView = view
            val holder: ViewHolder
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.apk_grid_item_container, parent, false)
                holder = ViewHolder()
                holder.mIvIcon = convertView!!.findViewById(R.id.icon)
                holder.mTvInfo = convertView.findViewById(R.id.info)
                convertView.tag = holder
            } else {
                holder = convertView.tag as ViewHolder
            }
            if (mIconArray != null) {
                holder.mTvInfo!!.text = mInfoArray!![position]
                holder.mIvIcon!!.setImageDrawable(mIconArray!!.getDrawable(position))
            } else {
                holder.mTvInfo!!.text = mInfoList!![position]
                holder.mIvIcon!!.setImageDrawable(mIconList!![position])
            }
            return convertView
        }
    }
}
