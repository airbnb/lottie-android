package com.airbnb.lottie.sample.compose.showcase

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.airbnb.lottie.sample.compose.LottieComposeApplication
import com.airbnb.lottie.sample.compose.api.FeaturedAnimationsResponse
import com.airbnb.lottie.sample.compose.api.LottieFilesApi
import com.airbnb.mvrx.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import javax.inject.Inject

class ShowcaseViewModel(application: Application) : AndroidViewModel(application) {
    private val _featuredAnimations = MutableStateFlow(Uninitialized as Async<FeaturedAnimationsResponse>)
    val featuredAnimations: StateFlow<Async<FeaturedAnimationsResponse>> = _featuredAnimations

    @Inject
    lateinit var api: LottieFilesApi

    init {
        (application as LottieComposeApplication).component.inject(this)
        fetchFeatured()
    }

    fun fetchFeatured() {
        viewModelScope.launch(Dispatchers.IO) {
            _featuredAnimations.value = Loading()
            _featuredAnimations.value = try {
                Success(api.getFeatured())
            } catch (e: Throwable) {
                Log.d("Gabe", "fetchFeatured: failed", e)
                Fail(e)
            }
        }
    }
}