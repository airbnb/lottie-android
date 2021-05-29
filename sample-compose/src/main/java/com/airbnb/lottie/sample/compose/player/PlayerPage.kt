package com.airbnb.lottie.sample.compose.player

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.airbnb.lottie.ImageAssetDelegate
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.*
import com.airbnb.lottie.sample.compose.BuildConfig
import com.airbnb.lottie.sample.compose.R
import com.airbnb.lottie.sample.compose.composables.DebouncedCircularProgressIndicator
import com.airbnb.lottie.sample.compose.ui.Teal
import com.airbnb.lottie.sample.compose.utils.drawBottomBorder
import com.airbnb.lottie.sample.compose.utils.maybeBackground
import com.airbnb.lottie.sample.compose.utils.maybeDrawBorder
import com.airbnb.lottie.sample.compose.utils.toDummyBitmap
import kotlin.math.ceil
import kotlin.math.roundToInt

@Composable
fun PlayerPage(
    spec: LottieCompositionSpec,
    animationBackgroundColor: Color? = null,
) {
    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val scaffoldState = rememberScaffoldState()
    val isPlaying = remember { mutableStateOf(true) }
    val repeatCount = remember { mutableStateOf(Integer.MAX_VALUE) }
    val speed = remember { mutableStateOf(1f) }
    val outlineMasksAndMattes = remember { mutableStateOf(false) }
    val applyOpacityToLayers = remember { mutableStateOf(false) }
    val enableMergePaths = remember { mutableStateOf(false) }

    var focusMode by remember { mutableStateOf(false) }
    var backgroundColor by remember { mutableStateOf(animationBackgroundColor) }
    var showWarningsDialog by remember { mutableStateOf(false) }

    val borderToolbar = remember { mutableStateOf(false) }
    val speedToolbar = remember { mutableStateOf(false) }
    val backgroundColorToolbar = remember { mutableStateOf(false) }

    val failedMessage = stringResource(R.string.failed_to_load)
    val okMessage = stringResource(R.string.ok)

    val compositionResult = lottieComposition(spec)
    val dummyBitmapStrokeWidth = with(LocalDensity.current) { 3.dp.toPx() }
    val progress = animateLottieComposition(
        compositionResult(),
        isPlaying.value,
        restartOnPlay = false,
        repeatCount = repeatCount.value,
        speed = speed.value,
    ) { isPlaying.value = false }

    LaunchedEffect(compositionResult) {
        if (compositionResult.isFailure) {
            scaffoldState.snackbarHostState.showSnackbar(
                message = failedMessage,
                actionLabel = okMessage,
            )
        }
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
                        onClick = { backPressedDispatcher?.onBackPressed() },
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null
                        )
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
                                contentDescription = null
                            )
                        }
                    }
                    IconButton(
                        onClick = { focusMode = !focusMode },
                    ) {
                        Icon(
                            Icons.Filled.RemoveRedEye,
                            tint = if (focusMode) Teal else Color.Black,
                            contentDescription = null
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
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .maybeBackground(backgroundColor)
                    .fillMaxWidth()
            ) {
                LottieAnimation(
                    compositionResult(),
                    progress.value,
                    imageAssetDelegate = remember { ImageAssetDelegate { it.bitmap ?: it.toDummyBitmap(dummyBitmapStrokeWidth) }},
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center)
                        .maybeDrawBorder(borderToolbar.value)
                )
                if (compositionResult.isLoading) {
                    DebouncedCircularProgressIndicator(
                        color = Teal,
                        modifier = Modifier
                            .size(48.dp)
                    )
                }
            }
            ExpandVisibility(speedToolbar.value && !focusMode) {
                SpeedToolbar(speed)
            }
            ExpandVisibility(!focusMode && backgroundColorToolbar.value) {
                BackgroundColorToolbar(
                    animationBackgroundColor = animationBackgroundColor,
                    onColorChanged = { backgroundColor = it }
                )
            }
            ExpandVisibility(!focusMode) {
                PlayerControlsRow(compositionResult(), progress, isPlaying, speed, repeatCount)
            }
            ExpandVisibility(!focusMode) {
                Toolbar(
                    border = borderToolbar,
                    speed = speedToolbar,
                    backgroundColor = backgroundColorToolbar,
                    outlineMasksAndMattes = outlineMasksAndMattes,
                    applyOpacityToLayers = applyOpacityToLayers,
                    enableMergePaths = enableMergePaths
                )
            }
        }
    }

    if (showWarningsDialog) {
        WarningDialog(warnings = compositionResult()?.warnings ?: emptyList(), onDismiss = { showWarningsDialog = false })
    }
}

@Composable
private fun ColumnScope.ExpandVisibility(visible: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        content()
    }
}

