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
import com.airbnb.lottie.samples.model.CompositionArgs
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.CacheControl
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.ResponseBody
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream

class CompositionData(var composition: LottieComposition? = null) {
    val images = HashMap<String, Bitmap>()
}

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val handler = Handler(Looper.getMainLooper())

    val composition = MutableLiveData<CompositionData>()
    val error = MutableLiveData<Throwable>()

    fun fetchAnimation(args: CompositionArgs) {
        val url = args.url ?: args.animationData?.lottieLink
        if (url != null) {
            fetchAnimationByUrl(url)
        } else if (args.fileUri != null) {
            fetchAnimationByFileUri(args.fileUri)
        }
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
                        onFailure = { _, e -> onFailure(e) },
                        onResponse = { _, response ->
                            if (!response.isSuccessful) {
                                onFailure(IllegalStateException("Response was unsuccessful."))
                            } else {
                                if (response.body()?.contentType() == MediaType.parse("application/zip")) {
                                    handleZipResponse(response.body()!!)
                                } else {
                                    val string = response.body()?.string()
                                    if (string == null) {
                                        onFailure(IllegalStateException("Response body was null"))
                                        return@OkHttpCallback
                                    }
                                    handleJsonResponse(string)
                                }
                            }
                        }
                ))
    }

    private fun onFailure(e: Exception) {
        handler.post { error.value = e }
    }

    private fun handleJsonResponse(jsonString: String) {
        try {
            LottieComposition.Factory.fromJsonString(jsonString, {
                if (it == null) {
                    error.value = IllegalArgumentException("Unable to parse composition")
                } else {
                    composition.value = CompositionData(it)
                }
            })
        } catch (e: RuntimeException) {
            error.value = e
        }
    }

    @SuppressLint("CheckResult")
    private fun handleZipResponse(body: ResponseBody) {
        Observable.just(body.byteStream())
                .map {
                    val compositionData = CompositionData()
                    val zis: ZipInputStream
                    try {
                        zis = ZipInputStream(body.byteStream())

                        var zipEntry = zis.nextEntry
                        while (zipEntry != null) {
                            if (zipEntry.name.contains("__MACOSX")) {
                                zis.closeEntry()
                            } else if (zipEntry.name.contains(".json")) {
                                val composition = LottieComposition.Factory.fromInputStreamSync(zis, false)
                                if (composition == null) {
                                    throw IllegalArgumentException("Unable to parse composition")
                                } else {
                                    compositionData.composition = composition
                                }
                            } else if (zipEntry.name.contains(".png")) {
                                val name = zipEntry.name.split("/").last()
                                compositionData.images[name] = BitmapFactory.decodeStream(zis)
                            } else {
                                zis.closeEntry()
                            }
                            zipEntry = zis.nextEntry
                        }

                        zis.close()
                        compositionData
                    } catch (e: IOException) {
                        throw e
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    composition.value = it
                }, {
                    error.value = it
                })

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

        LottieComposition.Factory.fromInputStream(fis, {
            if (it == null) {
                error.value = IllegalArgumentException("Er")
            } else {
                composition.value = CompositionData(it)
            }
        })
    }
}