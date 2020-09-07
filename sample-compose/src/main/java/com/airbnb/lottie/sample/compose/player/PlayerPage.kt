package com.airbnb.lottie.sample.compose.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.preferredSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.Fragment
import com.airbnb.lottie.sample.compose.api.AnimationData
import com.airbnb.lottie.sample.compose.composables.LottieAnimation
import com.airbnb.lottie.sample.compose.composables.LottieAnimationSpec
import com.airbnb.lottie.sample.compose.composables.LottieComposeScaffoldView
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
    val spec = LottieAnimationSpec.Url(animationData.file)
    LottieAnimation(
        spec,
        modifier = Modifier.fillMaxSize()
    )
}