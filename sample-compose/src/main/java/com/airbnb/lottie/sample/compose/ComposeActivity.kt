package com.airbnb.lottie.sample.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.airbnb.lottie.compose.LottieAnimationSpec
import com.airbnb.lottie.sample.compose.lottiefiles.LottieFilesPage
import com.airbnb.lottie.sample.compose.player.PlayerPage
import com.airbnb.lottie.sample.compose.preview.PreviewPage
import com.airbnb.lottie.sample.compose.showcase.ShowcasePage
import com.airbnb.lottie.sample.compose.ui.LottieTheme
import com.airbnb.lottie.sample.compose.ui.Teal
import com.airbnb.lottie.sample.compose.ui.toColorSafe
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

        LottieTheme {
            Scaffold(
                bottomBar = {
                    BottomNavigation(
                        backgroundColor = Color(0xFFF7F7F7),
                        elevation = 8.dp,
                        contentColor = Teal,
                    ) {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route

                        BottomNavItemData.values().forEach { item ->
                            BottomNavigationItem(
                                icon = {
                                    Icon(
                                        painter = painterResource(item.iconRes),
                                        contentDescription = null
                                    )
                                },
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
                        composable(Route.Showcase.route, arguments = Route.Showcase.args) { ShowcasePage(navController) }
                        composable(Route.Preview.route, arguments = Route.Preview.args) { PreviewPage(navController) }
                        composable(Route.LottieFiles.route, arguments = Route.LottieFiles.args) { LottieFilesPage(navController) }
                        composable(Route.Learn.route, arguments = Route.Learn.args) { ShowcasePage(navController) }
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


    private enum class BottomNavItemData(val route: Route, @DrawableRes val iconRes: Int, @StringRes val labelRes: Int) {
        Showcase(Route.Showcase, R.drawable.ic_showcase, R.string.bottom_tab_showcase),
        Preview(Route.Preview, R.drawable.ic_device, R.string.bottom_tab_preview),
        LottieFiles(Route.LottieFiles, R.drawable.ic_lottie_files, R.string.bottom_tab_lottie_files),
        Docs(Route.Learn, R.drawable.ic_docs, R.string.bottom_tab_docs),
    }
}