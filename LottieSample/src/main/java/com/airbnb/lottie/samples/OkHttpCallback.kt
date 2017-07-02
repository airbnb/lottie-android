package com.airbnb.lottie.samples

import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

internal open class OkHttpCallback(
        val onResponse: ((call: Call, response: Response) -> Unit)? = null,
        val onFailure: ((call: Call, exception: IOException) -> Unit)? = null
): Callback {
    override fun onResponse(call: Call, response: Response) = onResponse?.invoke(call, response) ?: Unit
    override fun onFailure(call: Call, response: IOException) = onFailure?.invoke(call, response) ?: Unit

}