package com.airbnb.lottie.sample.compose.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Text
import androidx.compose.runtime.Composable
import androidx.fragment.app.Fragment
import com.airbnb.lottie.sample.compose.ComposeFragment
import com.airbnb.lottie.sample.compose.composables.LottieComposeScaffoldView
import com.airbnb.mvrx.args

class PlayerFragment : Fragment() {
    private val animationId: Int by args()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return LottieComposeScaffoldView(requireContext()) {
            PlayerPage(animationId)
        }
    }
}

@Composable
fun PlayerPage(animationId: Int) {
    Text("$animationId")
}