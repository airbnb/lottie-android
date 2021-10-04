package com.airbnb.lottie.snapshots.tests

import android.content.Context
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.snapshots.SnapshotTestCase
import com.airbnb.lottie.snapshots.SnapshotTestCaseContext
import com.airbnb.lottie.snapshots.consumeAndSnapshotCompositions
import com.airbnb.lottie.snapshots.snapshotComposition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class AssetsTestCase : SnapshotTestCase {
    override suspend fun SnapshotTestCaseContext.run() = coroutineScope {
        val assetsChannel = listAssets()
        val compositionsChannel = parseCompositionsFromAssets(context, assetsChannel)
        repeat(3) {
            launch {
                for ((name, composition) in compositionsChannel) {
                    snapshotComposition(name, composition = composition)
                }
            }
        }
    }
    private fun SnapshotTestCaseContext.listAssets(assets: MutableList<String> = mutableListOf(), pathPrefix: String = ""): List<String> {
        context.assets.list(pathPrefix)?.forEach { animation ->
            val pathWithPrefix = if (pathPrefix.isEmpty()) animation else "$pathPrefix/$animation"
            if (!animation.contains('.')) {
                listAssets(assets, pathWithPrefix)
                return@forEach
            }
            if (!animation.endsWith(".json") && !animation.endsWith(".zip")) return@forEach
            assets += pathWithPrefix
        }
        return assets
    }

    private fun CoroutineScope.parseCompositionsFromAssets(context: Context, assets: List<String>) = produce(
        context = Executors.newSingleThreadExecutor().asCoroutineDispatcher(),
        capacity = 10,
    ) {
        for (asset in assets) {
            val result = LottieCompositionFactory.fromAssetSync(context, asset)
            val composition = result.value ?: throw IllegalArgumentException("Unable to parse $asset.", result.exception)
            send(asset to composition)
        }
    }
}