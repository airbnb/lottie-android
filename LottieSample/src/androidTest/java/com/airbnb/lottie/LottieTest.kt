package com.airbnb.lottie

import android.Manifest
import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.updateLayoutParams
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import com.airbnb.lottie.samples.SnapshotTestActivity

import kotlinx.coroutines.runBlocking
import org.junit.Before
import com.airbnb.lottie.samples.R as SampleAppR

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
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
    var snapshotActivityRule = ActivityTestRule(SnapshotTestActivity::class.java)
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
            snapshotFrameBoundaries()
            snapshotScaleTypes()
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
            val bitmap = activity.snapshotFilmstrip(composition)
            snapshotter.record(bitmap, animation, "default")
        }
    }

    private suspend fun snapshotFrameBoundaries() {
        withAnimationView("Tests/Frame.json") { animationView ->
            animationView.frame = 16
        }
        withAnimationView("Tests/Frame.json") { animationView ->
            animationView.frame = 17
        }
        withAnimationView("Tests/Frame.json") { animationView ->
            animationView.frame = 50
        }
        withAnimationView("Tests/Frame.json") { animationView ->
            animationView.frame = 51
        }

        withAnimationView("Tests/RGB.json") { animationView ->
            animationView.frame = 0
        }

        withAnimationView("Tests/RGB.json") { animationView ->
            animationView.frame = 1
        }
        withAnimationView("Tests/RGB.json") { animationView ->
            animationView.frame = 2
        }
    }

    private suspend fun snapshotScaleTypes() {
        withAnimationView("LottieLogo1.json", "Wrap Content") { animationView ->
            animationView.updateLayoutParams {
                width = ViewGroup.LayoutParams.WRAP_CONTENT
                height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }

        withAnimationView("LottieLogo1.json", "Match Parent") { animationView ->
            animationView.updateLayoutParams {
                width = ViewGroup.LayoutParams.MATCH_PARENT
                height = ViewGroup.LayoutParams.MATCH_PARENT
            }
        }

        withAnimationView("LottieLogo1.json", "300x300@2x") { animationView ->
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scale = 2f
        }

        withAnimationView("LottieLogo1.json", "300x300@4x") { animationView ->
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scale = 4f
        }

        withAnimationView("LottieLogo1.json", "300x300 centerCrop") { animationView ->
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.CENTER_CROP
        }

        withAnimationView("LottieLogo1.json", "300x300 centerInside") { animationView ->
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

        withAnimationView("LottieLogo1.json", "300x300 centerInside @2x") { animationView ->
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.CENTER_INSIDE
            animationView.scale = 2f
        }

        withAnimationView("LottieLogo1.json", "300x300 centerCrop @2x") { animationView ->
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.CENTER_CROP
            animationView.scale = 2f
        }

        withAnimationView("LottieLogo1.json", "600x300 centerInside") { animationView ->
            animationView.updateLayoutParams {
                width = 600.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

        withAnimationView("LottieLogo1.json", "300x600 centerInside") { animationView ->
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 600.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
    }

    private suspend fun withAnimationView(animationName: String, variant: String = "default", block: (LottieAnimationView) -> Unit) {
        val animationView = activity.getAnimationView()
        animationView.setComposition(parseComposition(animationName))
        val layoutParams = animationView.layoutParams
        animationView.frame = 0
        animationView.scale = 1f
        animationView.scaleType
        animationView.scaleType = ImageView.ScaleType.FIT_CENTER

        block(animationView)

        val bitmap = activity.snapshotAnimationView()
        snapshotter.record(bitmap, animationName, variant)

        animationView.layoutParams = layoutParams
        animationView.requestLayout()
        animationView.scale = 1f
        animationView.scaleType = ImageView.ScaleType.FIT_CENTER
    }

    private suspend fun parseComposition(animationName: String) = suspendCoroutine<LottieComposition> { continuation ->
        var isResumed = false
        LottieCompositionFactory.fromAsset(activity, animationName)
                .addFailureListener {
                    if (isResumed) return@addFailureListener
                    continuation.resumeWithException(it)
                    isResumed = true
                }
                .addListener {
                    if (isResumed) return@addListener
                    continuation.resume(it)
                    isResumed = true
                }
    }

    private val Number.dp get() = this.toFloat() / (Resources.getSystem().displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}
