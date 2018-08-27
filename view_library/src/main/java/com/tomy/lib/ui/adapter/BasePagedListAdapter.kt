package com.tomy.lib.ui.adapter

import android.arch.paging.PagedListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**@author Tomy
 * Created by Tomy on 2018/7/30.
 */
abstract class BasePagedListAdapter<T: BaseItemData>: PagedListAdapter<T, BasePagedListAdapter<T>.BaseViewHolder>(object : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean =
            oldItem.getKey() == newItem.getKey()

    /**
     * Note that in kotlin, == checking on data classes compares all contents, but in Java,
     * typically you'll implement Object#equals, and use it to compare object contents.
     */
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean =
            oldItem == newItem
}) {

    private var mOnItemClickListener: OnItemClickListener<T>? = null

    abstract fun getLayoutId(): Int

    abstract fun bindToView(itemView: View, data: T)

    fun setOnItemClickListener(listener: OnItemClickListener<T>) {
        mOnItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(parent)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        bindToView(holder.itemView, getItem(position)!!)
        holder.itemView.setOnClickListener {
            mOnItemClickListener?.onItemClick(getItem(position)!!)
        }
    }


    inner class BaseViewHolder(parent: ViewGroup): RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(getLayoutId(), parent, false)
    )

    interface OnItemClickListener<T> {
        fun onItemClick(data: T)
    }

}