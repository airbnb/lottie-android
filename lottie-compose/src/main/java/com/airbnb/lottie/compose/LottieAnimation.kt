package com.airbnb.lottie.compose

import android.graphics.Matrix
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
import androidx.compose.ui.unit.IntSize
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieDrawable
import kotlin.math.roundToInt

/**
 * This is the base LottieAnimation composable. It takes a composition and renders it at a specific progress.
 *
 * The overloaded version of [LottieAnimation] that handles playback and is sufficient for most use cases.
 *
 * @param composition The composition that will be rendered. To generate a [LottieComposition], you can use
 *                    [rememberLottieComposition].
 * @param progress The progress (between 0 and 1) that should be rendered. If you want to render a specific
 *                 frame, you can use [LottieComposition.getFrameForProgress]. In most cases, you will want
 *                 to use one of th overloaded LottieAnimation composables that drives the animation for you.
 *                 The overloads that have isPlaying as a parameter instead of progress will drive the
 *                 animation automatically. You may want to use this version if you want to drive the animation
 *                 from your own Animatable or via events such as download progress or a gesture.
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
 * @param enableMergePaths Enables experimental merge paths support. Most animations with merge paths will
 *                         want this on but merge path support is more limited than some other rendering
 *                         features so it defaults to off. The only way to know if your animation will work
 *                         well with merge paths or not is to try it. If your animation has merge paths and
 *                         doesn't render correctly, please file an issue.
 * @param dynamicProperties Allows you to change the properties of an animation dynamically. To use them, use
 *                          [rememberLottieDynamicProperties]. Refer to its docs for more info.
 * @param alignment Define where the animation should be placed within this composable if it has a different
 *                  size than this composable.
 * @param contentScale Define how the animation should be scaled if it has a different size than this Composable.
 */
@Composable
fun LottieAnimation(
    composition: LottieComposition?,
    @FloatRange(from = 0.0, to = 1.0) progress: Float,
    modifier: Modifier = Modifier,
    outlineMasksAndMattes: Boolean = false,
    applyOpacityToLayers: Boolean = false,
    enableMergePaths: Boolean = false,
    dynamicProperties: LottieDynamicProperties? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val drawable = remember { LottieDrawable() }
    val matrix = remember { Matrix() }
    var setDynamicProperties: LottieDynamicProperties? by remember { mutableStateOf(null) }

    if (composition == null || composition.duration == 0f) return Box(modifier)

    Canvas(
        modifier = modifier
            .lottieLayout(composition)
    ) {
        drawIntoCanvas { canvas ->
            val compositionSize = Size(composition.bounds.width().toFloat(), composition.bounds.height().toFloat())
            val intSize = IntSize(size.width.roundToInt(), size.height.roundToInt())

            val scale = contentScale.computeScaleFactor(compositionSize, size)
            val translation = alignment.align(compositionSize * scale, intSize, layoutDirection)
            matrix.reset()
            matrix.preTranslate(translation.x.toFloat(), translation.y.toFloat())
            matrix.preScale(scale.scaleX, scale.scaleY)


            drawable.composition = composition
            if (dynamicProperties !== setDynamicProperties) {
                setDynamicProperties?.removeFrom(drawable)
                dynamicProperties?.addTo(drawable)
                setDynamicProperties = dynamicProperties
            }
            drawable.setOutlineMasksAndMattes(outlineMasksAndMattes)
            drawable.isApplyingOpacityToLayersEnabled = applyOpacityToLayers
            drawable.enableMergePathsForKitKatAndAbove(enableMergePaths)
            drawable.progress = progress
            drawable.draw(canvas.nativeCanvas, matrix)
        }
    }
}

/**
 * This is like [LottieAnimation] except that it handles driving the animation via [animateLottieCompositionAsState]
 * instead of taking a raw progress parameter.
 *
 * @see LottieAnimation
 * @see animateLottieCompositionAsState
 */
@Composable
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
    enableMergePaths: Boolean = false,
    dynamicProperties: LottieDynamicProperties? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val progress by animateLottieCompositionAsState(
        composition,
        isPlaying,
        restartOnPlay,
        clipSpec,
        speed,
        iterations,
    )
    LottieAnimation(
        composition,
        progress,
        modifier,
        outlineMasksAndMattes,
        applyOpacityToLayers,
        enableMergePaths,
        dynamicProperties,
        alignment,
        contentScale,
    )
}

private operator fun Size.times(scale: ScaleFactor): IntSize {
    return IntSize((width * scale.scaleX).toInt(), (height * scale.scaleY).toInt())
}