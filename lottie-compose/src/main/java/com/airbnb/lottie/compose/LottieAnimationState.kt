package com.airbnb.lottie.compose

import androidx.compose.runtime.*
import com.airbnb.lottie.ImageAssetDelegate
import com.airbnb.lottie.LottieDrawable

/**
 * Create a [LottieAnimationState] and remember it
 *
 * @param autoPlay Initial value for [LottieAnimationState.isPlaying]
 * @param repeatCount Initial value for [LottieAnimationState.repeatCount]
 * @param initialProgress Initial value for [LottieAnimationState.progress]
 * @param enableMergePaths Initial value for [LottieAnimationState.enableMergePaths]
 * @param imageAssetsFolder Initial value for [LottieAnimationState.imageAssetsFolder]
 * @param imageAssetDelegate Initial value for [LottieAnimationState.imageAssetDelegate]
 */
@Composable
fun rememberLottieAnimationState(
    autoPlay: Boolean = true,
    repeatCount: Int = 0,
    initialProgress: Float = 0f,
    enableMergePaths: Boolean = true,
    imageAssetsFolder: String? = null,
    imageAssetDelegate: ImageAssetDelegate? = null,

): LottieAnimationState {
    // Use rememberSavedInstanceState so you can pause/resume animations
    return remember(repeatCount, autoPlay) {
        LottieAnimationState(
            isPlaying = autoPlay,
            repeatCount = repeatCount,
            initialProgress = initialProgress,
            enableMergePaths = enableMergePaths,
            imageAssetsFolder = imageAssetsFolder,
            imageAssetDelegate = imageAssetDelegate,
        )
    }
}

/**
 * State of the [LottieAnimation] composable
 *
 * @param isPlaying Initial value for [isPlaying]
 * @param repeatCount Initial value for [repeatCount]
 * @param initialProgress Initial value for [progress]
 * @param enableMergePaths Initial value for [enableMergePaths]
 * @param imageAssetsFolder Initial value for [LottieAnimationState.imageAssetsFolder]
 * @param imageAssetDelegate Initial value for [LottieAnimationState.imageAssetDelegate]
 *
 * @see rememberLottieAnimationState
 */
class LottieAnimationState(
    isPlaying: Boolean,
    repeatCount: Int = 0,
    initialProgress: Float = 0f,
    enableMergePaths: Boolean = true,
    imageAssetsFolder: String? = null,
    imageAssetDelegate: ImageAssetDelegate? = null,
) {
    var progress by mutableStateOf(initialProgress)

    // TODO: make this public
    private var _frame = mutableStateOf(0)
    val frame: Int by _frame

    /**
     * Whether the animation is currently playing.
     */
    var isPlaying by mutableStateOf(isPlaying)

    /**
     * How many times the animation will be played. Use [Int.MAX_VALUE] for
     * infinite repetitions.
     */
    var repeatCount by mutableStateOf(repeatCount)

    var speed by mutableStateOf(1f)

    /**
     * Enable this to debug slow animations by outlining masks and mattes. The performance overhead of the masks and mattes will
     * be proportional to the surface area of all of the masks/mattes combined.
     * <p>
     * DO NOT leave this enabled in production.
     */
    var outlineMasksAndMattes by mutableStateOf(false)


    /**
     * Sets whether to apply opacity to the each layer instead of shape.
     * <p>
     * Opacity is normally applied directly to a shape. In cases where translucent shapes overlap, applying opacity to a layer will be more accurate
     * at the expense of performance.
     * <p>
     * The default value is false.
     * <p>
     * Note: This process is very expensive and will incur additional performance overhead.
     */
    var applyOpacityToLayers by mutableStateOf(false)

    /**
     * Enable this to get merge path support.
     * <p>
     * Merge paths currently don't work if the the operand shape is entirely contained within the
     * first shape. If you need to cut out one shape from another shape, use an even-odd fill type
     * instead of using merge paths.
     * <p>
     * If your animation contains merge paths and you are encountering rendering issues, disabling
     * merge paths might help.
     */
    var enableMergePaths by mutableStateOf(enableMergePaths)

    /**
     * If you use image assets, you must explicitly specify the folder in assets/ in which they are
     * located because bodymovin uses the name filenames across all compositions (img_#).
     * Do NOT rename the images themselves.
     * <p>
     * If your images are located in src/main/assets/airbnb_loader/ then set this to "airbnb_loader".
     * <p>
     * <p>
     * Be wary if you are using many images, however. Lottie is designed to work with vector shapes
     * from After Effects. If your images look like they could be represented with vector shapes,
     * see if it is possible to convert them to shape layers and re-export your animation. Check
     * the documentation at https://airbnb.io/lottie for more information about importing shapes from
     * Sketch or Illustrator to avoid this.
     */
    var imageAssetsFolder by mutableStateOf(imageAssetsFolder)

    /**
     * Use this if you can't bundle images with your app. This may be useful if you download the
     * animations from the network or have the images saved to an SD Card. In that case, Lottie
     * will defer the loading of the bitmap to this delegate.
     * <p>
     * Be wary if you are using many images, however. Lottie is designed to work with vector shapes
     * from After Effects. If your images look like they could be represented with vector shapes,
     * see if it is possible to convert them to shape layers and re-export your animation. Check
     * the documentation at https://airbnb.io/lottie for more information about importing shapes from
     * Sketch or Illustrator to avoid this.
     */
    var imageAssetDelegate by mutableStateOf(imageAssetDelegate)

    internal fun updateFrame(frame: Int) {
        _frame.value = frame
    }

    fun toggleIsPlaying() {
        isPlaying = !isPlaying
    }

    internal fun applyTo(drawable: LottieDrawable) {
        drawable.progress = progress
        drawable.setOutlineMasksAndMattes(outlineMasksAndMattes)
        drawable.isApplyingOpacityToLayersEnabled = applyOpacityToLayers
        drawable.enableMergePathsForKitKatAndAbove(enableMergePaths)
    }
}