package com.airbnb.lottie.sample.compose

import android.util.Base64
import androidx.navigation.compose.NamedNavArgument
import androidx.navigation.compose.navArgument
import java.net.URLEncoder

sealed class Route(val route: String, val args: List<NamedNavArgument> = emptyList()) {
    object Showcase : Route("showcase")

    object Preview : Route("preview")

    object LottieFiles : Route("lottiefiles")

    object Learn : Route("learn")

    object Player : Route(
        "player",
        listOf(
            navArgument("url") {
                androidx.navigation.NavType.StringType
                nullable = true
            },
            navArgument("file") {
                androidx.navigation.NavType.StringType
                nullable = true
            },
            navArgument("asset") {
                androidx.navigation.NavType.StringType
                nullable = true
            },
            navArgument("backgroundColor") {
                androidx.navigation.NavType.StringType
                nullable = true
            },
        )
    ) {
        val fullRoute = "$route?url={url}&file={file}&asset={asset}&backgroundColor={backgroundColor}"

        fun forUrl(url: String, backgroundColor: String? = null) = when (backgroundColor) {
            null -> "${route}?url=${url.toBase64()}"
            else -> "${route}?url=${url.toBase64()}&backgroundColor=${backgroundColor.toBase64()}"
        }

        fun forFile(file: String) = "${route}?file=${file.toBase64()}"

        fun forAsset(asset: String) = "${route}?asset=${asset.toBase64()}"
    }
}

private fun String.toBase64() = Base64.encodeToString(toByteArray(), Base64.NO_PADDING or Base64.NO_WRAP or Base64.URL_SAFE)