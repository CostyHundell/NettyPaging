package com.costyhundell.nettypager

import androidx.paging.PageKeyedDataSource
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.schedulers.Schedulers

abstract class SimpleNettyPagerDataSource<T>(private val numberOfApis: Int) : PageKeyedDataSource<Int, NettyItem>() {

    abstract var single: Single<T>
    var observable: Observable<T>? = null

    private var retryCompletable: Completable? = null
    private var nextCompletable: Completable? = null
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
        callback.onResult(items, null, page)
    }

    fun postAfter(callback: LoadCallback<Int, NettyItem>, items: List<NettyItem>, page: Int) {
        callsMade = 0
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

    fun setNextInitialCall(
        placeHoldersEnabled: Boolean,
        requestedLoadSize: Int,
        call: Single<T>,
        callback: LoadInitialCallback<Int, NettyItem>
    ) {
        val newInitialParams = LoadInitialParams<Int>(requestedLoadSize, placeHoldersEnabled)
        single = call
        nextCompletable = Completable.fromAction { loadInitial(newInitialParams, callback) }

        fun runNext() {
            compositeDisposable.add(
                nextCompletable!!
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe()
            )
        }
    }

    fun setNextAfterCall(requestedLoadSize: Int, page: Int, call: Single<T>, callback: LoadCallback<Int, NettyItem>) {
        val newParams = LoadParams<Int>(page, requestedLoadSize)
        single = call
        nextCompletable = Completable.fromAction { loadAfter(newParams, callback) }

        fun runNext() {
            compositeDisposable.add(
                nextCompletable!!
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe()
            )
        }
    }


    fun clear() {
        compositeDisposable.clear()
    }

    private fun setRetryAction(action: Action?) {
        retryCompletable = if (action == null) null else Completable.fromAction(action)
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, NettyItem>) {
    }

    abstract fun onLoadInitialSuccess(callback: PageKeyedDataSource.LoadInitialCallback<Int, NettyItem>, response: T)
    abstract fun onLoadAfterSuccess(callback: LoadCallback<Int, NettyItem>, response: T, params: LoadParams<Int>)
    abstract fun onLoadInitialError(error: Throwable)
    abstract fun onLoadAfterError(error: Throwable)

}
