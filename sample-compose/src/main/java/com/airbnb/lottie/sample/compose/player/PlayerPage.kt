package com.airbnb.lottie.sample.compose.player

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.foundation.Icon
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope.weight
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.preferredWidth
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.ui.tooling.preview.Preview
import com.airbnb.lottie.sample.compose.BackPressedDispatcherAmbient
import com.airbnb.lottie.sample.compose.api.AnimationData
import com.airbnb.lottie.sample.compose.composables.LottieAnimation
import com.airbnb.lottie.sample.compose.composables.LottieAnimationSpec
import com.airbnb.lottie.sample.compose.composables.LottieComposeScaffoldView
import com.airbnb.lottie.sample.compose.composables.SeekBar
import com.airbnb.mvrx.args

class PlayerFragment : Fragment() {
    private val animationData: AnimationData by args()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return LottieComposeScaffoldView(requireContext()) {
            PlayerPage(animationData)
        }
    }
}

@Composable
fun PlayerPage(animationData: AnimationData) {
    val backPressedDispatcher = BackPressedDispatcherAmbient.current
    val spec = LottieAnimationSpec.Url(animationData.file)
    var progress by remember { mutableStateOf(0.5f) }
    var isPlaying by remember { mutableStateOf(true) }
    var isLooping by remember { mutableStateOf(true) }
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
            modifier = Modifier.fillMaxSize()
        )
        Row(
            verticalGravity = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { isPlaying = !isPlaying }) {
                Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow)
            }
            SeekBar(
                progress = progress,
                onProgressChanged = {
                    progress = it
                },
                modifier = Modifier
            )
            IconButton(onClick = { isLooping = !isLooping }) {
                Icon(
                    Icons.Filled.Repeat,
                    tint = if (isLooping) Color.Green else Color.Black,
                )
            }
        }
    }
}

@Composable
fun PlayPauseButton(isPlaying: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow)
    }
}

@Preview(name = "Player")
@Composable
fun PlayerPagePreview() {
    Providers(
        BackPressedDispatcherAmbient provides OnBackPressedDispatcher()
    ) {
        PlayerPage(animationData = AnimationData(123, null, null, "Title", "https://lottiefiles.com/download/public/32922"))
    }
}