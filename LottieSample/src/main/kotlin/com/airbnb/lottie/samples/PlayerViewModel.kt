package com.airbnb.lottie.samples

import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieTask
import com.airbnb.lottie.samples.model.CompositionArgs
import okhttp3.ResponseBody
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.zip.ZipInputStream

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val handler = Handler(Looper.getMainLooper())

    val composition = MutableLiveData<LottieComposition>()
    val error = MutableLiveData<Throwable>()

    fun fetchAnimation(args: CompositionArgs) {
        val url = args.url ?: args.animationData?.lottieLink

        val task = when {
            url != null -> LottieCompositionFactory.fromUrl(getApplication(), url)
            args.fileUri != null -> taskForUri(args.fileUri)
            args.asset != null -> LottieCompositionFactory.fromAsset(getApplication(), args.asset)
            else -> throw IllegalArgumentException("Don't know how to fetch animation for $args")
        }
        registerTask(task)
    }

    private fun handleJsonResponse(jsonString: String, cacheKey: String) {
        val task = LottieCompositionFactory.fromJsonString(jsonString, cacheKey)
        registerTask(task)
    }

    @SuppressLint("CheckResult")
    private fun handleZipResponse(body: ResponseBody, cacheKey: String) {
        val task = LottieCompositionFactory.fromZipStream(ZipInputStream(body.byteStream()), cacheKey)
        registerTask(task)
    }

    private fun taskForUri(uri: Uri): LottieTask<LottieComposition> {
        val fis = try {
            when (uri.scheme) {
                "file" -> FileInputStream(uri.path)
                "content" -> getApplication<LottieApplication>().contentResolver.openInputStream(uri)
                else -> return LottieTask() { throw IllegalArgumentException("Unknown scheme ${uri.scheme}") }
            }
        } catch (e: FileNotFoundException) {
            return LottieTask() { throw e }
        }

        return LottieCompositionFactory.fromJsonInputStream(fis, uri.toString())
    }

    private fun registerTask(task: LottieTask<LottieComposition>) {
        task
                .addListener {
                    composition.value = it
                }
                .addFailureListener {
                    error.value = it
                }
    }
}