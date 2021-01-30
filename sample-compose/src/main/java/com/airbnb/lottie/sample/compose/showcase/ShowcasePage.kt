package com.airbnb.lottie.sample.compose.showcase

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.navigate
import com.airbnb.lottie.sample.compose.Route
import com.airbnb.lottie.sample.compose.composables.AnimationRow
import com.airbnb.lottie.sample.compose.composables.Loader
import com.airbnb.lottie.sample.compose.composables.Marquee
import com.airbnb.lottie.sample.compose.ui.LottieTheme
import com.airbnb.lottie.sample.compose.utils.findNavController
import com.airbnb.lottie.sample.compose.utils.mavericksViewModelAndState
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.Uninitialized

@Composable
fun ShowcasePage() {
    val (_, showcaseState) = mavericksViewModelAndState<ShowcaseViewModel, ShowcaseState>()
    val featuredAnimations = showcaseState.animations
    val navController = findNavController()
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn {
            item {
                Marquee("Showcase")
            }
            items(featuredAnimations()?.data.orEmpty()) { data ->
                AnimationRow(
                    title = data.title,
                    previewUrl = data.preview_url ?: "",
                    previewBackgroundColor = data.bgColor,
                ) {
                    navController.navigate(Route.Player.forUrl(data.file, backgroundColor = data.bg_color))
                }
                Divider(color = Color.LightGray)
            }
        }
        if (featuredAnimations is Uninitialized || featuredAnimations is Loading) {
            Loader(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    LottieTheme {
        ShowcasePage()
    }
}