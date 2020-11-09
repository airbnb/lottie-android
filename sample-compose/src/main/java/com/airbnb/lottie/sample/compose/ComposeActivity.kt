package com.airbnb.lottie.sample.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.KEY_ROUTE
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigate
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.LottieAnimationSpec
import com.airbnb.lottie.sample.compose.lottiefiles.LottieFilesPage
import com.airbnb.lottie.sample.compose.player.PlayerPage
import com.airbnb.lottie.sample.compose.preview.PreviewPage
import com.airbnb.lottie.sample.compose.showcase.ShowcasePage
import com.airbnb.lottie.sample.compose.ui.LottieTheme
import com.airbnb.lottie.sample.compose.ui.Teal
import com.airbnb.lottie.sample.compose.ui.toColorSafe
import com.airbnb.lottie.sample.compose.utils.NavControllerAmbient
import com.airbnb.lottie.sample.compose.utils.drawTopBorder
import com.airbnb.lottie.sample.compose.utils.getBase64String

class ComposeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LottieScaffold()
        }
    }

    @Composable
    private fun LottieScaffold() {
        val navController = rememberNavController()

        Providers(
            NavControllerAmbient provides navController,
            BackPressedDispatcherAmbient provides (ContextAmbient.current as ComponentActivity).onBackPressedDispatcher
        ) {
            LottieTheme {
                Scaffold(
                    bottomBar = {
                        BottomNavigation(
                            backgroundColor = Color(0xFFF7F7F7),
                            elevation = 8.dp,
                            contentColor = Teal,
                        ) {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentRoute = navBackStackEntry?.arguments?.getString(KEY_ROUTE)

                            BottomNavItemData.values().forEach { item ->
                                BottomNavigationItem(
                                    icon = { Icon(vectorResource(item.iconRes)) },
                                    label = { Text(stringResource(item.labelRes)) },
                                    selected = currentRoute == item.route.route,
                                    onClick = {
                                        if (currentRoute != item.route.route) {
                                            navController.navigate(item.route.route)
                                        }
                                    },
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        NavHost(navController, startDestination = Route.Showcase.route) {
                            composable(Route.Showcase.route, arguments = Route.Showcase.args) { ShowcasePage() }
                            composable(Route.Preview.route, arguments = Route.Preview.args) { PreviewPage() }
                            composable(Route.LottieFiles.route, arguments = Route.LottieFiles.args) { LottieFilesPage() }
                            composable(Route.Learn.route, arguments = Route.Learn.args) { ShowcasePage() }
                            composable(
                                Route.Player.fullRoute,
                                arguments = Route.Player.args
                            ) { entry ->
                                val arguments = entry.arguments ?: error("No arguments provided to ${Route.Player}")
                                val spec = when {
                                    arguments.getString("url") != null -> LottieAnimationSpec.Url(arguments.getBase64String("url"))
                                    arguments.getString("file") != null -> LottieAnimationSpec.File(arguments.getBase64String("file"))
                                    arguments.getString("asset") != null -> LottieAnimationSpec.Asset(arguments.getBase64String("asset"))
                                    else -> error("You must specify a url, file, or asset")
                                }
                                val backgroundColor = when (arguments.getString("backgroundColor") != null) {
                                    true -> arguments.getBase64String("backgroundColor").toColorSafe()
                                    else -> null
                                }
                                PlayerPage(spec, backgroundColor)
                            }
                        }
                    }
                }
            }
        }
    }


    private enum class BottomNavItemData(val route: Route, @StringRes val iconRes: Int, @DrawableRes val labelRes: Int) {
        Showcase(Route.Showcase, R.drawable.ic_showcase, R.string.bottom_tab_showcase),
        Preview(Route.Preview, R.drawable.ic_device, R.string.bottom_tab_preview),
        LottieFiles(Route.LottieFiles, R.drawable.ic_lottie_files, R.string.bottom_tab_lottie_files),
        Learn(Route.Learn, R.drawable.ic_learn, R.string.bottom_tab_lottie_files),
    }
}