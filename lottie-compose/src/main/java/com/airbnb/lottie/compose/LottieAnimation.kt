package com.airbnb.lottie.compose

import androidx.annotation.FloatRange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import com.airbnb.lottie.ImageAssetDelegate
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.LottieImageAsset
import com.airbnb.lottie.manager.ImageAssetManager
import com.airbnb.lottie.setImageAssetManager

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
 * @param imageAssetsFolder If you use image assets, you must explicitly specify the folder in assets/ in which
 *                          they are located because bodymovin uses the name filenames across all
 *                          compositions (img_#). Do NOT rename the images themselves.
 *                          If your images are located in src/main/assets/airbnb_loader/ then imageAssetsFolder
 *                          should be set to "airbnb_loader"
 *                          Be wary if you are using many images, however. Lottie is designed to work with
 *                          vector shapes from After Effects. If your images look like they could be
 *                          represented with vector shapes, see if it is possible to convert them to shape
 *                          layers and re-export your animation. Check the documentation at
 *                          http://airbnb.io/lottie for more information about importing shapes from Sketch
 *                          or Illustrator to avoid this.
 * @param imageAssetCallback Use this if you want to return custom images for your animation's image layers.
 *                           This callback will be called on each frame and includes a [LottieImageAsset]
 *                           that has the metadata necessary to know which image is currently being fetched.
 *                           Instead of returning a bitmap from the callback, set the bitmap directly on the
 *                           image asset's bitmap property. Once you do this, the same bitmap will be included
 *                           in subsequent frames and you can choose to reuse it by doing nothing in your callback
 *                           in subsequent frames.
 *                           By default, Lottie also caches [LottieComposition]s and associated bitmaps. If you
 *                           are not reusing this animation, consider setting cacheComposition in [rememberLottieComposition]
 *                           to false.
 *                           Be wary if you are using many images, however. Lottie is designed to work with
 *                           vector shapes from After Effects. If your images look like they could be
 *                           represented with vector shapes, see if it is possible to convert them to shape
 *                           layers and re-export your animation. Check the documentation at
 *                           http://airbnb.io/lottie for more information about importing shapes from Sketch
 *                           or Illustrator to avoid this.
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
 */
@Composable
fun LottieAnimation(
    composition: LottieComposition?,
    @FloatRange(from = 0.0, to = 1.0) progress: Float,
    modifier: Modifier = Modifier,
    imageAssetsFolder: String? = null,
    imageAssetCallback: ((LottieImageAsset) -> Unit)? = null,
    outlineMasksAndMattes: Boolean = false,
    applyOpacityToLayers: Boolean = false,
    enableMergePaths: Boolean = false,
    dynamicProperties: LottieDynamicProperties? = null,
) {
    val drawable = remember { LottieDrawable() }
    var imageAssetManager by remember { mutableStateOf<ImageAssetManager?>(null) }
    var setDynamicProperties: LottieDynamicProperties? by remember { mutableStateOf(null) }
    val imageAssetCallbackState by rememberUpdatedState(imageAssetCallback)

    if (composition == null || composition.duration == 0f) return Box(modifier)

    if (composition.hasImages()) {
        val context = LocalContext.current
        LaunchedEffect(context, composition, imageAssetsFolder, imageAssetCallback) {
            val delegate = ImageAssetDelegate { asset ->
                imageAssetCallbackState?.invoke(asset)
                asset.bitmap
            }
            imageAssetManager = ImageAssetManager(context, imageAssetsFolder, delegate, composition.images, true)
        }
    } else {
        imageAssetManager = null
    }

    Canvas(
        modifier = modifier
            .maintainAspectRatio(composition)
    ) {
        drawIntoCanvas { canvas ->
            withTransform({
                scale(size.width / composition.bounds.width().toFloat(), size.height / composition.bounds.height().toFloat(), Offset.Zero)
            }) {
                drawable.composition = composition
                if (dynamicProperties !== setDynamicProperties) {
                    setDynamicProperties?.removeFrom(drawable)
                    dynamicProperties?.addTo(drawable)
                    setDynamicProperties = dynamicProperties
                }
                drawable.setOutlineMasksAndMattes(outlineMasksAndMattes)
                drawable.isApplyingOpacityToLayersEnabled = applyOpacityToLayers
                drawable.enableMergePathsForKitKatAndAbove(enableMergePaths)
                drawable.setImageAssetManager(imageAssetManager)
                drawable.progress = progress
                drawable.draw(canvas.nativeCanvas)
            }
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
    imageAssetsFolder: String? = null,
    imageAssetCallback: ((LottieImageAsset) -> Unit)? = null,
    outlineMasksAndMattes: Boolean = false,
    applyOpacityToLayers: Boolean = false,
    enableMergePaths: Boolean = false,
    dynamicProperties: LottieDynamicProperties? = null,
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
        imageAssetsFolder,
        imageAssetCallback,
        outlineMasksAndMattes,
        applyOpacityToLayers,
        enableMergePaths,
        dynamicProperties,
    )
}

private fun Modifier.maintainAspectRatio(composition: LottieComposition?): Modifier {
    composition ?: return this
    // TODO: use ContentScale and a transform here
    return this.then(aspectRatio(composition.bounds.width() / composition.bounds.height().toFloat()))
}
