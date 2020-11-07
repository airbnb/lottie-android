package com.airbnb.lottie.sample.compose.player

import android.os.Build
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.ScrollableRow
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredSize
import androidx.compose.foundation.layout.preferredWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedTask
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Providers
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.onCommit
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.ui.tooling.preview.Preview
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieAnimationSpec
import com.airbnb.lottie.compose.LottieAnimationState
import com.airbnb.lottie.compose.LottieCompositionResult
import com.airbnb.lottie.compose.rememberLottieAnimationState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.sample.compose.BackPressedDispatcherAmbient
import com.airbnb.lottie.sample.compose.BuildConfig
import com.airbnb.lottie.sample.compose.R
import com.airbnb.lottie.sample.compose.composables.DebouncedCircularProgressIndicator
import com.airbnb.lottie.sample.compose.composables.SeekBar
import com.airbnb.lottie.sample.compose.ui.Teal
import com.airbnb.lottie.sample.compose.utils.drawTopBorder
import com.airbnb.lottie.sample.compose.utils.maybeBackground
import com.airbnb.lottie.sample.compose.utils.maybeDrawBorder
import kotlin.math.ceil
import kotlin.math.roundToInt

@Composable
fun PlayerPage(
    spec: LottieAnimationSpec,
    animationBackgroundColor: Color? = null,
) {
    val backPressedDispatcher = BackPressedDispatcherAmbient.current
    val compositionResult = rememberLottieComposition(spec)
    val animationState = rememberLottieAnimationState(autoPlay = true, repeatCount = Integer.MAX_VALUE)
    val scaffoldState = rememberScaffoldState()
    val outlineMasksAndMattes = remember { mutableStateOf(false) }
    val applyOpacityToLayers = remember { mutableStateOf(false) }
    var focusMode by remember { mutableStateOf(false) }
    var backgroundColor by remember { mutableStateOf(animationBackgroundColor) }
    var showWarningsDialog by remember { mutableStateOf(false) }

    val borderToolbar = remember { mutableStateOf(false) }
    val speedToolbar = remember { mutableStateOf(false) }
    val backgroundColorToolbar = remember { mutableStateOf(false) }

    val failedMessage = stringResource(R.string.failed_to_load)
    val okMessage = stringResource(R.string.ok)

    LaunchedTask(compositionResult) {
        if (compositionResult is LottieCompositionResult.Fail) {
            scaffoldState.snackbarHostState.showSnackbar(
                message = failedMessage,
                actionLabel = okMessage,
            )
        }
    }

    onCommit(outlineMasksAndMattes.value) {
        animationState.outlineMasksAndMattes = outlineMasksAndMattes.value
    }
    onCommit(applyOpacityToLayers.value) {
        animationState.applyOpacityToLayers = applyOpacityToLayers.value
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = {},
                backgroundColor = Color.Transparent,
                elevation = 0.dp,
                navigationIcon = {
                    IconButton(
                        onClick = { backPressedDispatcher.onBackPressed() },
                    ) {
                        Icon(Icons.Default.Close)
                    }
                },
                actions = {
                    if (compositionResult()?.warnings?.isNotEmpty() == true) {
                        IconButton(
                            onClick = { showWarningsDialog = true }
                        ) {
                            Icon(
                                Icons.Filled.Warning,
                                tint = Color.Black,
                            )
                        }
                    }
                    IconButton(
                        onClick = { focusMode = !focusMode },
                    ) {
                        Icon(
                            Icons.Filled.RemoveRedEye,
                            tint = if (focusMode) Teal else Color.Black,
                        )
                    }
                }
            )
        },
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxHeight()
        ) {
            Box(
                alignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .maybeBackground(backgroundColor)
                    .fillMaxWidth()
            ) {
                LottieAnimation(
                    compositionResult,
                    animationState,
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center)
                        .maybeDrawBorder(borderToolbar.value)
                )
                if (compositionResult is LottieCompositionResult.Loading) {
                    DebouncedCircularProgressIndicator(
                        color = Teal,
                        modifier = Modifier
                            .preferredSize(48.dp)
                    )
                }
            }
            if (speedToolbar.value && !focusMode) {
                SpeedToolbar(
                    speed = animationState.speed,
                    onSpeedChanged = { animationState.speed = it }
                )
            }
            if (backgroundColorToolbar.value && !focusMode) {
                BackgroundColorToolbar(
                    animationBackgroundColor = animationBackgroundColor,
                    onColorChanged = { backgroundColor = it }
                )
            }
            if (!focusMode) {
                PlayerControlsRow(animationState, compositionResult())
                Toolbar(
                    border = borderToolbar,
                    speed = speedToolbar,
                    backgroundColor = backgroundColorToolbar,
                    outlineMasksAndMattes = outlineMasksAndMattes,
                    applyOpacityToLayers = applyOpacityToLayers,
                )
            }
        }
    }

    if (showWarningsDialog) {
        WarningDialog(warnings = compositionResult()?.warnings ?: emptyList(), onDismiss = { showWarningsDialog = false })
    }
}

