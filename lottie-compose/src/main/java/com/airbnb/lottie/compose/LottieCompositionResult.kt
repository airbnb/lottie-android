package com.airbnb.lottie.compose

import androidx.compose.runtime.*
import com.airbnb.lottie.LottieComposition
import kotlinx.coroutines.CompletableDeferred

/**
 * A [LottieCompositionResult] subclass is returned from [lottieComposition].
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
class LottieCompositionResult internal constructor(): State<LottieComposition?> {
    private val compositionDeferred = CompletableDeferred<LottieComposition>()
    var composition by mutableStateOf<LottieComposition?>(null)
        private set
    var error by mutableStateOf<Throwable?>(null)
        private set

    override val value: LottieComposition?
        get() = invoke()

    val isLoading by derivedStateOf { composition == null && error == null }

    val isComplete by derivedStateOf { composition != null || error != null }

    val isFailure by derivedStateOf { error != null }

    val isSuccess by derivedStateOf { composition != null }

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

        this.composition = composition
        compositionDeferred.complete(composition)
    }

    @Synchronized
    internal fun completeExceptionally(error: Throwable) {
        if (isComplete) return

        this.error = error
        compositionDeferred.completeExceptionally(error)
    }

    /**
     * This is an operator so an instance of [LottieCompositionResult] can be called like a function
     * instead of calling [invoke] by name.
     *
     * @return the composition if successful or null of it is is still loading or failed to load.
     */
    operator fun invoke(): LottieComposition? {
        return composition
    }
}