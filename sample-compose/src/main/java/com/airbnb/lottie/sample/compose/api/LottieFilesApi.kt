package com.airbnb.lottie.sample.compose.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LottieFilesApi {
    @GET("v2/featured")
    suspend fun getFeatured(): AnimationsResponseV2

    @GET("v1/recent")
    suspend fun getRecent(@Query("page") page: Int): AnimationsResponseV1

    @GET("v1/popular")
    suspend fun getPopular(@Query("page") page: Int): AnimationsResponseV1

    @GET("v1/search/{query}")
    suspend fun search(@Path("query") query: String, @Query("page") page: Int): AnimationsResponseV1
}