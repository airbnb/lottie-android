package com.airbnb.lottie.sample.compose.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.foundation.Icon
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.remember
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.Fragment
import androidx.ui.tooling.preview.Preview
import com.airbnb.lottie.sample.compose.BackPressedDispatcherAmbient
import com.airbnb.lottie.sample.compose.api.AnimationDataV2
import com.airbnb.lottie.sample.compose.composables.LottieAnimation
import com.airbnb.lottie.sample.compose.composables.LottieAnimationController
import com.airbnb.lottie.sample.compose.composables.LottieAnimationSpec
import com.airbnb.lottie.sample.compose.composables.LottieAnimationState
import com.airbnb.lottie.sample.compose.composables.LottieComposeScaffoldView
import com.airbnb.lottie.sample.compose.composables.SeekBar
import com.airbnb.mvrx.args

class PlayerFragment : Fragment() {
    private val animationData: AnimationDataV2 by args()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return LottieComposeScaffoldView(requireContext()) {
            PlayerPage(animationData)
        }
    }
}

@Composable
fun PlayerPage(animationData: AnimationDataV2) {
    val backPressedDispatcher = BackPressedDispatcherAmbient.current
    val spec = remember { LottieAnimationSpec.Url(animationData.file) }
    val animationController = remember { LottieAnimationController(LottieAnimationState(repeatCount = Integer.MAX_VALUE, isPlaying = true)) }
    val animationState by animationController.state.collectAsState()
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
        LottieAnimation(
            spec,
            animationController,
            modifier = Modifier.fillMaxSize()
        )
        Row(
            verticalGravity = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { animationController.toggleIsPlaying() }) {
                Icon(if (animationState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow)
            }
            SeekBar(
                progress = animationState.progress,
                onProgressChanged = {
                    animationController.setProgress(it)
                },
                modifier = Modifier
            )
            IconButton(onClick = {
                val repeatCount = if (animationState.repeatCount== Integer.MAX_VALUE) 0 else Integer.MAX_VALUE
                animationController.setRepeatCount(repeatCount)
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
        PlayerPage(animationData = AnimationDataV2(123, null, null, "Title", "https://lottiefiles.com/download/public/32922"))
    }
}