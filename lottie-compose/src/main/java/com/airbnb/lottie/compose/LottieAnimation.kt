package com.airbnb.lottie.compose

import android.graphics.Matrix
import android.graphics.Typeface
import androidx.annotation.FloatRange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import com.airbnb.lottie.AsyncUpdates
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.LottieFeatureFlag
import com.airbnb.lottie.RenderMode
import kotlin.math.roundToInt

/**
 * This is the base LottieAnimation composable. It takes a composition and renders it at a specific progress.
 *
 * The overloaded version of [LottieAnimation] that handles playback and is sufficient for most use cases.
 *
 * @param composition The composition that will be rendered. To generate a [LottieComposition], you can use
 *                    [rememberLottieComposition].
 * @param progress A provider for the progress (between 0 and 1) that should be rendered. If you want to render a
 *                         specific frame, you can use [LottieComposition.getFrameForProgress]. In most cases, you will want
 *                         to use one of the overloaded LottieAnimation composables that drives the animation for you.
 *                         The overloads that have isPlaying as a parameter instead of progress will drive the
 *                         animation automatically. You may want to use this version if you want to drive the animation
 *                         from your own Animatable or via events such as download progress or a gesture.
 * @param outlineMasksAndMattes Enable this to debug slow animations by outlining masks and mattes.
 *                              The performance overhead of the masks and mattes will be proportional to the
 *                              surface area of all of the masks/mattes combined.
 *                              DO NOT leave this enabled in production.
 * @param applyOpacityToLayers Sets whether to apply opacity to the each layer instead of shape.
 *                             Opacity is normally applied directly to a shape. In cases where translucent
 *                             shapes overlap, applying opacity to a layer will be more accurate at the
 *                             expense of performance.
 *                             Note: This process is very expensive. The performance impact will be reduced
 *                             when hardware acceleration is enabled.
 * @param applyEffectsToLayers Sets whether to apply effects such as drop shadow and blur to each layer instead of
 *                             individually per shape. In case where effects are applied to shape groups and
 *                             composition layers, this will improve correctness but at a performance cost.
 *                             Note: This process is expensive when hardware acceleration is unavailable.
 * @param applyShadowToLayers  Equivalent to applyEffectsToLayers, but exposed for backwards compatibility.
 * @param enableMergePaths Enables experimental merge paths support. Most animations with merge paths will
 *                         want this on but merge path support is more limited than some other rendering
 *                         features so it defaults to off. The only way to know if your animation will work
 *                         well with merge paths or not is to try it. If your animation has merge paths and
 *                         doesn't render correctly, please file an issue.
 * @param renderMode Allows you to specify whether you want Lottie to use hardware or software rendering.
 *                   Defaults to AUTOMATIC. Refer to [LottieAnimationView.setRenderMode] for more info.
 * @param maintainOriginalImageBounds When true, dynamically set bitmaps will be drawn with the exact bounds of the original animation,
 *                                    regardless of the bitmap size.
 *                                    When false, dynamically set bitmaps will be drawn at the top left of the original image but with its own bounds.
 *                                    Defaults to false.
 * @param dynamicProperties Allows you to change the properties of an animation dynamically. To use them, use
 *                          [rememberLottieDynamicProperties]. Refer to its docs for more info.
 * @param alignment Define where the animation should be placed within this composable if it has a different
 *                  size than this composable.
 * @param contentScale Define how the animation should be scaled if it has a different size than this Composable.
 * @param clipToCompositionBounds Determines whether or not Lottie will clip the animation to the original animation composition bounds.
 *                                The composition bounds refers to the Lottie animation composition, not the Compose composition.
 * @param clipTextToBoundingBox When true, if there is a bounding box set on a text layer (paragraph text), any text
 *                              that overflows past its height will not be drawn.
 * @param fontMap A map of keys to Typefaces. The key can be: "fName", "fFamily", or "fFamily-fStyle" as specified in your Lottie file.
 * @param asyncUpdates When set to true, some parts of animation updates will be done off of the main thread.
 *                     For more details, refer to the docs of [AsyncUpdates].
 * @param safeMode If set to true, draw will be wrapped with a try/catch which will cause Lottie to
 *                     render an empty frame rather than crash your app.
 */
