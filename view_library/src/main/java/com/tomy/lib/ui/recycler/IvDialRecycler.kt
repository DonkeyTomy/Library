package com.tomy.lib.ui.recycler

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import com.tomy.lib.ui.R

/**@author Tomy
 * Created by Tomy on 2018/8/3.
 */
class IvDialRecycler(context: Context, attributeSet: AttributeSet): RecyclerView(context, attributeSet) {
    init {
        layoutManager = GridLayoutManager(context, 3)
        adapter = GridAdapter()
    }

    private var mOnItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(value: Char)
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
            holder.itemView.findViewById<ImageView>(R.id.btn_dial).setOnClickListener {
                mOnItemClickListener?.onItemClick(valueArray[position])
            }
        }

    }

    inner class DialViewHolder(parent: ViewGroup): ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.iv_dial_item, parent, false)
    ) {
        fun bindToButton(srcId: Int) {
            itemView.findViewById<ImageView>(R.id.btn_dial).setImageResource(srcId)
        }
    }

    companion object {
        val buttonArray = arrayOf(
                R.drawable.btn_dial_pad_1, R.drawable.btn_dial_pad_2, R.drawable.btn_dial_pad_3,
                R.drawable.btn_dial_pad_4, R.drawable.btn_dial_pad_5, R.drawable.btn_dial_pad_6,
                R.drawable.btn_dial_pad_7, R.drawable.btn_dial_pad_8, R.drawable.btn_dial_pad_9,
                R.drawable.btn_dial_pad_star, R.drawable.btn_dial_pad_0, R.drawable.btn_dial_pad_pound)

        val valueArray = arrayOf(
                '1', '2', '3',
                '4', '5', '6',
                '7', '8', '9',
                '*', '0', '#')
    }
}