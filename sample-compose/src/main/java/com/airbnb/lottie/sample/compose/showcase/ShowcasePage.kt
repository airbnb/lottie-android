package com.airbnb.lottie.sample.compose.showcase

import android.app.Application
import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.RowScope.gravity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.viewModel
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.ui.tooling.preview.Preview
import com.airbnb.lottie.sample.compose.R
import com.airbnb.lottie.sample.compose.api.AnimationResponseV2
import com.airbnb.lottie.sample.compose.api.LottieFilesApi
import com.airbnb.lottie.sample.compose.composables.LottieAnimation
import com.airbnb.lottie.sample.compose.ui.LottieTheme
import com.airbnb.lottie.sample.compose.ui.textColorDark
import com.airbnb.mvrx.*
import dev.chrisbanes.accompanist.coil.CoilImage
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

@Composable
fun ShowcasePage() {
    val viewModel: ShowcaseViewModel = viewModel()
    val featuredAnimations = viewModel.featuredAnimations.collectAsState()
    ShowcasePage(featuredAnimations.value)
}

@Composable
fun ShowcasePage(featuredAnimations: Async<AnimationResponseV2>) {
    val scrollState = rememberScrollState()
    Log.d("Gabe", "ShowcasePage: $featuredAnimations")
    Stack(
        modifier = Modifier.fillMaxSize()
    ) {
        ScrollableColumn(
            scrollState = scrollState
        ) {
            Marquee("Showcase")
        }
        Loader(modifier = Modifier.gravity(Alignment.Center))
//        if (featuredAnimations is Uninitialized || featuredAnimations is Loading) {
//        }
    }
}

@Composable
fun Marquee(text: String) {
    Text(
        text,
        fontSize = 16.sp,
        color = textColorDark,
        modifier = Modifier
            .padding(vertical = 24.dp, horizontal = 16.dp)
    )
}

@Composable
fun Loader(
    modifier: Modifier = Modifier
) {
    LottieAnimation(
        R.raw.loading,
        modifier = Modifier
            .preferredSize(100.dp)
            .then(modifier)
    )
}

@Composable
fun AnimationRow(
    title: String,
    previewUrl: String,
    previewBackgroundColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.padding(16.dp)
    ) {
        CoilImage(
            data = previewUrl,
            modifier = Modifier
                .preferredSize(40.dp)
                .padding(end = 16.dp)
        )
        Text(
            title,
            fontSize = 16.sp,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    LottieTheme {
        ShowcasePage()
    }
}