package com.airbnb.lottie.sample.compose.examples

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.sample.compose.R

@Composable
fun CachingExamplesPage() {
    UsageExamplePageScaffold {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            ExampleCard("Default Caching", "Lottie caches compositions by default") {
                Example1()
            }
            ExampleCard("Day/Night", "Animations in raw/res will automatically respect day and night mode") {
                Example2()
            }
            ExampleCard("Skip Cache", "Skip the cache") {
                Example3()
            }
        }
    }
}

@Composable
private fun Example1() {
    // By default, Lottie will cache compositions with a key derived from your LottieCompositionSpec.
    // If you request the composition multiple times or request it again at some point later, it
    // will return the previous composition. LottieComposition itself it stateless. All stateful
    // actions should happen within LottieAnimation.
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.heart))
    LottieAnimation(composition)
}

@Composable
private fun Example2() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.sun_moon))
    LottieAnimation(composition)
}

@Composable
private fun Example3() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.we_accept_inline_image),
        // Don't cache this composition. You may want to do this for animations that have images
        // because the bitmaps are much larger to store than the rest of the animation.
        cacheKey = null,
    )
    LottieAnimation(composition)
}