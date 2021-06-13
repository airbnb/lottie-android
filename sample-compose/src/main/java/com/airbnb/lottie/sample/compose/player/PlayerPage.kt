package com.airbnb.lottie.sample.compose.player

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
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
import com.airbnb.lottie.sample.compose.utils.*
import kotlinx.coroutines.flow.collect
import kotlin.math.ceil
import kotlin.math.roundToInt

@Stable
class PlayerPageState {
    var isPlaying by mutableStateOf(true)
    var repeatCount by mutableStateOf(LottieConstants.RepeatForever)
    var speed by mutableStateOf(1f)
    var outlineMasksAndMattes by mutableStateOf(false)
    var applyOpacityToLayers by mutableStateOf(false)
    var enableMergePaths by mutableStateOf(false)
    var focusMode by mutableStateOf(false)
    var showWarningsDialog by mutableStateOf(false)

    var borderToolbar by mutableStateOf(false)
    var speedToolbar by mutableStateOf(false)
    var backgroundColorToolbar by mutableStateOf(false)
}

@Composable
fun PlayerPage(
    spec: LottieCompositionSpec,
    animationBackgroundColor: Color? = null,
) {
    val scaffoldState = rememberScaffoldState()
    val state = remember { PlayerPageState() }

    val failedMessage = stringResource(R.string.failed_to_load)
    val okMessage = stringResource(R.string.ok)

    val compositionResult = lottieComposition(spec)

    LaunchedEffect(compositionResult.isFailure) {
        if (!compositionResult.isFailure) return@LaunchedEffect
        scaffoldState.snackbarHostState.showSnackbar(
            message = failedMessage,
            actionLabel = okMessage,
        )
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = { PlayerPageTopAppBar(state, compositionResult()) },
    ) {
        PlayerPageContent(state, compositionResult, animationBackgroundColor)
    }

    if (state.showWarningsDialog) {
        WarningDialog(warnings = compositionResult()?.warnings ?: emptyList(), onDismiss = { state.showWarningsDialog = false })
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
    compositionResult: LottieCompositionResult,
    animationBackgroundColor: Color?,
) {
    var backgroundColor by remember(animationBackgroundColor) { mutableStateOf(animationBackgroundColor) }
    val dummyBitmapStrokeWidth = with(LocalDensity.current) { 3.dp.toPx() }
    val imageAssetDelegate = remember(compositionResult()) {
        if (compositionResult()?.hasEmbeddedBitmaps == true) {
            null
        } else {
            ImageAssetDelegate { if (it.hasBitmap()) null else it.toDummyBitmap(dummyBitmapStrokeWidth) }
        }
    }
    val progress = animateLottieComposition(
        compositionResult(),
        state.isPlaying,
        restartOnPlay = false,
        repeatCount = state.repeatCount,
        speed = state.speed,
    ) { state.isPlaying = false }

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
                imageAssetDelegate = imageAssetDelegate,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
                    .maybeDrawBorder(state.borderToolbar)
            )
            if (compositionResult.isLoading) {
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
                onColorChanged = { backgroundColor = it }
            )
        }
        ExpandVisibility(!state.focusMode) {
            PlayerControlsRow(compositionResult(), progress, state)
        }
        ExpandVisibility(!state.focusMode) {
            Toolbar(state)
        }
    }
}

@Composable
private fun PlayerControlsRow(
    composition: LottieComposition?,
    progress: MutableState<Float>,
    state: PlayerPageState,
) {
    val totalTime = ((composition?.duration ?: 0L / state.speed) / 1000.0)
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
                    onClick = { state.isPlaying = !state.isPlaying },
                ) {
                    Icon(
                        if (state.isPlaying) Icons.Filled.Pause
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
            AnimationSlider(
                progress,
                state,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    state.repeatCount = if (state.repeatCount == LottieConstants.RepeatForever) 1 else LottieConstants.RepeatForever
                },
            ) {
                Icon(
                    Icons.Filled.Repeat,
                    tint = if (state.repeatCount == LottieConstants.RepeatForever) Teal else Color.Black,
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
private fun AnimationSlider(
    progress: MutableState<Float>,
    state: PlayerPageState,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isInteracting = isInteracting(interactionSource)
    var wasPlayingOnInteractionStart by remember { mutableStateOf(true) }

    LaunchedEffect(isInteracting) {
        state.isPlaying = when (isInteracting) {
            true -> {
                wasPlayingOnInteractionStart = state.isPlaying
                false
            }
            false -> {
                wasPlayingOnInteractionStart
            }
        }
    }

    Slider(
        value = progress.value,
        interactionSource = interactionSource,
        onValueChange = { progress.value = it },
        modifier = modifier,
    )
}

@Composable
fun isInteracting(interactionSource: MutableInteractionSource): Boolean {
    val interactions = remember { mutableStateListOf<Interaction>() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> interactions.add(interaction)
                is PressInteraction.Release -> interactions.remove(interaction.press)
                is PressInteraction.Cancel -> interactions.remove(interaction.press)
                is DragInteraction.Start -> interactions.add(interaction)
                is DragInteraction.Stop -> interactions.remove(interaction.start)
                is DragInteraction.Cancel -> interactions.remove(interaction.start)
            }
        }
    }

    return interactions.isNotEmpty()
}

@Composable
private fun SpeedToolbar(
    state: PlayerPageState,
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
            isActivated = state.speed == 0.5f,
            onClick = { state.speed = 0.5f },
            modifier = Modifier.padding(end = 8.dp)
        )
        ToolbarChip(
            label = "1x",
            isActivated = state.speed == 1f,
            onClick = { state.speed = 1f },
            modifier = Modifier.padding(end = 8.dp)
        )
        ToolbarChip(
            label = "1.5x",
            isActivated = state.speed == 1.5f,
            onClick = { state.speed = 1.5f },
            modifier = Modifier.padding(end = 8.dp)
        )
        ToolbarChip(
            label = "2x",
            isActivated = state.speed == 2f,
            onClick = { state.speed = 2f },
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
    val state = remember { PlayerPageState() }
    state.speed = 1f
    SpeedToolbar(state)
}

@Preview(name = "Player")
@Composable
fun PlayerPagePreview() {
    PlayerPage(LottieCompositionSpec.Url("https://lottiefiles.com/download/public/32922"))
}