@Composable
private fun PlayerControlsRow(
    composition: LottieComposition?,
    progress: MutableState<Float>,
    isPlaying: MutableState<Boolean>,
    speed: MutableState<Float>,
    repeatCount: MutableState<Int>,
) {
    val totalTime = ((composition?.duration ?: 0L / speed.value) / 1000.0)
    val totalTimeFormatted = ("%.1f").format(totalTime)

    val progressFormatted = ("%.1f").format(progress.value * totalTime)

    val frame = composition?.getFrameForProgress(progress.value)?.roundToInt() ?: 0
    val durationFrames = ceil(composition?.durationFrames ?: 0f).roundToInt()
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { isPlaying.value = !isPlaying.value },
                ) {
                    Icon(
                        if (isPlaying.value) Icons.Filled.Pause
                        else Icons.Filled.PlayArrow,
                        contentDescription = null
                    )
                }
                Text(
                    "$frame/$durationFrames\n${progressFormatted}/$totalTimeFormatted",
                    style = TextStyle(fontSize = 8.sp),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 48.dp, bottom = 8.dp)
                )
            }
            Slider(
                value = progress.value,
                onValueChange = { progress.value = it },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                repeatCount.value = if (repeatCount.value == Integer.MAX_VALUE) 1 else Integer.MAX_VALUE
            }) {
                Icon(
                    Icons.Filled.Repeat,
                    tint = if (repeatCount.value == Integer.MAX_VALUE) Teal else Color.Black,
                    contentDescription = null
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
    speed: MutableState<Float>,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .drawBottomBorder()
            .padding(vertical = 12.dp, horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        ToolbarChip(
            label = "0.5x",
            isActivated = speed.value == 0.5f,
            onClick = { speed.value = 0.5f },
            modifier = Modifier.padding(end = 8.dp)
        )
        ToolbarChip(
            label = "1x",
            isActivated = speed.value == 1f,
            onClick = { speed.value = 1f },
            modifier = Modifier.padding(end = 8.dp)
        )
        ToolbarChip(
            label = "1.5x",
            isActivated = speed.value == 1.5f,
            onClick = { speed.value = 1.5f },
            modifier = Modifier.padding(end = 8.dp)
        )
        ToolbarChip(
            label = "2x",
            isActivated = speed.value == 2f,
            onClick = { speed.value = 2f },
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
            .drawBottomBorder()
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
            .size(24.dp)
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
    enableMergePaths: MutableState<Boolean>,
) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp)
            .padding(bottom = 8.dp)
    ) {
        ToolbarChip(
            iconPainter = painterResource(R.drawable.ic_masks_and_mattes),
            label = stringResource(R.string.toolbar_item_masks),
            isActivated = outlineMasksAndMattes.value,
            onClick = { outlineMasksAndMattes.value = it },
            modifier = Modifier.padding(end = 8.dp)
        )
        ToolbarChip(
            iconPainter = painterResource(R.drawable.ic_layers),
            label = stringResource(R.string.toolbar_item_opacity_layers),
            isActivated = applyOpacityToLayers.value,
            onClick = { applyOpacityToLayers.value = it },
            modifier = Modifier.padding(end = 8.dp)
        )
        ToolbarChip(
            iconPainter = painterResource(R.drawable.ic_color),
            label = stringResource(R.string.toolbar_item_color),
            isActivated = backgroundColor.value,
            onClick = { backgroundColor.value = it },
            modifier = Modifier.padding(end = 8.dp)
        )
        ToolbarChip(
            iconPainter = painterResource(R.drawable.ic_speed),
            label = stringResource(R.string.toolbar_item_speed),
            isActivated = speed.value,
            onClick = { speed.value = it },
            modifier = Modifier.padding(end = 8.dp)
        )
        ToolbarChip(
            iconPainter = painterResource(R.drawable.ic_border),
            label = stringResource(R.string.toolbar_item_border),
            isActivated = border.value,
            onClick = { border.value = it },
            modifier = Modifier.padding(end = 8.dp)
        )
        ToolbarChip(
            iconPainter = rememberVectorPainter(Icons.Default.MergeType),
            label = stringResource(R.string.toolbar_item_merge_paths),
            isActivated = enableMergePaths.value,
            onClick = { enableMergePaths.value = it },
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
                .width(400.dp)
                .heightIn(min = 32.dp, max = 500.dp)
        ) {
            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier
            ) {
                LazyColumn {
                    itemsIndexed(warnings) { i, warning ->
                        Text(
                            warning,
                            fontSize = 8.sp,
                            textAlign = TextAlign.Left,
                            modifier = Modifier
                                .fillMaxWidth()
                                .run { if (i != 0) drawBottomBorder() else this }
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
    val speed = remember { mutableStateOf(1f) }
    SpeedToolbar(speed)
}

@Preview(name = "Player")
@Composable
fun PlayerPagePreview() {
    PlayerPage(LottieCompositionSpec.Url("https://lottiefiles.com/download/public/32922"))
}