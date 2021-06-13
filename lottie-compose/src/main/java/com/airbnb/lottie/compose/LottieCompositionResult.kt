package com.airbnb.lottie.compose

import androidx.compose.runtime.*
import com.airbnb.lottie.LottieComposition
import kotlinx.coroutines.CompletableDeferred

/**
 * A [LottieCompositionResult] subclass is returned from [lottieComposition]. It can be completed with a result
 * or exception only one time.
 *
 * This class implements State<LottieComposition> so you either use it like:
 * ```
 * val compositionResult = lottieComposition(...)
 * // Or
 * val composition by lottieComposition(...)
 * ```
 *
 * @see lottieComposition
 */
@Stable
class LottieCompositionResult internal constructor(): State<LottieComposition?> {
    private val compositionDeferred = CompletableDeferred<LottieComposition>()

    override var value: LottieComposition? by mutableStateOf(null)
        private set

    var error by mutableStateOf<Throwable?>(null)
        private set

    val isLoading by derivedStateOf { value == null && error == null }

    val isComplete by derivedStateOf { value != null || error != null }

    val isFailure by derivedStateOf { error != null }

    val isSuccess by derivedStateOf { value != null }

    /**
     * This can throw if the [LottieComposition] fails to load.
     *
     * These animations should never fail:
     * * [LottieCompositionSpec.RawRes]
     * * [LottieCompositionSpec.Asset]
     * * [LottieCompositionSpec.JsonString]
     *
     * These animations may fail:
     * * [LottieCompositionSpec.Url]
     * * [LottieCompositionSpec.File]
     */
    suspend fun await(): LottieComposition {
        return compositionDeferred.await()
    }

    /**
     * Like [await] but returns null instead of throwing an exception if the animation fails
     * to load.
     */
    suspend fun awaitOrNull(): LottieComposition? {
        return try {
            await()
        } catch (e: Throwable) {
            null
        }
    }

    @Synchronized
    internal fun complete(composition: LottieComposition) {
        if (isComplete) return

        this.value = composition
        compositionDeferred.complete(composition)
    }

    @Synchronized
    internal fun completeExceptionally(error: Throwable) {
        if (isComplete) return

        this.error = error
        compositionDeferred.completeExceptionally(error)
    }
}