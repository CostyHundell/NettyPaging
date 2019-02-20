package com.costyhundell.nettypager

import androidx.paging.PageKeyedDataSource
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.schedulers.Schedulers

abstract class NettyPagerDataSource<T> : PageKeyedDataSource<Int, NettyItem>() {

    abstract var single: Single<T>
    var observable: Observable<T>? = null

    private var retryCompletable: Completable? = null
    private var compositeDisposable = CompositeDisposable()

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, NettyItem>) {
        val disposable = when {
            single != null -> {
                single!!.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        setRetryAction(Action { loadInitial(params, callback) })
                        onLoadInitialSuccess(callback, response)
                    }, { error ->
                        onLoadInitialError(error)
                    })
            }
            observable != null -> {
                observable!!
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        setRetryAction(Action { loadInitial(params, callback) })
                        onLoadInitialSuccess(callback, response)
                    }, { error ->
                        onLoadInitialError(error)
                    })
            }
            else -> null
        }

        compositeDisposable.add(disposable!!)
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, NettyItem>) {
        val disposable = when {
            single != null -> {
                single!!
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        setRetryAction(Action { loadAfter(params, callback) })
                        onLoadAfterSuccess(callback, response, params)
                    }, { error ->
                        onLoadAfterError(error)
                    })
            }
            observable != null -> {
                observable!!
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        setRetryAction(Action { loadAfter(params, callback) })
                        onLoadAfterSuccess(callback, response, params)
                    }, { error ->
                        onLoadAfterError(error)
                    })
            }
            else -> null
        }

        compositeDisposable.add(disposable!!)
    }

    fun postInitial(callback: LoadInitialCallback<Int, NettyItem>, items: List<NettyItem>, page: Int) {
        callback.onResult(items, null, page)
    }

    fun postAfter(callback: LoadCallback<Int, NettyItem>, items: List<NettyItem>, page: Int) {
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
