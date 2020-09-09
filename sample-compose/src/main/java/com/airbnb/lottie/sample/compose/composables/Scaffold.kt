package com.airbnb.lottie.sample.compose.composables

import android.content.Context
import android.util.Log
import android.view.View
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Icon
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.BottomAppBar
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.vectorResource
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.sample.compose.NavControllerAmbient
import com.airbnb.lottie.sample.compose.R
import com.airbnb.lottie.sample.compose.ui.LottieTheme

fun Fragment.LottieComposeScaffoldView(context: Context, Content: @Composable () -> Unit): View {
    return ComposeView(context).apply {
        setContent {
            Providers(NavControllerAmbient provides findNavController()) {
                LottieTheme {
                    Scaffold(
                        bottomBar = {
                            BottomAppBar{
                                Row(
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    BottomBarButton(R.drawable.ic_showcase) {
                                        findNavController().navigate(R.id.showcase)
                                    }

                                    BottomBarButton(R.drawable.ic_lottie_files) {
                                        findNavController().navigate(R.id.lottie_files)
                                    }
                                    BottomBarButton(R.drawable.ic_device) {
                                    }
                                    BottomBarButton(R.drawable.ic_learn) {
                                    }
                                }
                            }
                        }
                    ){
                        Content()
                    }
                }
            }
        }
    }
}

@Composable
fun BottomBarButton(@DrawableRes iconRes: Int, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(vectorResource(iconRes))
    }
}