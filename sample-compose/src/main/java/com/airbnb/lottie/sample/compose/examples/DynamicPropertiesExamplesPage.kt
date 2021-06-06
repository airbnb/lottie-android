package com.airbnb.lottie.sample.compose.examples

import android.graphics.PointF
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import com.airbnb.lottie.sample.compose.R

@Composable
fun DynamicPropertiesExamplesPage() {
    UsageExamplePageScaffold {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            ExampleCard("Heart Color", "Click to change color") {
                HeartColor()
            }
            ExampleCard("Jump Height", "Click to jump heiht") {
                JumpHeight()
            }
            ExampleCard("Change Properties", "Click to toggle whether the dynamic property is used") {
                ToggleProperty()
            }
        }
    }
}

@Composable
private fun HeartColor() {
    val colors = remember {
        listOf(
            Color.Red,
            Color.Green,
            Color.Blue,
            Color.Yellow,
        )
    }
    var colorIndex by remember { mutableStateOf(0) }
    val color by derivedStateOf { colors[colorIndex] }

    val dynamicProperties = rememberLottieDynamicProperties(
        rememberLottieDynamicProperty(LottieProperty.COLOR, color.toArgb(), "H2", "Shape 1", "Fill 1"),
    )
    LottieAnimation(
        LottieCompositionSpec.RawRes(R.raw.heart),
        repeatCount = Integer.MAX_VALUE,
        dynamicProperties = dynamicProperties,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { colorIndex = (colorIndex + 1) % colors.size },
            )
    )
}

@Composable
private fun JumpHeight() {
    val extraJumpHeights = remember { listOf(0.dp, 24.dp, 48.dp, 128.dp) }
    var extraJumpIndex by remember { mutableStateOf(0) }
    val extraJumpHeight by derivedStateOf { extraJumpHeights[extraJumpIndex] }
    val extraJumpHeightPx = with(LocalDensity.current) { extraJumpHeight.toPx() }

    val point = remember { PointF() }
    val dynamicProperties = rememberLottieDynamicProperties(
        rememberLottieDynamicProperty(LottieProperty.TRANSFORM_POSITION, "Body") { frameInfo ->
            var startY = frameInfo.startValue.y
            var endY = frameInfo.endValue.y
            when {
                startY > endY -> startY += extraJumpHeightPx
                else -> endY += extraJumpHeightPx
            }
            point.set(frameInfo.startValue.x, lerp(startY, endY, frameInfo.interpolatedKeyframeProgress))
            point
        }
    )
    LottieAnimation(
        LottieCompositionSpec.Asset("AndroidWave.json"),
        repeatCount = Integer.MAX_VALUE,
        dynamicProperties = dynamicProperties,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { extraJumpIndex = (extraJumpIndex + 1) % extraJumpHeights.size },
            )
    )
}

@Composable
private fun ToggleProperty() {
    var useDynamicProperty by remember { mutableStateOf(true) }
    val dynamicProperties = rememberLottieDynamicProperties(
        rememberLottieDynamicProperty(LottieProperty.COLOR, Color.Green.toArgb(), "H2", "Shape 1", "Fill 1"),
    )
    LottieAnimation(
        LottieCompositionSpec.RawRes(R.raw.heart),
        repeatCount = Integer.MAX_VALUE,
        dynamicProperties = dynamicProperties.takeIf { useDynamicProperty },
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { useDynamicProperty = !useDynamicProperty },
            )
    )
}

private fun lerp(valueA: Float, valueB: Float, progress: Float): Float {
    val smallerY = minOf(valueA, valueB)
    val largerY = maxOf(valueA, valueB)
    return smallerY + progress * (largerY - smallerY)
}