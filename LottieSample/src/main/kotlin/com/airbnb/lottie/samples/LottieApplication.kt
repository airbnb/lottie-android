package com.airbnb.lottie.samples

import android.support.multidex.MultiDexApplication
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LottieApplication : MultiDexApplication() {
    val okHttpClient by lazy { OkHttpClient.Builder().build() }

    val gson by lazy {
        GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()
    }

    val retrofit by lazy {
        Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl("http://lottiefiles.com/api/v1/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
    }

    val lottiefilesService by lazy { retrofit.create(LottiefilesService::class.java) }
}