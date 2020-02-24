package com.airbnb.lottie.samples

import android.animation.ValueAnimator
import android.app.Application
import android.net.Uri
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieTask
import com.airbnb.lottie.samples.model.CompositionArgs
import com.airbnb.mvrx.*
import java.io.FileInputStream

data class PlayerState(
        val composition: Async<LottieComposition> = Uninitialized,
        val controlsVisible: Boolean = true,
        val controlBarVisible: Boolean = true,
        val renderGraphVisible: Boolean = false,
        val borderVisible: Boolean = false,
        val backgroundColorVisible: Boolean = false,
        val scaleVisible: Boolean = false,
        val speedVisible: Boolean = false,
        val trimVisible: Boolean = false,
        val useHardwareAcceleration: Boolean = false,
        val useMergePaths: Boolean = false,
        val minFrame: Int = 0,
        val maxFrame: Int = 0,
        val speed: Float = 1f,
        val repeatCount: Int = ValueAnimator.INFINITE
) : MvRxState

class PlayerViewModel(
        initialState: PlayerState,
        private val application: Application
) : MvRxViewModel<PlayerState>(initialState) {

    fun fetchAnimation(args: CompositionArgs) {
        val url = args.url ?: args.animationDataV2?.file ?: args.animationData?.lottieLink

        when {
            url != null -> LottieCompositionFactory.fromUrl(application, url, null)
            args.fileUri != null -> taskForUri(args.fileUri)
            args.asset != null -> LottieCompositionFactory.fromAsset(application, args.asset, null)
            else -> error("Don't know how to fetch animation for $args")
        }
                .addListener {
                    setState {
                        copy(composition = Success(it), minFrame = it.startFrame.toInt(), maxFrame = it.endFrame.toInt())
                    }
                }
                .addFailureListener { setState { copy(composition = Fail(it)) } }
    }

    private fun taskForUri(uri: Uri): LottieTask<LottieComposition> {
        val fis = when (uri.scheme) {
            "file" -> FileInputStream(uri.path ?: error("File has no path!"))
            "content" -> application.contentResolver.openInputStream(uri)
            else -> error("Unknown scheme ${uri.scheme}")
        }

        return LottieCompositionFactory.fromJsonInputStream(fis, null)
    }

    fun toggleRenderGraphVisible() = setState { copy(renderGraphVisible = !renderGraphVisible) }

    fun toggleBorderVisible() = setState { copy(borderVisible = !borderVisible) }

    fun toggleBackgroundColorVisible() = setState { copy(backgroundColorVisible = !backgroundColorVisible) }

    fun setBackgroundColorVisible(visible: Boolean) = setState { copy(backgroundColorVisible = visible) }

    fun toggleScaleVisible() = setState { copy(scaleVisible = !scaleVisible) }

    fun setScaleVisible(visible: Boolean) = setState { copy(scaleVisible = visible) }

    fun toggleSpeedVisible() = setState { copy(speedVisible = !speedVisible) }

    fun setSpeedVisible(visible: Boolean) = setState { copy(speedVisible = visible) }

    fun toggleTrimVisible() = setState { copy(trimVisible = !trimVisible) }

    fun setTrimVisible(visible: Boolean) = setState { copy(trimVisible = visible) }

    fun toggleHardwareAcceleration() = setState { copy(useHardwareAcceleration = !useHardwareAcceleration) }

    fun toggleMergePaths() = setState { copy(useMergePaths = !useMergePaths) }

    fun setMinFrame(minFrame: Int) = setState {
        copy(minFrame = Math.max(minFrame, composition()?.startFrame?.toInt() ?: 0))
    }

    fun setMaxFrame(maxFrame: Int) = setState {
        copy(maxFrame = Math.min(maxFrame, composition()?.endFrame?.toInt() ?: 0))
    }

    fun setSpeed(speed: Float) = setState { copy(speed = speed) }

    fun toggleLoop() = setState { copy(repeatCount = if (repeatCount == ValueAnimator.INFINITE) 0 else ValueAnimator.INFINITE) }

    fun setDistractionFree(distractionFree: Boolean) = setState {
        copy(
                controlsVisible = !distractionFree,
                controlBarVisible = !distractionFree,
                renderGraphVisible = false,
                borderVisible = false,
                backgroundColorVisible = false,
                scaleVisible = false,
                speedVisible = false,
                trimVisible = false
        )
    }

    companion object : MvRxViewModelFactory<PlayerViewModel, PlayerState> {
        override fun create(viewModelContext: ViewModelContext, state: PlayerState): PlayerViewModel? {
            return PlayerViewModel(state, viewModelContext.app())
        }
    }
}