package com.tomy.lib.ui.recycler

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.tomy.lib.ui.R

/**@author Tomy
 * Created by Tomy on 2018/8/1.
 */
class DialRecycler(context: Context, attributeSet: AttributeSet): RecyclerView(context, attributeSet) {
    init {
        layoutManager = GridLayoutManager(context, 3)
        adapter = GridAdapter()
    }

    private var mOnItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(value: String)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mOnItemClickListener = listener
    }

    inner class GridAdapter: RecyclerView.Adapter<DialViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialViewHolder {
            return DialViewHolder(parent)
        }

        override fun getItemCount() = buttonArray.size

        override fun onBindViewHolder(holder: DialViewHolder, position: Int) {
            val buttonChar = buttonArray[position]
            holder.bindToButton(buttonChar)
            holder.itemView.findViewById<Button>(R.id.btn_dial).setOnClickListener {
                mOnItemClickListener?.onItemClick(buttonChar)
            }
        }

    }

    inner class DialViewHolder(parent: ViewGroup): ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.dial_item, parent, false)
    ) {
        fun bindToButton(value: String) {
            itemView.findViewById<TextView>(R.id.btn_dial).text = value
        }
    }

    companion object {
        val buttonArray = arrayOf(
                "1", "2", "3",
                "4", "5", "6",
                "7", "8", "9",
                "*", "0", "#")
    }
}