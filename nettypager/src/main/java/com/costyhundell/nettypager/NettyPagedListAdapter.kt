package com.costyhundell.nettypager

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

abstract class NettyPagedListAdapter<U : RecyclerView.ViewHolder>(DIFF_CALLBACK: DiffUtil.ItemCallback<NettyItem>): PagedListAdapter<NettyItem, U>(DIFF_CALLBACK) {
    abstract override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): U

    abstract override fun onBindViewHolder(holder: U, position: Int)

    override fun getItemViewType(position: Int): Int = getItem(position)!!.getItemViewType()
}