package com.airbnb.lottie.sample.compose.composables

import android.content.Context
import android.util.Log
import android.view.View
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.sample.compose.NavControllerAmbient
import com.airbnb.lottie.sample.compose.ui.LottieTheme

fun Fragment.LottieComposeScaffoldView(context: Context, Content: @Composable () -> Unit): View {
    return ComposeView(context).apply {
        setContent {
            Providers(NavControllerAmbient provides findNavController()) {
                LottieTheme {
                    Surface(color = MaterialTheme.colors.background) {
                        Content()
                    }
                }
            }
        }
    }
}