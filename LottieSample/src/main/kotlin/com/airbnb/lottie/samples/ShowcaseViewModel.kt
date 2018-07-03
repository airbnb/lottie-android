package com.airbnb.lottie.samples

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.airbnb.lottie.samples.model.AnimationResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

private const val COLLECTION = "lottie-showcase"
private val TAG = ShowcaseViewModel::class.java.simpleName
class ShowcaseViewModel(application: Application) : AndroidViewModel(application) {

    private val lottiefilesService by lazy { (application as LottieApplication).lottiefilesService }

    private var disposables = CompositeDisposable()

    val collection = MutableLiveData<AnimationResponse>()
    val loading = MutableLiveData<Boolean>().apply { value = false }

    fun fetchAnimations() {
        disposables.add(lottiefilesService.getCollection(COLLECTION)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(3)
                .doOnSubscribe { loading.value = true }
                .subscribe({
                    collection.value = it
                }, {
                    Log.e(TAG, "Error loading collection", it)
                }, {
                    loading.value = false
                }))
    }
}