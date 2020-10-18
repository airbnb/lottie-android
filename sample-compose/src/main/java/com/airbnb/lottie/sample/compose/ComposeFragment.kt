package com.airbnb.lottie.sample.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Icon
import androidx.compose.foundation.layout.*
import androidx.compose.material.BottomAppBar
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.res.vectorResource
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.sample.compose.ui.LottieTheme
import com.airbnb.lottie.sample.compose.utils.NavControllerAmbient

abstract class ComposeFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                Providers(
                    NavControllerAmbient provides findNavController(),
                    BackPressedDispatcherAmbient provides (ContextAmbient.current as ComponentActivity).onBackPressedDispatcher
                ) {
                    LottieTheme {
                        Scaffold(
                            bottomBar = {
                                BottomAppBar {
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
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier.padding(innerPadding)
                            ) {
                                root()
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    abstract fun root()


    @Composable
    private fun BottomBarButton(@DrawableRes iconRes: Int, onClick: () -> Unit) {
        IconButton(onClick = onClick) {
            Icon(vectorResource(iconRes))
        }
    }
}
