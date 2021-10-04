package com.airbnb.lottie.snapshots.tests

import android.content.res.Configuration
import android.graphics.Canvas
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.model.LottieCompositionCache
import com.airbnb.lottie.snapshots.R
import com.airbnb.lottie.snapshots.SnapshotTestCase
import com.airbnb.lottie.snapshots.SnapshotTestCaseContext
import com.airbnb.lottie.snapshots.log

class NightModeTestCase : SnapshotTestCase {
    override suspend fun SnapshotTestCaseContext.run() {
        var newConfig = Configuration(context.resources.configuration)
        newConfig.uiMode = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()
        newConfig.uiMode = newConfig.uiMode or Configuration.UI_MODE_NIGHT_NO
        val dayContext = context.createConfigurationContext(newConfig)
        var result = LottieCompositionFactory.fromRawResSync(dayContext, R.raw.day_night)
        var composition = result.value!!
        var drawable = LottieDrawable()
        drawable.composition = composition
        var bitmap = bitmapPool.acquire(drawable.intrinsicWidth, drawable.intrinsicHeight)
        var canvas = Canvas(bitmap)
        log("Drawing day_night day")
        drawable.draw(canvas)
        snapshotter.record(bitmap, "Day/Night", "Day")
        LottieCompositionCache.getInstance().clear()
        bitmapPool.release(bitmap)

        newConfig = Configuration(context.resources.configuration)
        newConfig.uiMode = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()
        newConfig.uiMode = newConfig.uiMode or Configuration.UI_MODE_NIGHT_YES
        val nightContext = context.createConfigurationContext(newConfig)
        result = LottieCompositionFactory.fromRawResSync(nightContext, R.raw.day_night)
        composition = result.value!!
        drawable = LottieDrawable()
        drawable.composition = composition
        bitmap = bitmapPool.acquire(drawable.intrinsicWidth, drawable.intrinsicHeight)
        canvas = Canvas(bitmap)
        log("Drawing day_night day")
        drawable.draw(canvas)
        snapshotter.record(bitmap, "Day/Night", "Night")
        LottieCompositionCache.getInstance().clear()
        bitmapPool.release(bitmap)
    }
}