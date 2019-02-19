package com.costyhundell.nettypager

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

abstract class NettyPagedListAdapter<U : RecyclerView.ViewHolder>(DIFF_CALLBACK: DiffUtil.ItemCallback<NettyItem>): PagedListAdapter<NettyItem, U>(DIFF_CALLBACK) {
    var item: NettyItem? = null
    abstract override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): U

    override fun onBindViewHolder(holder: U, position: Int) {
        item = getItem(position)
    }

    override fun getItemViewType(position: Int): Int = getItem(position)!!.getItemViewType()
}