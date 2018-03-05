package com.airbnb.lottie.samples

import com.airbnb.lottie.samples.model.AnimationResponse
import io.reactivex.Observable
import retrofit2.http.GET

interface LottiefilesService {
    @GET("recent")
    fun getRecent(): Observable<AnimationResponse>
}