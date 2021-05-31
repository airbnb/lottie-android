package com.airbnb.lottie.sample.compose.examples

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.airbnb.lottie.sample.compose.R

@Composable
fun BasicUsageExamplePage() {
    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = { backPressedDispatcher?.onBackPressed() },
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Box(modifier = Modifier.height(16.dp))
            ExampleCard("Example 1", "Repeat once") {
                Example1()
            }
            ExampleCard("Example 2", "Repeat forever") {
                Example2()
            }
            ExampleCard("Example 3", "Repeat forever from 50% to 75%") {
                Example3()
            }
            ExampleCard("Example 4", "Using LottieAnimationResult") {
                Example4()
            }
            ExampleCard("Example 5", "Using LottieComposition") {
                Example5()
            }
            ExampleCard("Example 6", "Splitting out the animation driver") {
                Example6()
            }
            ExampleCard("Example 7", "Toggle on click - click me") {
                Example7()
            }
        }
    }
}

/**
 * Nice and easy... This will play one time as soon as the composition loads
 * then it will stop.
 */
@Composable
private fun Example1() {
    LottieAnimation(LottieCompositionSpec.RawRes(R.raw.heart))
}

/**
 * This will repeat forever.
 */
@Composable
private fun Example2() {
    LottieAnimation(
        LottieCompositionSpec.RawRes(R.raw.heart),
        repeatCount = Integer.MAX_VALUE,
    )
}

/**
 * This will repeat between 50% and 75% forever.
 */
@Composable
private fun Example3() {
    LottieAnimation(
        LottieCompositionSpec.RawRes(R.raw.heart),
        repeatCount = Integer.MAX_VALUE,
        clipSpec = LottieClipSpec.MinAndMaxProgress(0.5f, 0.75f),
    )
}

/**
 * Here, you can check the result for loading/failure states.
 */
@Composable
private fun Example4() {
    val compositionResult = lottieComposition(LottieCompositionSpec.RawRes(R.raw.heart))
    when {
        compositionResult.isLoading -> {
            Text("Animation is loading...")
        }
        compositionResult.isFailure -> {
            Text("Animation failed to load")
        }
        else -> {
            LottieAnimation(compositionResult())
        }
    }
}

/**
 * If you just want access to the composition itself, you can use the delegate
 * version of lottieComposition like this.
 */
@Composable
private fun Example5() {
    val composition by lottieComposition(LottieCompositionSpec.RawRes(R.raw.heart))
    LottieAnimation(
        composition,
        progress = 0.85f,
    )
}

/**
 * Here, you have access to the composition and animation individually.
 */
@Composable
private fun Example6() {
    val composition by lottieComposition(LottieCompositionSpec.RawRes(R.raw.heart))
    val progress by animateLottieComposition(
        composition,
        repeatCount = Integer.MAX_VALUE,
    )
    LottieAnimation(
        composition,
        progress,
    )
}

/**
 * Here, you can toggle playback by clicking the animation.
 */
@Composable
private fun Example7() {
    var isPlaying by remember { mutableStateOf(false) }
    LottieAnimation(
        LottieCompositionSpec.RawRes(R.raw.heart),
        repeatCount = Integer.MAX_VALUE,
        // When this is true, it it will start from 0 every time it is played again.
        // When this is false, it will resume from the progress it was pause at.
        restartOnPlay = false,
        isPlaying = isPlaying,
        modifier = Modifier
            .clickable { isPlaying = !isPlaying }
    )
}

@Composable
private fun ExampleCard(
    name: String,
    description: String,
    content: @Composable () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        elevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(bottom = 16.dp)
            .padding(horizontal = 48.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color.Gray)
            ) {
                content()
            }
            Text(
                name,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 4.dp)
            )
            Text(
                description,
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            )
        }
    }
}

@Preview
@Composable
fun ExampleCardPreview() {
    ExampleCard("Example 1", "Heart animation") {
        LottieAnimation(LottieCompositionSpec.RawRes(R.raw.heart))
    }
}