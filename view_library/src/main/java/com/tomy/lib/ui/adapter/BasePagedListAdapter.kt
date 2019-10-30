package com.tomy.lib.ui.adapter

import android.annotation.SuppressLint
import android.arch.paging.PagedListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import timber.log.Timber

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
    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean =
            oldItem == newItem
}) {

    private var mOnItemClickListener: OnItemClickListener<T>? = null
    private var mOnItemLongClickListener: OnItemLongClickListener<T>? = null

    abstract fun getLayoutId(): Int

    abstract fun bindToView(itemView: View, data: T)

    fun setOnItemClickListener(listener: OnItemClickListener<T>) {
        mOnItemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener<T>) {
        mOnItemLongClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(parent)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
//        Timber.e("onBindViewHolder.position = $position")
        val item = getItem(position)
        bindToView(holder.itemView, item!!)
        holder.itemView.setOnClickListener {
            mOnItemClickListener?.onItemClick(item)
        }

        holder.itemView.setOnLongClickListener {
            return@setOnLongClickListener mOnItemLongClickListener?.onItemLongClickListener(item) ?: false
        }
    }


    inner class BaseViewHolder(parent: ViewGroup): RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(getLayoutId(), parent, false)
    )

    interface OnItemClickListener<T> {
        fun onItemClick(data: T)
    }

    interface OnItemLongClickListener<T> {
        fun onItemLongClickListener(data: T): Boolean
    }

}