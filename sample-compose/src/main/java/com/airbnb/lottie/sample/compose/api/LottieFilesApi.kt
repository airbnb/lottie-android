package com.airbnb.lottie.sample.compose.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LottieFilesApi {
    @GET("v2/featured")
    suspend fun getFeatured(): AnimationResponseV2
}