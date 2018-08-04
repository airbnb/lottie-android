package com.airbnb.lottie.samples

import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.samples.R.id.url
import com.airbnb.lottie.samples.model.CompositionArgs
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.CacheControl
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val handler = Handler(Looper.getMainLooper())

    val composition = MutableLiveData<LottieComposition>()
    val error = MutableLiveData<Throwable>()

    fun fetchAnimation(args: CompositionArgs) {
        val url = args.url ?: args.animationData?.lottieLink
        if (url != null) {
            fetchAnimationByUrl(url)
        } else if (args.fileUri != null) {
            fetchAnimationByFileUri(args.fileUri)
        } else if (args.asset != null) {
            fetchAnimationByAsset(args.asset)
        }
    }

    private fun fetchAnimationByUrl(url: String) {
        val request = try {
            Request.Builder()
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
                        onFailure = { _, e -> onFailure(e) },
                        onResponse = { _, response -> onResponse(url, response) }
                ))
    }

    private fun onFailure(e: Exception) {
        handler.post { error.value = e }
    }

    fun onResponse(url: String, response: Response) {
        handler.post {
            if (!response.isSuccessful) {
                onFailure(IllegalStateException("Response was unsuccessful."))
            } else {
                if (response.body()?.contentType() == MediaType.parse("application/zip")) {
                    handleZipResponse(response.body()!!, url)
                } else {
                    val string = response.body()?.string()
                    if (string == null) {
                        onFailure(IllegalStateException("Response body was null"))
                        return@post
                    }
                    handleJsonResponse(string, url)
                }
            }
        }
    }

    private fun handleJsonResponse(jsonString: String, cacheKey: String) {
        LottieCompositionFactory.fromJsonString(jsonString, cacheKey)
                .addListener {
                    this.composition.value = it
                }
                .addFailureListener {
                    this.error.value = it
                }
    }

    @SuppressLint("CheckResult")
    private fun handleZipResponse(body: ResponseBody, cacheKey: String) {
        LottieCompositionFactory.fromZipStream(ZipInputStream(body.byteStream()), cacheKey)
                .addListener {
                    composition.value = it
                }
                .addFailureListener {
                    error.value = it
                }
    }

    private fun fetchAnimationByFileUri(uri: Uri) {
        val fis = try {
            when (uri.scheme) {
                "file" -> FileInputStream(uri.path)
                "content" -> getApplication<LottieApplication>().contentResolver.openInputStream(uri)
                else -> {
                    error.value = IllegalArgumentException("Unknown scheme ${uri.scheme}")
                    return
                }
            }
        } catch (e: FileNotFoundException) {
            error.value = e
            return
        }

        LottieCompositionFactory.fromJsonInputStream(fis, uri.toString())
                .addListener {
                    this.composition.value = it
                }
                .addFailureListener {
                    this.error.value = it
                }
    }

    private fun fetchAnimationByAsset(asset: String) {
        LottieCompositionFactory.fromAsset(getApplication(), asset)
                .addListener {
                    composition.value = it
                }
                .addFailureListener {
                    error.value = it
                }
    }
}