package com.airbnb.lottie.sample.compose.preview

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.compose.navArgument
import androidx.ui.tooling.preview.Preview
import com.airbnb.lottie.sample.compose.R
import com.airbnb.lottie.sample.compose.Route
import com.airbnb.lottie.sample.compose.composables.Marquee
import com.airbnb.lottie.sample.compose.ui.LottieTheme
import com.airbnb.lottie.sample.compose.utils.findNavController
import androidx.navigation.compose.navigate

@Composable
fun PreviewPage() {
    var showingAssetsDialog by remember { mutableStateOf(false) }
    var showingUrlDialog by remember { mutableStateOf(false) }
    val navController = findNavController()

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
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .preferredHeight(48.dp)
            ) {
                Icon(
                    vectorResource(iconRes),
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(16.dp)
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
    val context = ContextAmbient.current
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
        modifier = Modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(name)
    }
}

@Preview
@Composable
fun PreviewPagePreview() {
    LottieTheme {
        Box(modifier = Modifier.background(Color.White)) {
            PreviewPage()
        }
    }
}
