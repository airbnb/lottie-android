package com.airbnb.lottie

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import android.util.Log
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import com.airbnb.lottie.samples.FilmStripSnapshotTestActivity

import com.airbnb.lottie.samples.views.FilmStripView
import kotlinx.coroutines.runBlocking
import org.junit.Before
import com.airbnb.lottie.samples.R as SampleAppR

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.IllegalStateException
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private const val SIZE_PX = 200

/**
 * Run these with: ./gradlew recordMode screenshotTests
 * If you run that command, it completes successfully, and nothing shows up in git, then you
 * haven't broken anything!
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class LottieTest {

    @get:Rule
    var snapshotActivityRule = ActivityTestRule(FilmStripSnapshotTestActivity::class.java)
    private val activity get() = snapshotActivityRule.activity

    @get:Rule
    var permissionRule = GrantPermissionRule.grant(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private lateinit var snapshotter: HappoSnapshotter

    @Before
    fun setup() {
        snapshotter = HappoSnapshotter(activity)
    }

    @Test
    fun testAll() {
        runBlocking {
            snapshotAssets()
            snapshotter.finalizeReportAndUpload()
        }
    }

    private suspend fun snapshotAssets(pathPrefix: String = "") {
        activity.getAssets().list(pathPrefix)?.forEach { animation ->
            if (!animation.contains('.')) {
                snapshotAssets(if (pathPrefix.isEmpty()) animation else "$pathPrefix/$animation")
                return@forEach
            }
            if (!animation.endsWith(".json") && !animation.endsWith(".zip")) return@forEach
            val composition = parseComposition(if (pathPrefix.isEmpty()) animation else "$pathPrefix/$animation")
            val bitmap = activity.snapshot(composition)
            snapshotter.record(animation, bitmap)
        }
    }

    private suspend fun parseComposition(animationName: String) = suspendCoroutine<LottieComposition> { continuation ->
        LottieCompositionFactory.fromAsset(activity, animationName)
                .addFailureListener { continuation.resumeWithException(it) }
                .addListener { continuation.resume(it) }
    }
}
