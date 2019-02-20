package com.costyhundell.nettypager

import androidx.annotation.RequiresApi
import androidx.paging.PageKeyedDataSource
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

@RequiresApi(24)
abstract class MultiNettyPagerDataSource<U>(val multiCalls: List<Single<U>>): PageKeyedDataSource<Int, NettyItem>() {

    private var callMap: MutableMap<Int, U> = emptyMap<Int, U>().toMutableMap()

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, NettyItem>) {
        multiCalls.forEachIndexed { index, single ->
            single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    callMap[index] = response
                    if (index == multiCalls.size -1) {
                        onLoadInitialSuccess(callback, callMap)
                    }
                }, { error ->
//                    onLoadInitialError(error)
                })
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, NettyItem>) {
        multiCalls.forEachIndexed { index, single ->
            single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    callMap[index] = response
                    if (index == multiCalls.size -1) {
                        onLoadAfterSuccess(callback, callMap, params)
                    }
                }, { error ->
                    //                    onLoadInitialError(error)
                })
        }
    }

    fun postInitial(callback: LoadInitialCallback<Int, NettyItem>, items: List<NettyItem>, page: Int) {
        callback.onResult(items, null, page)
    }

    fun postAfter(callback: LoadCallback<Int, NettyItem>, items: List<NettyItem>, page: Int) {
        callback.onResult(items, page)
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, NettyItem>) {}

    abstract fun onLoadInitialSuccess(callback: LoadInitialCallback<Int, NettyItem>, results: Map<Int, U>)
    abstract fun onLoadAfterSuccess(callback: LoadCallback<Int, NettyItem>, results: Map<Int, U>, params: LoadParams<Int>)
}