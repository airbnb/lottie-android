package com.airbnb.lottie.samples

import com.airbnb.lottie.samples.model.AnimationResponse
import com.airbnb.lottie.samples.model.AnimationResponseV2
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LottiefilesApi {
    @GET("v1/recent")
    fun getRecent(@Query("page") page: Int): Single<AnimationResponse>

    @GET("v1/popular")
    fun getPopular(@Query("page") page: Int): Single<AnimationResponse>

    @GET("v2/featured")
    fun getCollection(): Single<AnimationResponseV2>

    @GET("v1/search/{query}")
    fun search(@Path("query") query: String, @Query("page") page: Int): Single<AnimationResponse>
}