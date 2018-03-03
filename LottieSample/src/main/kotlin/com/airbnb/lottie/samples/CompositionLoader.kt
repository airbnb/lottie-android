package com.airbnb.lottie.samples

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Parcelable
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.OnCompositionLoadedListener
import kotlinx.android.parcel.Parcelize
import okhttp3.Request
import java.io.FileInputStream
import java.io.FileNotFoundException

@SuppressLint("ParcelCreator")
@Parcelize
class CompositionArgs(
        val assetName: String? = null,
        val url: String? = null,
        val fileUri: Uri? = null
) : Parcelable
class CompositionLoader(
        private val context: Context,
        args: CompositionArgs,
        private val listener: OnCompositionLoadedListener
) {
    private val okHttpClient by lazy { (context.applicationContext as LottieApplication).okHttpClient }

    init {
        if (args.assetName != null) {
            LottieComposition.Factory.fromAssetFileName(context, args.assetName, listener)
        } else if (args.url != null) {
            loadUrl(args.url)
        } else if (args.fileUri != null) {
            loadFile(args.fileUri)
        }
    }

    private fun loadUrl(url: String) {
        val request: Request
        try {
            request = Request.Builder()
                    .url(url)
                    .build()
        } catch (e: IllegalArgumentException) {
            onLoadError()
            return
        }
        okHttpClient.newCall(request)?.enqueue(OkHttpCallback(
                onFailure = { _, _ -> onLoadError() },
                onResponse = { _, response ->
                    if (!response.isSuccessful) {
                        onLoadError()
                    } else {
                        val string = response.body()?.string()
                        if (string == null) {
                            onLoadError()
                            return@OkHttpCallback
                        }
                        try {
                            LottieComposition.Factory.fromJsonString(string, listener)
                        } catch (e: RuntimeException) {
                            onLoadError()
                        }
                    }
                }))
    }

    private fun loadFile(uri: Uri) {
        val fis = try {
            when (uri.scheme) {
                "file" -> FileInputStream(uri.path)
                "content" -> context.contentResolver.openInputStream(uri)
                else -> {
                    onLoadError()
                    return
                }
            }
        } catch (e: FileNotFoundException) {
            onLoadError()
            return
        }

        LottieComposition.Factory.fromInputStream(fis, { composition ->
            if (composition == null) {
                onLoadError()
            } else {
                listener.onCompositionLoaded(composition)
            }
        })
    }

    private fun onLoadError() = listener.onCompositionLoaded(null)
}