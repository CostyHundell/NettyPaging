package com.costyhundell.nettypager

import androidx.paging.PageKeyedDataSource
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.schedulers.Schedulers

abstract class NettyPagerDataSource(val numberOfApis: Int) : PageKeyedDataSource<Int, NettyItem>() {

    abstract var single: Single<NettyResponse>
    var oldSingle: Single<NettyResponse>? = null
    var observable: Observable<NettyResponse>? = null

    private var retryCompletable: Completable? = null
    private var compositeDisposable = CompositeDisposable()
    var callsMade = 0

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, NettyItem>) {
        val disposable = when {
            observable != null -> {
                observable!!
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        callsMade++
                        setRetryAction(Action { loadInitial(params, callback) })
                        onLoadInitialSuccess(callback, response)
                    }, { error ->
                        onLoadInitialError(error)
                    })
            }
            else -> {
                single.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        callsMade++
                        setRetryAction(Action { loadInitial(params, callback) })
                        onLoadInitialSuccess(callback, response)
                    }, { error ->
                        onLoadInitialError(error)
                    })
            }

        }

        compositeDisposable.add(disposable!!)
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, NettyItem>) {
        val disposable = when {
            observable != null -> {
                observable!!
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        callsMade++
                        setRetryAction(Action { loadAfter(params, callback) })
                        onLoadAfterSuccess(callback, response, params)
                    }, { error ->
                        onLoadAfterError(error)
                    })
            }
            else -> {
                single
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        callsMade++
                        setRetryAction(Action { loadAfter(params, callback) })
                        onLoadAfterSuccess(callback, response, params)
                    }, { error ->
                        onLoadAfterError(error)
                    })
            }
        }

        compositeDisposable.add(disposable!!)
    }

    fun postInitial(callback: LoadInitialCallback<Int, NettyItem>, items: List<NettyItem>, page: Int) {
        callsMade = 0
        if (oldSingle != null) single = oldSingle!!
        callback.onResult(items, null, page)
    }

    fun postAfter(callback: LoadCallback<Int, NettyItem>, items: List<NettyItem>, page: Int) {
        callsMade = 0
        if (oldSingle != null) single = oldSingle!!
        callback.onResult(items, page)
    }

    fun retry() {
        if (retryCompletable != null) {
            compositeDisposable.add(
                retryCompletable!!
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe()
            )
        }
    }

    fun setNextInitialCall(placeHoldersEnabled: Boolean, requestedLoadSize: Int, call: Single<NettyResponse>, callback: LoadInitialCallback<Int, NettyItem>): Completable {
        val newInitialParams = LoadInitialParams<Int>(requestedLoadSize, placeHoldersEnabled)
        if (callsMade == 1) oldSingle = single
        single = call
        return Completable.fromAction { loadInitial(newInitialParams, callback) }

    }

    fun setNextAfterCall(page: Int, requestedLoadSize: Int, call: Single<NettyResponse>, callback: LoadCallback<Int, NettyItem>): Completable {
        val newParams = LoadParams(page, requestedLoadSize)
        if (callsMade == 1) oldSingle = single
        single = call
        return Completable.fromAction { loadAfter(newParams, callback) }

    }



    fun clear() {
        compositeDisposable.clear()
    }

    private fun setRetryAction(action: Action?) {
        retryCompletable = if (action == null) null else Completable.fromAction(action)
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, NettyItem>) {}

    abstract fun onLoadInitialSuccess(callback: PageKeyedDataSource.LoadInitialCallback<Int, NettyItem>, response: NettyResponse)
    abstract fun onLoadAfterSuccess(callback: LoadCallback<Int, NettyItem>, response: NettyResponse, params: LoadParams<Int>)
    abstract fun onLoadInitialError(error: Throwable)
    abstract fun onLoadAfterError(error: Throwable)
    protected open fun manageApis(): Single<NettyResponse>? { return null }

    fun Completable.runNext() {
        compositeDisposable.add(
            this
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
        )
    }
}
