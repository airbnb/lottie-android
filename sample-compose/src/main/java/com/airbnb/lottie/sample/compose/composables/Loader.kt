package com.airbnb.lottie.sample.compose.composables

import androidx.compose.foundation.layout.preferredSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.sample.compose.R

@Composable
fun Loader(
    modifier: Modifier = Modifier
) {
    LottieAnimation(
        LottieAnimationSpec.RawRes(R.raw.loading),
        modifier = Modifier
            .preferredSize(100.dp)
            .then(modifier)
    )
}