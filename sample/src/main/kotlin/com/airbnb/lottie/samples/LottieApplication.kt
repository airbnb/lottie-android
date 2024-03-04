package com.airbnb.lottie.samples

import androidx.multidex.MultiDexApplication
import com.airbnb.lottie.L
import com.airbnb.mvrx.Mavericks
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class LottieApplication : MultiDexApplication() {
    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val gson by lazy {
        GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://api.lottiefiles.com/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        Mavericks.initialize(this)
        L.DBG = true
    }
}
