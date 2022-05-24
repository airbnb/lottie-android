package com.airbnb.lottie.sample.compose.player

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.LottieAnimatable
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.resetToBeginning
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

@Stable
class PlayerPageState(backgroundColor: Color?) {
    val animatable = LottieAnimatable()

    var backgroundColor by mutableStateOf(backgroundColor)
    var outlineMasksAndMattes by mutableStateOf(false)
    var applyOpacityToLayers by mutableStateOf(false)
    var enableMergePaths by mutableStateOf(false)
    var focusMode by mutableStateOf(false)
    var showWarningsDialog by mutableStateOf(false)

    var borderToolbar by mutableStateOf(false)
    var speedToolbar by mutableStateOf(false)
    var backgroundColorToolbar by mutableStateOf(false)

    var progressSliderGesture: Float? by mutableStateOf(null)
    var shouldPlay by mutableStateOf(true)
    var targetSpeed by mutableStateOf(1f)
    var shouldLoop by mutableStateOf(true)
}

@Composable
fun PlayerPage(
    spec: LottieCompositionSpec,
    animationBackgroundColor: Color? = null,
) {
    val scaffoldState = rememberScaffoldState()
    val state = remember { PlayerPageState(animationBackgroundColor) }

    val failedMessage = stringResource(R.string.failed_to_load)
    val okMessage = stringResource(R.string.ok)

    val compositionResult = rememberLottieComposition(spec)

    LaunchedEffect(compositionResult.isFailure) {
        if (!compositionResult.isFailure) return@LaunchedEffect
        scaffoldState.snackbarHostState.showSnackbar(
            message = failedMessage,
            actionLabel = okMessage,
        )
    }

    val dummyBitmapStrokeWidth = with(LocalDensity.current) { 3.dp.toPx() }
    LaunchedEffect(compositionResult.value) {
        val composition = compositionResult.value ?: return@LaunchedEffect
        for (asset in composition.images.values) {
            if (asset.bitmap != null) continue
            asset.bitmap = asset.toDummyBitmap(dummyBitmapStrokeWidth)
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = { PlayerPageTopAppBar(state, compositionResult.value) },
    ) {
        PlayerPageContent(
            state,
            compositionResult.value,
            compositionResult.isLoading,
            animationBackgroundColor,
        )
    }

    if (state.showWarningsDialog) {
        WarningDialog(warnings = compositionResult.value?.warnings ?: emptyList(), onDismiss = { state.showWarningsDialog = false })
    }
}

@Composable
private fun ColumnScope.ExpandVisibility(
    visible: Boolean,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        content()
    }
}

@Composable
private fun PlayerPageTopAppBar(
    state: PlayerPageState,
    composition: LottieComposition?,
) {
    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

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
            if (composition?.warnings?.isNotEmpty() == true) {
                IconButton(
                    onClick = { state.showWarningsDialog = true }
                ) {
                    Icon(
                        Icons.Filled.Warning,
                        tint = Color.Black,
                        contentDescription = null
                    )
                }
            }
            IconButton(
                onClick = { state.focusMode = !state.focusMode },
            ) {
                Icon(
                    Icons.Filled.RemoveRedEye,
                    tint = if (state.focusMode) Teal else Color.Black,
                    contentDescription = null
                )
            }
        }
    )
}

@Composable
fun PlayerPageContent(
    state: PlayerPageState,
    composition: LottieComposition?,
    isLoading: Boolean,
    animationBackgroundColor: Color?,
) {
    LaunchedEffect(
        composition,
        state.shouldPlay,
        state.targetSpeed,
        state.shouldLoop,
        state.progressSliderGesture,
    ) {
        composition ?: return@LaunchedEffect
        state.progressSliderGesture?.let { p ->
            state.animatable.snapTo(composition, p, resetLastFrameNanos = true)
            return@LaunchedEffect
        }
        if (state.shouldPlay) {
            if (!state.animatable.isPlaying && state.animatable.isAtEnd) {
                state.animatable.resetToBeginning()
            }
            state.animatable.animate(
                composition,
                iterations = if (state.shouldLoop) LottieConstants.IterateForever else 1,
                initialProgress = state.animatable.progress,
                speed = state.targetSpeed,
                continueFromPreviousAnimate = state.animatable.isPlaying,
            )
            state.shouldPlay = false
        }
    }

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxHeight()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .maybeBackground(state.backgroundColor)
                .fillMaxWidth()
        ) {
            PlayerPageLottieAnimation(
                composition,
                { state.animatable.progress },
                modifier = Modifier
                    // TODO: figure out how maxWidth can play nice with the aspectRatio modifier inside of LottieAnimation.
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .maybeDrawBorder(state.borderToolbar)
            )
            if (isLoading) {
                DebouncedCircularProgressIndicator(
                    color = Teal,
                    modifier = Modifier
                        .size(48.dp)
                )
            }
        }
        ExpandVisibility(state.speedToolbar && !state.focusMode) {
            SpeedToolbar(state)
        }
        ExpandVisibility(!state.focusMode && state.backgroundColorToolbar) {
            BackgroundColorToolbar(
                animationBackgroundColor = animationBackgroundColor,
                onColorChanged = { state.backgroundColor = it }
            )
        }
        ExpandVisibility(!state.focusMode) {
            PlayerControlsRow(state, composition)
        }
        ExpandVisibility(!state.focusMode) {
            Toolbar(state)
        }
    }
}

