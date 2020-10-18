package com.airbnb.lottie.sample.compose.composables

import androidx.compose.foundation.layout.preferredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieAnimationSpec
import com.airbnb.lottie.sample.compose.R

@Composable
fun Loader(
    modifier: Modifier = Modifier
) {
    val animationSpec = remember { LottieAnimationSpec.RawRes(R.raw.loading) }
    LottieAnimation(
        animationSpec,
        modifier = Modifier
            .preferredSize(100.dp)
            .then(modifier)
    )
}