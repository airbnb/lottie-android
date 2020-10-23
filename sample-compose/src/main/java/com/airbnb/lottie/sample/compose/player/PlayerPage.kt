package com.airbnb.lottie.sample.compose.player

import android.os.Parcelable
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.ui.tooling.preview.Preview
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.*
import com.airbnb.lottie.sample.compose.BackPressedDispatcherAmbient
import com.airbnb.lottie.sample.compose.ComposeFragment
import com.airbnb.lottie.sample.compose.R
import com.airbnb.lottie.sample.compose.composables.SeekBar
import com.airbnb.lottie.sample.compose.ui.toColorSafe
import com.airbnb.mvrx.args
import kotlinx.android.parcel.Parcelize
import kotlin.math.ceil
import kotlin.math.roundToInt

class PlayerFragment : ComposeFragment() {
    private val args: Args by args()

    @Composable
    override fun root() {
        val spec = when (val a = args) {
            is Args.Url -> LottieAnimationSpec.Url(a.url)
            is Args.File -> LottieAnimationSpec.File(a.fileName)
            is Args.Asset -> LottieAnimationSpec.Asset(a.assetName)
        }
        val backgroundColor = when (val a = args) {
            is Args.Url -> a.backgroundColorStr?.toColorSafe()
            else -> null
        }

        PlayerPage(spec, backgroundColor)
    }

    sealed class Args : Parcelable {
        /** colorStr is the value from the LottieFiles API. */
        @Parcelize
        class Url(val url: String, val backgroundColorStr: String? = null) : Args()

        @Parcelize
        class File(val fileName: String) : Args()

        @Parcelize
        class Asset(val assetName: String) : Args()
    }
}

@Composable
fun PlayerPage(
    spec: LottieAnimationSpec,
    backgroundColor: Color? = null,
) {
    val backPressedDispatcher = BackPressedDispatcherAmbient.current
    val composition: LottieComposition? = rememberLottieComposition(spec)
    val animationState = rememberLottieAnimationState(autoPlay = true, repeatCount = Integer.MAX_VALUE)
    val border = remember { mutableStateOf(false) }
    val speed = remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxHeight()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = { backPressedDispatcher.onBackPressed() },
            ) {
                Icon(Icons.Filled.Close)
            }
            IconButton(
                onClick = { backPressedDispatcher.onBackPressed() },
            ) {
                Icon(Icons.Filled.RemoveRedEye)
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .maybeBackground(backgroundColor)
        ) {
            LottieAnimation(
                composition,
                animationState,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
                    .maybeDrawBorder(border.value)
            )
        }
        if (speed.value) {
            SpeedToolbar(
                speed = animationState.speed,
                onSpeedChanged = { animationState.speed = it }
            )
        }
        PlayerControlsRow(animationState, composition)
        Toolbar(
            border = border,
            speed = speed,
        )
    }
}

@Composable
fun PlayerControlsRow(
    animationState: LottieAnimationState,
    composition: LottieComposition?,
) {
    val totalTime = ((composition?.duration ?: 0L / animationState.speed) / 1000.0)
    val totalTimeFormatted = ("%.1f").format(totalTime)

    val progress = (totalTime / 100.0) * ((animationState.progress * 100.0).roundToInt())
    val progressFormatted = ("%.1f").format(progress)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .drawTopBorder()
    ) {
        Box(
            alignment = Alignment.Center
        ) {
            IconButton(
                onClick = { animationState.toggleIsPlaying() },
            ) {
                Icon(if (animationState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow)
            }
            Text(
                "${animationState.frame}/${ceil(composition?.durationFrames ?: 0f).toInt()}\n${progressFormatted}/$totalTimeFormatted",
                style = TextStyle(fontSize = 8.sp),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 48.dp, bottom = 8.dp)
            )
        }
        SeekBar(
            progress = animationState.progress,
            onProgressChanged = {
                animationState.setProgress(it)
            },
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = {
            val repeatCount = if (animationState.repeatCount == Integer.MAX_VALUE) 0 else Integer.MAX_VALUE
            animationState.repeatCount = repeatCount
        }) {
            Icon(
                Icons.Filled.Repeat,
                tint = if (animationState.repeatCount > 0) Color.Green else Color.Black,
            )
        }
    }
}

@Composable
fun SpeedToolbar(
    speed: Float,
    onSpeedChanged: (Float) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = Modifier
            .drawTopBorder()
            .fillMaxWidth()
    ) {
        ToolbarChip(
            label = "0.5x",
            isActivated = speed == 0.5f,
            onClick = { onSpeedChanged(0.5f) },
            modifier = Modifier.padding(end = 8.dp)
        )
        ToolbarChip(
            label = "1x",
            isActivated = speed == 1f,
            onClick = { onSpeedChanged(1f) },
            modifier = Modifier.padding(end = 8.dp)
        )
        ToolbarChip(
            label = "1.5x",
            isActivated = speed == 1.5f,
            onClick = { onSpeedChanged(1.5f) },
            modifier = Modifier.padding(end = 8.dp)
        )
        ToolbarChip(
            label = "2x",
            isActivated = speed == 2f,
            onClick = { onSpeedChanged(2f) },
            modifier = Modifier.padding(end = 8.dp)
        )
    }
}

@Composable
fun Toolbar(
    border: MutableState<Boolean>,
    speed: MutableState<Boolean>,
) {
    ScrollableRow(
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 12.dp),
        modifier = Modifier
            .drawTopBorder()
            .fillMaxWidth()
    ) {
        ToolbarChip(
            iconRes = R.drawable.ic_border,
            label = stringResource(R.string.toolbar_item_border),
            isActivated = border.value,
            onClick = { border.value = it },
            modifier = Modifier.padding(end = 8.dp)
        )
        ToolbarChip(
            iconRes = R.drawable.ic_speed,
            label = stringResource(R.string.toolbar_item_speed),
            isActivated = speed.value,
            onClick = { speed.value = it},
            modifier = Modifier.padding(end = 8.dp)
        )
    }
}

@Preview
@Composable
fun SpeedToolbarPreview() {
    SpeedToolbar(speed = 1f, onSpeedChanged = {})
}

@Preview(name = "Player")
@Composable
fun PlayerPagePreview() {
    Providers(
        BackPressedDispatcherAmbient provides OnBackPressedDispatcher()
    ) {
        PlayerPage(LottieAnimationSpec.Url("https://lottiefiles.com/download/public/32922"))
    }
}

private fun Modifier.maybeBackground(color: Color?): Modifier {
    return if (color == null) {
        this
    } else {
        this.then(background(color))
    }
}

private fun Modifier.drawTopBorder(color: Color = Color.DarkGray) = this.then(drawBehind {
    drawRect(color, Offset.Zero, size = Size(size.width, 1f))
})

private fun Modifier.maybeDrawBorder(draw: Boolean, color: Color = Color.Black, width: Dp = 1.dp): Modifier {
    return if (draw) {
        this.then(border(width, color))
    } else {
        this
    }
}