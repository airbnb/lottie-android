package com.airbnb.lottie.sample.compose.examples

import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.sample.compose.R
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.PagerDefaults
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState

@Composable
fun ViewPagerExamplePage() {
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Magenta)
    val pagerState = rememberPagerState(pageCount = colors.size)
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        HorizontalPager(
            pagerState,
            flingBehavior = PagerDefaults.defaultPagerFlingConfig(
                pagerState,
                decayAnimationSpec = exponentialDecay(frictionMultiplier = 0.05f),
            )
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors[page])
            )
        }
        WalkthroughAnimation(pagerState)
        HorizontalPagerIndicator(
            pagerState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )

    }
}

@Composable
private fun WalkthroughAnimation(pagerState: PagerState) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.walkthrough))
    val progress by derivedStateOf { (pagerState.currentPage + pagerState.currentPageOffset) / (pagerState.pageCount - 1f) }
    LottieAnimation(
        composition,
        progress,
        modifier = Modifier
            .fillMaxSize()
    )
}