package com.costyhundell.nettypager

import androidx.annotation.RequiresApi
import androidx.paging.PageKeyedDataSource
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

@RequiresApi(24)
abstract class MultiNettyPagerDataSource<U>: PageKeyedDataSource<Int, NettyItem>() {

    abstract var single: Single<U>
    var observable: Observable<U>? = null

    private var retryCompletable: Completable? = null
    private var compositeDisposable = CompositeDisposable()

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, NettyItem>) {

            single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess {
                        response ->
//                        onLoadInitialSuccess(callback)

                }
                .doOnError {
                        error ->
                    //                    onLoadInitialError(error)
                }
                .subscribe()

    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, NettyItem>) {

            single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess {
                        response ->
//                    onLoadAfterSuccess(callback)

                }
                .doOnError {
                        error ->
                    //                    onLoadInitialError(error)
                }.subscribe()
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