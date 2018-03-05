package com.airbnb.lottie.samples

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.samples.model.CompositionArgs
import okhttp3.CacheControl
import okhttp3.Request
import java.util.concurrent.TimeUnit

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    val composition = MutableLiveData<LottieComposition>()
    val error = MutableLiveData<Throwable>()

    fun fetchAnimation(args: CompositionArgs) {
        val url = args.url ?: "https://www.lottiefiles.com/download/${args.animationData?.id}"
        fetchAnimationByUrl(url)
    }

    private fun fetchAnimationByUrl(url: String) {
        val request: Request
        try {
            request = Request.Builder()
                    .cacheControl(CacheControl.Builder()
                            .maxAge(Int.MAX_VALUE, TimeUnit.DAYS)
                            .build())
                    .url(url)
                    .build()
        } catch (e: IllegalArgumentException) {
            error.value = e
            return
        }
        getApplication<LottieApplication>().okHttpClient
                .newCall(request)
                ?.enqueue(OkHttpCallback(
                        onFailure = { _, e -> error.value = e },
                        onResponse = { _, response ->
                            if (!response.isSuccessful) {
                                error.value = IllegalStateException("Response was unsuccessful.")
                            } else {
                                val string = response.body()?.string()
                                if (string == null) {
                                    error.value = IllegalStateException("Response body was null")
                                    return@OkHttpCallback
                                }
                                try {
                                    LottieComposition.Factory.fromJsonString(string, {
                                        if (it == null) {
                                            error.value = IllegalArgumentException("Unable to parse composition")
                                        } else {
                                            composition.value = it
                                        }
                                    })
                                } catch (e: RuntimeException) {
                                    error.value = e
                                }
                            }
                        }))
    }
}