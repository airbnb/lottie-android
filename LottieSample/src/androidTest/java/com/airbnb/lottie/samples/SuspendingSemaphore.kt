package com.airbnb.lottie.samples

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking

/**
 * Semaphore that suspends instead of sleeps.
 */
class SuspendingSemaphore(limit: Int) {
    // The actual object sent in the channel is arbitrary and is unused,
    // we just rely on the buffering mechanism to limit how many items can be acquired at once.
    private val bufferedChannel = Channel<Int>(limit)

    /**
     * Returns when the number of current acquired count goes below the limit.
     */
    fun acquire() {
        runBlocking {
            bufferedChannel.send(0)
        }
    }

    /**
     * Must be matched with a call to [acquire] after the item is done being used.
     */
    fun release() {
        runBlocking {
            bufferedChannel.receive()
        }
    }
}