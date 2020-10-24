package com.airbnb.lottie.sample.compose.composables

import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProgressIndicatorConstants
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedTask
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.delay

@Composable
fun DebouncedCircularProgressIndicator(
    delayMs: Long = 1_500L,
    color: Color = MaterialTheme.colors.primary,
    strokeWidth: Dp = ProgressIndicatorConstants.DefaultStrokeWidth,
    modifier: Modifier = Modifier,
) {
    var readyToShow by remember { mutableStateOf(false) }
    LaunchedTask {
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