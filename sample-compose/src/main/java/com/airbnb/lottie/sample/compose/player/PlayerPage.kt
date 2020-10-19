package com.airbnb.lottie.sample.compose.player

import android.os.Parcelable
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.foundation.Icon
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.ui.tooling.preview.Preview
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieAnimationSpec
import com.airbnb.lottie.compose.rememberLottieAnimationState
import com.airbnb.lottie.sample.compose.BackPressedDispatcherAmbient
import com.airbnb.lottie.sample.compose.ComposeFragment
import com.airbnb.lottie.sample.compose.composables.SeekBar
import com.airbnb.lottie.sample.compose.ui.toColorSafe
import com.airbnb.mvrx.args
import kotlinx.android.parcel.Parcelize

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
    val animationState = rememberLottieAnimationState(autoPlay = true, repeatCount = Integer.MAX_VALUE)
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
                spec,
                animationState,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { animationState.toggleIsPlaying() }) {
                Icon(if (animationState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow)
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