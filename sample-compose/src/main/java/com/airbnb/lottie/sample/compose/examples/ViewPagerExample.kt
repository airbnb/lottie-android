package com.airbnb.lottie.sample.compose.examples

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.sample.compose.R

@Composable
fun ViewPagerExamplePage() {
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Magenta)
    val pagerState = rememberPagerState { colors.size }
    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        HorizontalPager(pagerState) { page ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors[page]),
            )
        }
        WalkthroughAnimation(pagerState, colors.size)
    }
}

@Composable
private fun WalkthroughAnimation(pagerState: PagerState, size: Int) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.walkthrough))
    val progress by remember { derivedStateOf { (pagerState.currentPage + pagerState.currentPageOffsetFraction) / (size - 1f) } }
    LottieAnimation(
        composition,
        { progress },
        modifier = Modifier
            .fillMaxSize(),
    )
}
