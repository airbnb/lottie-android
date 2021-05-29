package com.airbnb.lottie.compose

import androidx.compose.runtime.*
import com.airbnb.lottie.LottieComposition
import kotlinx.coroutines.CompletableDeferred

/**
 * A [LottieCompositionResult] subclass is returned from [rememberLottieComposition].
 *
 * To access a [LottieComposition] from a [LottieCompositionResult], call [invoke] directly or as an operator.
 */
class LottieCompositionResult internal constructor(): State<LottieComposition?> {
    private val compositionDeferred = CompletableDeferred<LottieComposition>()
    private var composition by mutableStateOf<LottieComposition?>(null)
    private var error by mutableStateOf<Throwable?>(null)

    val isLoading by derivedStateOf { composition == null && error == null }

    val isError by derivedStateOf { error != null }

    suspend fun await() = compositionDeferred.await()

    @Synchronized
    internal fun complete(composition: LottieComposition) {
        this.composition = composition
        compositionDeferred.complete(composition)
    }

    @Synchronized
    internal fun completeExceptionally(error: Throwable) {
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

    override val value: LottieComposition?
        get() = invoke()
}