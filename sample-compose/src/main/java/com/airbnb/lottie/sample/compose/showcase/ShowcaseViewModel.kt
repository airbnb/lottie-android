package com.airbnb.lottie.sample.compose.showcase

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.airbnb.lottie.sample.compose.api.AnimationResponseV2
import com.airbnb.lottie.sample.compose.api.LottieFilesApi
import com.airbnb.mvrx.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

class ShowcaseViewModel(application: Application) : AndroidViewModel(application) {
    private val _featuredAnimations = MutableStateFlow(Uninitialized as Async<AnimationResponseV2>)
    val featuredAnimations: StateFlow<Async<AnimationResponseV2>> = _featuredAnimations

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.lottiefiles.com/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val api = retrofit.create<LottieFilesApi>()

    init {
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