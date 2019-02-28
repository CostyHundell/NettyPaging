package com.costyhundell.nettypager

import androidx.paging.PageKeyedDataSource

abstract class NettyInitialCallback: PageKeyedDataSource.LoadInitialCallback<Int, NettyItem>() {
    override fun onResult(data: MutableList<NettyItem>, previousPageKey: Int?, nextPageKey: Int?) {
    }

    override fun onResult(
        data: MutableList<NettyItem>,
        position: Int,
        totalCount: Int,
        previousPageKey: Int?,
        nextPageKey: Int?
    ) {

    }
}