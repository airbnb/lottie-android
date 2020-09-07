package com.airbnb.lottie.sample.compose.api

import retrofit2.http.GET

interface LottieFilesApi {
    @GET("v2/featured")
    suspend fun getFeatured(): FeaturedAnimationsResponse
}