package com.airbnb.lottie.sample.compose.composables

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.sample.compose.R

@Composable
fun Loader(
    modifier: Modifier = Modifier
) {
    LottieAnimation(
        rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading)).value,
        modifier = modifier
            .size(100.dp)
    )
}