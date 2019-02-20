package com.costyhundell.nettypager

import androidx.paging.PageKeyedDataSource
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

abstract class MultiNettyPagerDataSource: PageKeyedDataSource<Int, NettyItem>() {

    abstract var multiCalls: List<Single<NettyResponse>>

    private var callMap: MutableMap<Int, NettyResponse> = emptyMap<Int, NettyResponse>().toMutableMap()

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

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, NettyItem>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    abstract fun onLoadInitialSuccess(callback: LoadInitialCallback<Int, NettyItem>, results: Map<Int, NettyResponse>)
    abstract fun onLoadAfterSuccess(callback: LoadCallback<Int, NettyItem>, results: Map<Int, NettyResponse>, params: LoadParams<Int>)
}