@Composable
private fun PlayerControlsRow(
    animationState: LottieAnimationState,
    composition: LottieComposition?,
) {
    val totalTime = ((composition?.duration ?: 0L / animationState.speed) / 1000.0)
    val totalTimeFormatted = ("%.1f").format(totalTime)

    val progress = (totalTime / 100.0) * ((animationState.progress * 100.0).roundToInt())
    val progressFormatted = ("%.1f").format(progress)

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .drawTopBorder()
        ) {
            Box(
                alignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { animationState.toggleIsPlaying() },
                ) {
                    Icon(if (animationState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow)
                }
                Text(
                    "${animationState.frame}/${ceil(composition?.durationFrames ?: 0f).toInt()}\n${progressFormatted}/$totalTimeFormatted",
                    style = TextStyle(fontSize = 8.sp),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 48.dp, bottom = 8.dp)
                )
            }
            SeekBar(
                progress = animationState.progress,
                onProgressChanged = {
                    animationState.progress = it
                },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                val repeatCount = if (animationState.repeatCount == Integer.MAX_VALUE) 0 else Integer.MAX_VALUE
                animationState.repeatCount = repeatCount
            }) {
                Icon(
                    Icons.Filled.Repeat,
                    tint = if (animationState.repeatCount > 0) Teal else Color.Black,
                )
            }
        }
        Text(
            BuildConfig.VERSION_NAME,
            fontSize = 6.sp,
            color = Color.Gray,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
    }
}

@Composable
private fun SpeedToolbar(
    speed: Float,
    onSpeedChanged: (Float) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .drawTopBorder()
            .padding(vertical = 12.dp, horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        ToolbarChip(
            label = "0.5x",
            isActivated = speed == 0.5f,
            onClick = { onSpeedChanged(0.5f) },
            modifier = Modifier.padding(end = 8.dp)
        )
        ToolbarChip(
            label = "1x",
            isActivated = speed == 1f,
            onClick = { onSpeedChanged(1f) },
            modifier = Modifier.padding(end = 8.dp)
        )
        ToolbarChip(
            label = "1.5x",
            isActivated = speed == 1.5f,
            onClick = { onSpeedChanged(1.5f) },
            modifier = Modifier.padding(end = 8.dp)
        )
        ToolbarChip(
            label = "2x",
            isActivated = speed == 2f,
            onClick = { onSpeedChanged(2f) },
            modifier = Modifier.padding(end = 8.dp)
        )
    }
}

@Composable
private fun BackgroundColorToolbar(
    animationBackgroundColor: Color?,
    onColorChanged: (Color) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .drawTopBorder()
            .padding(vertical = 12.dp, horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        listOfNotNull(
            colorResource(R.color.background_color1),
            colorResource(R.color.background_color2),
            colorResource(R.color.background_color3),
            colorResource(R.color.background_color4),
            colorResource(R.color.background_color5),
            colorResource(R.color.background_color6),
            animationBackgroundColor.takeIf { it != Color.White },
        ).forEachIndexed { i, color ->
            val strokeColor = if (i == 0) colorResource(R.color.background_color1_stroke) else color
            BackgroundToolbarItem(
                color = color,
                strokeColor = strokeColor,
                onClick = { onColorChanged(color) }
            )
        }
    }
}

@Composable
private fun BackgroundToolbarItem(
    color: Color,
    strokeColor: Color = color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
            .preferredSize(24.dp)
            .border(1.dp, strokeColor, shape = CircleShape)
    )
}

@Composable
private fun Toolbar(
    border: MutableState<Boolean>,
    speed: MutableState<Boolean>,
    backgroundColor: MutableState<Boolean>,
    outlineMasksAndMattes: MutableState<Boolean>,
    applyOpacityToLayers: MutableState<Boolean>,
) {
    ScrollableRow(
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 12.dp),
        modifier = Modifier
            .drawTopBorder()
            .fillMaxWidth()
    ) {
        ToolbarChip(
            iconRes = R.drawable.ic_masks_and_mattes,
            label = stringResource(R.string.toolbar_item_masks),
            isActivated = outlineMasksAndMattes.value,
            onClick = { outlineMasksAndMattes.value = it },
            modifier = Modifier.padding(end = 8.dp)
        )
        ToolbarChip(
            iconRes = R.drawable.ic_layers,
            label = stringResource(R.string.toolbar_item_opacity_layers),
            isActivated = applyOpacityToLayers.value,
            onClick = { applyOpacityToLayers.value = it },
            modifier = Modifier.padding(end = 8.dp)
        )
        ToolbarChip(
            iconRes = R.drawable.ic_color,
            label = stringResource(R.string.toolbar_item_color),
            isActivated = backgroundColor.value,
            onClick = { backgroundColor.value = it },
            modifier = Modifier.padding(end = 8.dp)
        )
        ToolbarChip(
            iconRes = R.drawable.ic_speed,
            label = stringResource(R.string.toolbar_item_speed),
            isActivated = speed.value,
            onClick = { speed.value = it },
            modifier = Modifier.padding(end = 8.dp)
        )
        ToolbarChip(
            iconRes = R.drawable.ic_border,
            label = stringResource(R.string.toolbar_item_border),
            isActivated = border.value,
            onClick = { border.value = it },
            modifier = Modifier.padding(end = 8.dp)
        )
    }
}

@Composable
fun WarningDialog(
    warnings: List<String>,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .preferredWidth(400.dp)
                .heightIn(min = 32.dp, max = 500.dp)
        ) {
            Box(
                alignment = Alignment.TopCenter,
                modifier = Modifier
            ) {
                ScrollableColumn {
                    warnings.forEachIndexed { i, warning ->
                        Text(
                            warning,
                            fontSize = 8.sp,
                            textAlign = TextAlign.Left,
                            modifier = Modifier
                                .fillMaxWidth()
                                .run { if (i != 0) drawTopBorder() else this }
                                .padding(vertical = 12.dp, horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun SpeedToolbarPreview() {
    SpeedToolbar(speed = 1f, onSpeedChanged = {})
}

@Preview(name = "Player")
@Composable
fun PlayerPagePreview() {
    Providers(
        BackPressedDispatcherAmbient provides OnBackPressedDispatcher()
    ) {
        PlayerPage(LottieAnimationSpec.Url("https://lottiefiles.com/download/public/32922"))
    }
}