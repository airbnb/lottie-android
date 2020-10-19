package com.airbnb.lottie.sample.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Scaffold
import androidx.compose.material.ripple.RippleIndication
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.sample.compose.player.PlayerFragment
import com.airbnb.lottie.sample.compose.ui.LottieTheme
import com.airbnb.lottie.sample.compose.ui.purple200
import com.airbnb.lottie.sample.compose.ui.purple700
import com.airbnb.lottie.sample.compose.utils.NavControllerAmbient
import com.airbnb.mvrx.asMavericksArgs

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
                                BottomAppBar(
                                    backgroundColor = Color(0xFFF7F7F7),
                                    elevation = 8.dp,
                                    contentColor = purple200,
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceAround,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        BottomBarButton(R.drawable.ic_showcase, R.string.bottom_tab_showcase) {
                                            findNavController().navigate(R.id.showcase)
                                        }

                                        BottomBarButton(R.drawable.ic_lottie_files, R.string.bottom_tab_lottie_files) {
                                            findNavController().navigate(R.id.lottie_files)
                                        }
                                        BottomBarButton(R.drawable.ic_device, R.string.bottom_tab_preview) {
                                            // DO NOT SUBMIT
                                            val args = PlayerFragment.Args.Url("https://assets4.lottiefiles.com/private_files/lf30_8xbh8fop.json")
                                            findNavController().navigate(R.id.player, args.asMavericksArgs())
//                                            findNavController().navigate(R.id.preview)
                                        }
                                        BottomBarButton(R.drawable.ic_learn, R.string.bottom_tab_learn) {
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
    private fun BottomBarButton(@DrawableRes iconRes: Int, @StringRes labelRes: Int, onClick: () -> Unit) {
        Column(
            modifier = Modifier
                .clickable(
                    onClick = onClick,
                    indication = RippleIndication(bounded = false)
                )
                .padding(6.dp)
        ) {
            Icon(
                vectorResource(iconRes),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )
            Text(
                stringResource(labelRes),
                style = TextStyle(fontSize = 12.sp)
            )
        }
    }
}
