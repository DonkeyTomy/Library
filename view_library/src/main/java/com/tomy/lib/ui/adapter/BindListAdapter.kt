package com.tomy.lib.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import butterknife.ButterKnife

/**@author Tomy
 * Created by Tomy on 2018/10/11.
 */
abstract class BindListAdapter<E, T: BindListAdapter.BaseHolder<E>>: BaseAdapter() {

    private val mDataList by lazy {
        ArrayList<E>()
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: T
        val item = getItem(position)
        val view: View
        if (convertView == null) {
            view    = LayoutInflater.from(parent.context).inflate(getLayoutId(), parent, false)
            holder  = getHolder()
            holder.setConvertView(view)
            view.tag = holder
        } else {
            view = convertView
            holder = convertView.tag as T
        }
        holder.bindToItem(position, item)
//        bindToHolder(holder, position, item)
        return view
    }

    fun setData(data: List<E>) {
        mDataList.clear()
        addData(data)
    }

    fun addData(data: List<E>) {
        mDataList.addAll(data)
        notifyDataSetChanged()
    }

    fun clear() {
        mDataList.clear()
        notifyDataSetChanged()
    }

    fun addItem(data: E) {
        mDataList.add(data)
        notifyDataSetChanged()
    }

    fun addItem(position: Int, data: E) {
        mDataList.add(position, data)
        notifyDataSetChanged()
    }

    fun removeItem(data: E) {
        mDataList.remove(data)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        mDataList.removeAt(position)
        notifyDataSetChanged()
    }

    fun setItem(position: Int, data: E) {
        mDataList[position] = data
        notifyDataSetChanged()
    }

    fun setItem(oldData: E, newData: E) {
        setItem(mDataList.indexOf(oldData), newData)
    }

    abstract fun getLayoutId(): Int

    abstract fun getHolder(): T

//    abstract fun bindToHolder(holder: T, position: Int, item: E)

    override fun getItem(position: Int): E {
        return mDataList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return mDataList.size
    }

    abstract class BaseHolder<E> {
        private var convertView: View? = null

        fun setConvertView(view: View) {
            convertView = view
            ButterKnife.bind(this, convertView!!)
        }

        fun getConvertView() = convertView

        abstract fun bindToItem(position: Int, item: E)
    }
}