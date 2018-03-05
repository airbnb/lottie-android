package com.airbnb.lottie.samples

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.content.Context
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.samples.model.AnimationData
import com.airbnb.lottie.samples.model.CompositionArgs
import okhttp3.Request

sealed class CompositionResult
class Loading : CompositionResult()
class Loaded(val composition: LottieComposition) : CompositionResult()
class LoadError(val throwable: Throwable) : CompositionResult()

object CompositionCache {
    val cache = HashMap<CompositionArgs, MutableLiveData<CompositionResult>>()

    fun observe(args: CompositionArgs?, owner: LifecycleOwner, observer: Observer<CompositionResult>) {
        args ?: return
        cache.getValue(args).observe(owner, observer)
    }

    fun removeObserver(args: CompositionArgs?, observer: Observer<CompositionResult>) {
        args ?: return
        cache.getValue(args).removeObserver(observer)
    }

    fun fetch(context: Context, args: CompositionArgs) {
        if (args.animationData != null) {
            fetchByAnimationData(context, args.animationData)
        } else if (args.url != null) {
            fetchByUrl(context, args.url)
        }
    }

    private fun fetchByAnimationData(context: Context, animationData: AnimationData) {
        fetchByUrlOrAnimationData(context, CompositionArgs(animationData = animationData))
    }

    private fun fetchByUrl(context: Context, url: String) {
        fetchByUrlOrAnimationData(context, CompositionArgs(url = url))
    }

    private fun fetchByUrlOrAnimationData(context: Context, args: CompositionArgs) {
        val url = args.url ?: args.animationData?.lottieLink
        cache[args] = MutableLiveData<CompositionResult>().apply { value = Loading() }
        val request: Request
        try {
            request = Request.Builder()
                    .url(url)
                    .build()
        } catch (e: IllegalArgumentException) {
            onError(args, e)
            return
        }
        okHttpClient(context).newCall(request)?.enqueue(OkHttpCallback(
                onFailure = { _, e ->  onError(args, e) },
                onResponse = { _, response ->
                    if (!response.isSuccessful) {
                        onError(args, IllegalStateException("Response was unsuccessful."))
                    } else {
                        val string = response.body()?.string()
                        if (string == null) {
                            onError(args, IllegalStateException("Response body was null"))
                            return@OkHttpCallback
                        }
                        try {
                            LottieComposition.Factory.fromJsonString(string, {
                                if (it == null) {
                                    onError(args, IllegalArgumentException("Unable to parse composition"))
                                } else {
                                    onComplete(args, it)
                                }
                            })
                        } catch (e: RuntimeException) {
                            onError(args, e)
                        }
                    }
                }))
    }

    private fun okHttpClient(context: Context) = (context.applicationContext as LottieApplication).okHttpClient

    private fun onComplete(args: CompositionArgs, composition: LottieComposition) {
        cache.getValue(args).value = Loaded(composition)
    }

    private fun onError(args: CompositionArgs, t: Throwable) {
        cache.getValue(args).value = LoadError(t)
    }
//
//
//    private fun loadFile(context: Context, uri: Uri) {
//        val fis = try {
//            when (uri.scheme) {
//                "file" -> FileInputStream(uri.path)
//                "content" -> context.contentResolver.openInputStream(uri)
//                else -> {
//                    onLoadError()
//                    return
//                }
//            }
//        } catch (e: FileNotFoundException) {
//            onLoadError()
//            return
//        }
//
//        LottieComposition.Factory.fromInputStream(fis, { composition ->
//            if (composition == null) {
//                onLoadError()
//            } else {
//                listener.onCompositionLoaded(composition)
//            }
//        })
//    }
//
//    private fun loadAnimationData(animationData: AnimationData) {
//        loadUrl(animationData.lottieLink)
//    }
}
