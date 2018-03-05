package com.airbnb.lottie.samples

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.OnLifecycleEvent
import com.airbnb.lottie.samples.model.AnimationResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class LottiefilesViewModel(application: Application) : AndroidViewModel(application) {

    val animationResponse = MutableLiveData<AnimationResponse?>()
    val animationResponseError = MutableLiveData<Throwable?>()
    val loading = MutableLiveData<Boolean>()
    private val disposables = CompositeDisposable()

    fun fetchAnimations() {
        loading.value = true
        disposables.add(getApplication<LottieApplication>().lottiefilesService.getRecent()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    animationResponse.value = it
                }, {
                    animationResponse.value = null
                    animationResponseError.value = it
                }, {
                    loading.value = false
                }))
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun cleanupDisposables() = disposables.dispose()
}