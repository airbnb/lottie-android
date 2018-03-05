package com.airbnb.lottie.samples

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.OnLifecycleEvent
import android.util.Log
import com.airbnb.lottie.samples.model.AnimationData
import com.airbnb.lottie.samples.model.AnimationResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class LottiefilesViewModel(application: Application) : AndroidViewModel(application) {

    enum class Mode {
        Popular,
        Recent
    }

    val animationDataList = MutableLiveData<List<AnimationData?>>()
    val loading = MutableLiveData<Boolean>()
    val mode = MutableLiveData<Mode>().apply { value = Mode.Recent }
    private var disposables = CompositeDisposable()
    private val responses = ArrayList<AnimationResponse>()

    fun mode() = mode.value ?: throw IllegalStateException("Mode must be set")

    fun switchMode() {
        disposables.dispose()
        disposables = CompositeDisposable()
        mode.value = if (mode() == Mode.Popular) Mode.Recent else Mode.Popular
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
        val observable =
                if (mode() == Mode.Recent) service.getRecent(page)
                else service.getPopular(page)

        disposables.add(observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { loading.value = true }
                .subscribe({
                    responses.add(it)
                    animationDataList.value = flatten(animationDataList.value, it.data)
                }, {
                    Log.d("Gabe", "e#\t", it);
                }, {
                    loading.value = false
                }))
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun cleanupDisposables() = disposables.dispose()
}