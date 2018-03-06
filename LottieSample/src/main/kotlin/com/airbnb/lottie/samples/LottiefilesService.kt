package com.airbnb.lottie.samples

import com.airbnb.lottie.samples.model.AnimationResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LottiefilesService {
    @GET("recent")
    fun getRecent(@Query("page") page: Int): Observable<AnimationResponse>

    @GET("popular")
    fun getPopular(@Query("page") page: Int): Observable<AnimationResponse>

    @GET("collections/{collection}")
    fun getCollection(@Path("collection") collection: String): Observable<AnimationResponse>

    @GET("search/{query}")
    fun search(@Path("query") query: String): Observable<AnimationResponse>
}