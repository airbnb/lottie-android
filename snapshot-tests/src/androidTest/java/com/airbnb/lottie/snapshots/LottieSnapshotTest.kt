package com.airbnb.lottie.snapshots

import android.Manifest
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.model.LottieCompositionCache
import com.airbnb.lottie.snapshots.tests.ApplyOpacityToLayerTestCase
import com.airbnb.lottie.snapshots.tests.AssetsTestCase
import com.airbnb.lottie.snapshots.tests.ColorStateListColorFilterTestCase
import com.airbnb.lottie.snapshots.tests.ComposeDynamicPropertiesTestCase
import com.airbnb.lottie.snapshots.tests.CustomBoundsTestCase
import com.airbnb.lottie.snapshots.tests.DynamicPropertiesTestCase
import com.airbnb.lottie.snapshots.tests.FailureTestCase
import com.airbnb.lottie.snapshots.tests.FrameBoundariesTestCase
import com.airbnb.lottie.snapshots.tests.MarkersTestCase
import com.airbnb.lottie.snapshots.tests.NightModeTestCase
import com.airbnb.lottie.snapshots.tests.OutlineMasksAndMattesTestCase
import com.airbnb.lottie.snapshots.tests.PartialFrameProgressTestCase
import com.airbnb.lottie.snapshots.tests.ProdAnimationsTestCase
import com.airbnb.lottie.snapshots.tests.ScaleTypesTestCase
import com.airbnb.lottie.snapshots.tests.TextTestCase
import com.airbnb.lottie.snapshots.utils.BitmapPool
import com.airbnb.lottie.snapshots.utils.HappoSnapshotter
import com.airbnb.lottie.snapshots.utils.ObjectPool
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@LargeTest
class LottieSnapshotTest {

    @get:Rule
    val snapshotActivityRule = ActivityScenarioRule(SnapshotTestActivity::class.java)

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    @Before
    fun setup() {
        LottieCompositionCache.getInstance().resize(1)
    }

    @Test
    fun testAll() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val snapshotter = HappoSnapshotter(context) { name, variant ->
            snapshotActivityRule.scenario.onActivity { activity ->
                activity.updateUiForSnapshot(name, variant)
            }
        }
        val testCaseContext: SnapshotTestCaseContext = object : SnapshotTestCaseContext {
            override val context: Context = context
            override val snapshotter: HappoSnapshotter = snapshotter
            override val bitmapPool: BitmapPool = BitmapPool()
            override val animationViewPool: ObjectPool<LottieAnimationView> = ObjectPool {
                val animationViewContainer = FrameLayout(context)
                NoCacheLottieAnimationView(context).apply {
                    animationViewContainer.addView(this)
                }
            }
            override val filmStripViewPool: ObjectPool<FilmStripView> = ObjectPool {
                FilmStripView(context).apply {
                    setLayerType(View.LAYER_TYPE_NONE, null)
                }
            }

            override fun onActivity(callback: (SnapshotTestActivity) -> Unit) {
                snapshotActivityRule.scenario.onActivity(callback)
            }
        }
        val prodAnimations = ProdAnimationsTestCase()
        val testCases = listOf(
            CustomBoundsTestCase(),
            ColorStateListColorFilterTestCase(),
            FailureTestCase(),
            FrameBoundariesTestCase(),
            ScaleTypesTestCase(),
            DynamicPropertiesTestCase(),
            MarkersTestCase(),
            AssetsTestCase(),
            TextTestCase(),
            PartialFrameProgressTestCase(),
            NightModeTestCase(),
            ApplyOpacityToLayerTestCase(),
            OutlineMasksAndMattesTestCase(),
            ComposeDynamicPropertiesTestCase(),
            prodAnimations,
        )

        withTimeout(TimeUnit.MINUTES.toMillis(45)) {
            launch {
                with(prodAnimations) {
                    // Kick off the downloads ahead of time so it can start while the other tests are snapshotting
                    testCaseContext.downloadAnimations()
                }
            }
            for (testCase in testCases) {
                Log.d(TAG, "Running test case ${testCase::class.java}")
                with(testCase) {
                    testCaseContext.bitmapPool.clear()
                    LottieCompositionCache.getInstance().clear()
                    testCaseContext.run()
                }
                Log.d(TAG, "Finished running test case ${testCase::class.java}")
            }
            snapshotter.finalizeReportAndUpload()
        }
    }

    companion object {
        private const val TAG = "LottieSnapshotTest"
    }
}
