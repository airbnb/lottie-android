package com.airbnb.lottie.sample.compose.composables

import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.delay

@Composable
fun DebouncedCircularProgressIndicator(
    modifier: Modifier = Modifier,
    delayMs: Long = 1_500L,
    color: Color = MaterialTheme.colors.primary,
    strokeWidth: Dp = ProgressIndicatorDefaults.StrokeWidth,
) {
    var readyToShow by remember { mutableStateOf(false) }
    LaunchedEffect(readyToShow) {
        delay(delayMs)
        readyToShow = true
    }
    if (readyToShow) {
        CircularProgressIndicator(
            color = color,
            strokeWidth = strokeWidth,
            modifier = modifier
        )
    }
}