package com.airbnb.lottie.sample.compose.preview

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.sample.compose.R
import com.airbnb.lottie.sample.compose.Route
import com.airbnb.lottie.sample.compose.composables.Marquee
import com.airbnb.lottie.sample.compose.ui.LottieTheme

@Composable
fun PreviewPage(navController: NavController) {
    var showingAssetsDialog by remember { mutableStateOf(false) }
    var showingUrlDialog by remember { mutableStateOf(false) }

    Column {
        Marquee(stringResource(R.string.tab_preview))
        PreviewRow(R.drawable.ic_qr_scan, R.string.scan_qr_code) {

        }
        PreviewRow(R.drawable.ic_file, R.string.open_file) {

        }
        PreviewRow(R.drawable.ic_network, R.string.enter_url) {
            showingUrlDialog = true
        }
        PreviewRow(R.drawable.ic_storage, R.string.load_from_assets) {
            showingAssetsDialog = true
        }
    }

    AssetsDialog(
        showingAssetsDialog,
        onDismiss = { showingAssetsDialog = false },
        onAssetSelected = { assetName ->
            navController.navigate(Route.Player.forAsset(assetName))
        }
    )
    UrlDialog(
        showingUrlDialog,
        onDismiss = { showingUrlDialog = false },
        onUrlSelected = { url ->
            navController.navigate(Route.Player.forUrl(url))
        }
    )
}

@Composable
private fun PreviewRow(
    @DrawableRes iconRes: Int,
    @StringRes textRes: Int,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .height(48.dp)
            ) {
                Icon(
                    painterResource(iconRes),
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(16.dp),
                    contentDescription = null
                )
                Text(
                    stringResource(textRes),
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                )
            }
            Divider(color = Color.LightGray)
        }
    }
}

@Composable
fun AssetsDialog(isShowing: Boolean, onDismiss: () -> Unit, onAssetSelected: (assetName: String) -> Unit) {
    if (!isShowing) return
    val context = LocalContext.current
    val assets = context.assets.list("")
        ?.asSequence()
        ?.filter { it.endsWith(".json") || it.endsWith(".zip") }
        ?.toList()
        ?: emptyList()
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(4.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
            ) {
                assets.forEach { asset ->
                    AssetRow(asset, onClick = {
                        onDismiss()
                        onAssetSelected(asset)
                    })
                }
            }
        }
    }
}

@Composable
fun UrlDialog(isShowing: Boolean, onDismiss: () -> Unit, onUrlSelected: (url: String) -> Unit) {
    if (!isShowing) return
    var url by remember { mutableStateOf("") }
    Dialog(onDismissRequest = {
        url = ""
        onDismiss()
    }) {
        Surface(
            shape = RoundedCornerShape(4.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    stringResource(R.string.enter_url),
                    fontSize = 18.sp,
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(stringResource(R.string.url)) },
                )
                TextButton(
                    onClick = { onUrlSelected(url) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.ok))
                }
            }
        }
    }
}

@Composable
private fun AssetRow(name: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(name)
    }
}

@Preview
@Composable
fun PreviewPagePreview() {
    val navController = rememberNavController()
    LottieTheme {
        Box(modifier = Modifier.background(Color.White)) {
            PreviewPage(navController)
        }
    }
}