@Composable
private fun PlayerPageLottieAnimation(
    composition: LottieComposition?,
    progressProvider: () -> Float,
    modifier: Modifier = Modifier,
) {
    LottieAnimation(
        composition,
        progressProvider,
        modifier = modifier,
    )
}

@Composable
private fun PlayerControlsRow(
    state: PlayerPageState,
    composition: LottieComposition?,
) {
    val totalTime = ((composition?.duration ?: 0L / state.animatable.speed) / 1000.0)
    val totalTimeFormatted = ("%.1f").format(totalTime)

    val progressFormatted = ("%.1f").format(state.animatable.progress * totalTime)

    val frame = composition?.getFrameForProgress(state.animatable.progress)?.roundToInt() ?: 0
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
                    onClick = { state.shouldPlay = !state.shouldPlay },
                ) {
                    Icon(
                        if (state.animatable.isPlaying) Icons.Filled.Pause
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
                value = state.progressSliderGesture ?: state.animatable.progress,
                onValueChange = { state.progressSliderGesture = it },
                onValueChangeFinished = { state.progressSliderGesture = null },
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = { state.shouldLoop = !state.shouldLoop },
            ) {
                Icon(
                    Icons.Filled.Repeat,
                    tint = if (state.animatable.iterations == 1) Color.Black else Teal,
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
private fun SpeedToolbar(state: PlayerPageState) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .drawBottomBorder()
            .padding(vertical = 12.dp, horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        ToolbarChip(
            label = "0.5x",
            isActivated = state.animatable.speed == 0.5f,
            onClick = { state.targetSpeed = 0.5f },
            modifier = Modifier.padding(end = 8.dp)
        )
        ToolbarChip(
            label = "1x",
            isActivated = state.animatable.speed == 1f,
            onClick = { state.targetSpeed = 1f },
            modifier = Modifier.padding(end = 8.dp)
        )
        ToolbarChip(
            label = "1.5x",
            isActivated = state.animatable.speed == 1.5f,
            onClick = { state.targetSpeed = 1.5f },
            modifier = Modifier.padding(end = 8.dp)
        )
        ToolbarChip(
            label = "2x",
            isActivated = state.animatable.speed == 2f,
            onClick = { state.targetSpeed = 2f },
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
private fun Toolbar(state: PlayerPageState) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp)
            .padding(bottom = 8.dp)
    ) {
        ToolbarChip(
            iconPainter = painterResource(R.drawable.ic_masks_and_mattes),
            label = stringResource(R.string.toolbar_item_masks),
            isActivated = state.outlineMasksAndMattes,
            onClick = { state.outlineMasksAndMattes = it },
            modifier = Modifier.padding(end = 8.dp)
        )
        ToolbarChip(
            iconPainter = painterResource(R.drawable.ic_layers),
            label = stringResource(R.string.toolbar_item_opacity_layers),
            isActivated = state.applyOpacityToLayers,
            onClick = { state.applyOpacityToLayers = it },
            modifier = Modifier.padding(end = 8.dp)
        )
        ToolbarChip(
            iconPainter = painterResource(R.drawable.ic_color),
            label = stringResource(R.string.toolbar_item_color),
            isActivated = state.backgroundColorToolbar,
            onClick = { state.backgroundColorToolbar = it },
            modifier = Modifier.padding(end = 8.dp)
        )
        ToolbarChip(
            iconPainter = painterResource(R.drawable.ic_speed),
            label = stringResource(R.string.toolbar_item_speed),
            isActivated = state.speedToolbar,
            onClick = { state.speedToolbar = it },
            modifier = Modifier.padding(end = 8.dp)
        )
        ToolbarChip(
            iconPainter = painterResource(R.drawable.ic_border),
            label = stringResource(R.string.toolbar_item_border),
            isActivated = state.borderToolbar,
            onClick = { state.borderToolbar = it },
            modifier = Modifier.padding(end = 8.dp)
        )
        ToolbarChip(
            iconPainter = rememberVectorPainter(Icons.Default.MergeType),
            label = stringResource(R.string.toolbar_item_merge_paths),
            isActivated = state.enableMergePaths,
            onClick = { state.enableMergePaths = it },
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
    val state = remember { PlayerPageState(null) }
    SpeedToolbar(state)
}

@Preview(name = "Player")
@Composable
fun PlayerPagePreview() {
    PlayerPage(LottieCompositionSpec.Url("https://lottiefiles.com/download/public/32922"))
}