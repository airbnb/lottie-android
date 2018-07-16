package com.airbnb.lottie.samples

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.OnLifecycleEvent
import android.util.Log
import com.airbnb.lottie.L
import com.airbnb.lottie.samples.model.AnimationData
import com.airbnb.lottie.samples.model.AnimationResponse
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class LottiefilesViewModel(application: Application) : AndroidViewModel(application) {

    val animationDataList = MutableLiveData<List<AnimationData?>>()
    val loading = MutableLiveData<Boolean>()
    val mode = MutableLiveData<LottiefilesMode>().apply { value = LottiefilesMode.Recent }
    private var disposables = CompositeDisposable()
    private val responses = ArrayList<AnimationResponse>()

    private var searchQuery: String? = null

    fun mode() = mode.value ?: throw IllegalStateException("Mode must be set")

    fun setMode(mode: LottiefilesMode, searchQuery: String? = null) {
        this.searchQuery = searchQuery
        disposables.dispose()
        disposables = CompositeDisposable()
        this.mode.value = mode
        animationDataList.value = null
        loading.value = false
        responses.clear()
        fetchMoreAnimations()
    }

    fun fetchMoreAnimations() {
        if (loading.value == true) return

        val page = (responses.lastOrNull()?.currentPage ?: -1) + 1
        if (!responses.isEmpty() && page > responses.last().lastPage) return

        val service = getApplication<LottieApplication>().lottiefilesService
        val observable = when (mode()) {
            LottiefilesMode.Recent -> service.getRecent(page)
            LottiefilesMode.Popular -> service.getPopular(page)
            LottiefilesMode.Search ->
                if (searchQuery == null) Observable.empty() else service.search(searchQuery ?: "")
        }

        disposables.add(observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(3)
                .doOnSubscribe { loading.value = true }
                .subscribe({
                    responses.add(it)
                    animationDataList.value = flatten(animationDataList.value, it.data)
                }, {
                    Log.d(L.TAG, "e#\t", it);
                }, {
                    loading.value = false
                }))
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun cleanupDisposables() = disposables.dispose()
}