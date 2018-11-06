package com.tomy.lib.ui.recycler

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**@author Tomy
 * Created by Tomy on 2018/10/22.
 */
abstract class ADialRecycler<T>(context: Context, attributeSet: AttributeSet): RecyclerView(context, attributeSet) {

    private val mIconList = ArrayList<T>()

    private var mOnItemClickListener: OnItemClickListener<T>? = null

    init {
        layoutManager = GridLayoutManager(context, 2)
        adapter = LauncherAdapter()
    }

    fun setIconList(list: List<T>) {
        mIconList.apply {
            clear()
            addAll(list)
        }
        adapter.notifyDataSetChanged()
    }

    fun getItem(position: Int): T {
        return mIconList[position]
    }

    fun setOnItemClickListener(listener: OnItemClickListener<T>) {
        mOnItemClickListener = listener
    }

    abstract fun bindToItem(position: Int, itemView: View)

    abstract fun getItemLayoutId(): Int

    interface OnItemClickListener<T> {
        fun onItemClick(position: Int, data: T)
    }

    inner class LauncherAdapter: RecyclerView.Adapter<BaseViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
            return BaseViewHolder(parent)
        }

        override fun getItemCount(): Int {
            return mIconList.size
        }

        override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
            holder.itemView.apply {
                bindToItem(position, this)
                setOnClickListener {
                    mOnItemClickListener?.onItemClick(position, mIconList[position])
                }
            }
        }

    }

    inner class BaseViewHolder(parent: ViewGroup): ViewHolder(
            LayoutInflater.from(parent.context).inflate(getItemLayoutId(), parent, false)
    )

}