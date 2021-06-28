package com.airbnb.lottie.sample.compose.examples

import android.graphics.PointF
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
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
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.heart))
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
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = color.toArgb(),
            keyPath = arrayOf(
                "H2",
                "Shape 1",
                "Fill 1",
            )
        ),
    )
    LottieAnimation(
        composition,
        iterations = LottieConstants.IterateForever,
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
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("AndroidWave.json"))
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
        composition,
        iterations = LottieConstants.IterateForever,
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
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.heart))
    var useDynamicProperty by remember { mutableStateOf(true) }
    val dynamicProperties = rememberLottieDynamicProperties(
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = Color.Green.toArgb(),
            keyPath = arrayOf(
                "H2",
                "Shape 1",
                "Fill 1",
            )
        ),
    )
    LottieAnimation(
        composition,
        iterations = LottieConstants.IterateForever,
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