@Composable
@JvmOverloads
fun LottieAnimation(
    composition: LottieComposition?,
    progress: () -> Float,
    modifier: Modifier = Modifier,
    outlineMasksAndMattes: Boolean = false,
    applyOpacityToLayers: Boolean = false,
    applyShadowToLayers: Boolean? = null,
    applyEffectsToLayers: Boolean = true,
    enableMergePaths: Boolean = false,
    renderMode: RenderMode = RenderMode.AUTOMATIC,
    maintainOriginalImageBounds: Boolean = false,
    dynamicProperties: LottieDynamicProperties? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    clipToCompositionBounds: Boolean = true,
    clipTextToBoundingBox: Boolean = false,
    fontMap: Map<String, Typeface>? = null,
    asyncUpdates: AsyncUpdates = AsyncUpdates.AUTOMATIC,
    safeMode: Boolean = false,
) {
    val drawable = remember { LottieDrawable() }
    val matrix = remember { Matrix() }
    var setDynamicProperties: LottieDynamicProperties? by remember(composition) { mutableStateOf(null) }

    if (composition == null || composition.duration == 0f) return Box(modifier)

    val bounds = composition.bounds
    val context = LocalContext.current
    Canvas(
        modifier = modifier
            .lottieSize(bounds.width(), bounds.height())
    ) {
        drawIntoCanvas { canvas ->
            val compositionSize = Size(bounds.width().toFloat(), bounds.height().toFloat())
            val intSize = IntSize(size.width.roundToInt(), size.height.roundToInt())

            val scale = contentScale.computeScaleFactor(compositionSize, size)
            val translation = alignment.align(compositionSize * scale, intSize, layoutDirection)
            matrix.reset()
            matrix.preTranslate(translation.x.toFloat(), translation.y.toFloat())
            matrix.preScale(scale.scaleX, scale.scaleY)

            drawable.enableFeatureFlag(LottieFeatureFlag.MergePathsApi19, enableMergePaths)
            drawable.setSafeMode(safeMode)
            drawable.renderMode = renderMode
            drawable.asyncUpdates = asyncUpdates
            drawable.composition = composition
            drawable.setFontMap(fontMap)
            if (dynamicProperties !== setDynamicProperties) {
                setDynamicProperties?.removeFrom(drawable)
                dynamicProperties?.addTo(drawable)
                setDynamicProperties = dynamicProperties
            }
            drawable.setOutlineMasksAndMattes(outlineMasksAndMattes)
            drawable.isApplyingOpacityToLayersEnabled = applyOpacityToLayers
            drawable.isApplyingEffectsToLayersEnabled = applyEffectsToLayers
            if (applyShadowToLayers != null) {
                drawable.isApplyingEffectsToLayersEnabled = applyShadowToLayers;
            }
            drawable.maintainOriginalImageBounds = maintainOriginalImageBounds
            drawable.clipToCompositionBounds = clipToCompositionBounds
            drawable.clipTextToBoundingBox = clipTextToBoundingBox
            val markerForAnimationsDisabled = drawable.markerForAnimationsDisabled
            if (!drawable.animationsEnabled(context) && markerForAnimationsDisabled != null) {
                drawable.progress = markerForAnimationsDisabled.startFrame
            } else {
                drawable.progress = progress()
            }
            drawable.setBounds(0, 0, bounds.width(), bounds.height())
            drawable.draw(canvas.nativeCanvas, matrix)
        }
    }
}

/**
 * This is like [LottieAnimation] except that it takes a raw progress parameter instead of taking a progress provider.
 *
 * @see LottieAnimation
 */
@Composable
@Deprecated("Pass progress as a lambda instead of a float. This overload will be removed in the next release.")
fun LottieAnimation(
    composition: LottieComposition?,
    @FloatRange(from = 0.0, to = 1.0) progress: Float,
    modifier: Modifier = Modifier,
    outlineMasksAndMattes: Boolean = false,
    applyOpacityToLayers: Boolean = false,
    applyEffectsToLayers: Boolean = true,
    enableMergePaths: Boolean = false,
    renderMode: RenderMode = RenderMode.AUTOMATIC,
    maintainOriginalImageBounds: Boolean = false,
    dynamicProperties: LottieDynamicProperties? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    clipToCompositionBounds: Boolean = true,
    safeMode: Boolean = false,
    asyncUpdates: AsyncUpdates = AsyncUpdates.AUTOMATIC,
) {
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier,
        outlineMasksAndMattes = outlineMasksAndMattes,
        applyOpacityToLayers = applyOpacityToLayers,
        applyEffectsToLayers = applyEffectsToLayers,
        enableMergePaths = enableMergePaths,
        renderMode = renderMode,
        maintainOriginalImageBounds = maintainOriginalImageBounds,
        dynamicProperties = dynamicProperties,
        alignment = alignment,
        contentScale = contentScale,
        clipToCompositionBounds = clipToCompositionBounds,
        asyncUpdates = asyncUpdates,
        safeMode = safeMode
    )
}

/**
 * This is like [LottieAnimation] except that it handles driving the animation via [animateLottieCompositionAsState]
 * instead of taking a progress provider.
 *
 * @see LottieAnimation
 * @see animateLottieCompositionAsState
 */
@Composable
@JvmOverloads
fun LottieAnimation(
    composition: LottieComposition?,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true,
    restartOnPlay: Boolean = true,
    clipSpec: LottieClipSpec? = null,
    speed: Float = 1f,
    iterations: Int = 1,
    outlineMasksAndMattes: Boolean = false,
    applyOpacityToLayers: Boolean = false,
    applyEffectsToLayers: Boolean = true,
    enableMergePaths: Boolean = false,
    renderMode: RenderMode = RenderMode.AUTOMATIC,
    reverseOnRepeat: Boolean = false,
    maintainOriginalImageBounds: Boolean = false,
    dynamicProperties: LottieDynamicProperties? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    clipToCompositionBounds: Boolean = true,
    clipTextToBoundingBox: Boolean = false,
    fontMap: Map<String, Typeface>? = null,
    safeMode: Boolean = false,
    asyncUpdates: AsyncUpdates = AsyncUpdates.AUTOMATIC,
) {
    val progress by animateLottieCompositionAsState(
        composition,
        isPlaying,
        restartOnPlay,
        reverseOnRepeat,
        clipSpec,
        speed,
        iterations,
    )
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier,
        outlineMasksAndMattes = outlineMasksAndMattes,
        applyOpacityToLayers = applyOpacityToLayers,
        applyEffectsToLayers = applyEffectsToLayers,
        enableMergePaths = enableMergePaths,
        renderMode = renderMode,
        maintainOriginalImageBounds = maintainOriginalImageBounds,
        dynamicProperties = dynamicProperties,
        alignment = alignment,
        contentScale = contentScale,
        clipToCompositionBounds = clipToCompositionBounds,
        clipTextToBoundingBox = clipTextToBoundingBox,
        fontMap = fontMap,
        asyncUpdates = asyncUpdates,
        safeMode = safeMode
    )
}

private operator fun Size.times(scale: ScaleFactor): IntSize {
    return IntSize((width * scale.scaleX).toInt(), (height * scale.scaleY).toInt())
}
