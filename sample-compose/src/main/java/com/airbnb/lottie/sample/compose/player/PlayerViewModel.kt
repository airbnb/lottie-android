package com.airbnb.lottie.sample.compose.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.airbnb.lottie.sample.compose.api.LottieFilesApi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.lottiefiles.com/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val api = retrofit.create<LottieFilesApi>()
}