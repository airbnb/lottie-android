package com.airbnb.lottie.sample.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticAmbientOf
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.airbnb.lottie.sample.compose.ui.LottieTheme

val NavControllerAmbient = staticAmbientOf<NavController> { error("You must specify a NavController.") }

@Composable
fun findNavController() = NavControllerAmbient.current

abstract class ComposeFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
//                Providers(NavControllerAmbient provides Navigation.findNavController(this)) {
                    LottieTheme {
                        Surface(color = MaterialTheme.colors.background) {
                            Content()
                        }
                    }
//                }
            }
        }
    }

    @Composable
    abstract fun Content()
}