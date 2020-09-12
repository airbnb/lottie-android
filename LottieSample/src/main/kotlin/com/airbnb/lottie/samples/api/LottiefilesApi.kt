package com.airbnb.lottie.samples.api

import com.airbnb.lottie.samples.model.AnimationResponse
import com.airbnb.lottie.samples.model.AnimationResponseV2
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LottiefilesApi {
    @GET("v1/recent")
    suspend fun getRecent(@Query("page") page: Int): AnimationResponse

    @GET("v1/popular")
    suspend fun getPopular(@Query("page") page: Int): AnimationResponse

    @GET("v2/featured")
    suspend fun getCollection(): AnimationResponseV2

    @GET("v1/search/{query}")
    suspend fun search(@Path("query") query: String, @Query("page") page: Int): AnimationResponse
}