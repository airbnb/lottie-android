package com.airbnb.lottie.snapshots

import android.Manifest
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import android.os.Debug
import android.util.Log
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.airbnb.lottie.Lottie
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieConfig
import com.airbnb.lottie.model.LottieCompositionCache
import com.airbnb.lottie.snapshots.tests.ApplyOpacityToLayerTestCase
import com.airbnb.lottie.snapshots.tests.AssetsTestCase
import com.airbnb.lottie.snapshots.tests.ClipChildrenTestCase
import com.airbnb.lottie.snapshots.tests.ColorStateListColorFilterTestCase
import com.airbnb.lottie.snapshots.tests.ComposeDynamicPropertiesTestCase
import com.airbnb.lottie.snapshots.tests.ComposeScaleTypesTestCase
import com.airbnb.lottie.snapshots.tests.CompositionFrameRate
import com.airbnb.lottie.snapshots.tests.CustomBoundsTestCase
import com.airbnb.lottie.snapshots.tests.DynamicPropertiesTestCase
import com.airbnb.lottie.snapshots.tests.FailureTestCase
import com.airbnb.lottie.snapshots.tests.FrameBoundariesTestCase
import com.airbnb.lottie.snapshots.tests.LargeCompositionSoftwareRendering
import com.airbnb.lottie.snapshots.tests.MarkersTestCase
import com.airbnb.lottie.snapshots.tests.NightModeTestCase
import com.airbnb.lottie.snapshots.tests.OutlineMasksAndMattesTestCase
import com.airbnb.lottie.snapshots.tests.PartialFrameProgressTestCase
import com.airbnb.lottie.snapshots.tests.ProdAnimationsTestCase
import com.airbnb.lottie.snapshots.tests.ScaleTypesTestCase
import com.airbnb.lottie.snapshots.tests.SeekBarTestCase
import com.airbnb.lottie.snapshots.tests.SoftwareRenderingDynamicPropertiesInvalidationTestCase
import com.airbnb.lottie.snapshots.tests.TextTestCase
import com.airbnb.lottie.snapshots.utils.BitmapPool
import com.airbnb.lottie.snapshots.utils.HappoSnapshotter
import com.airbnb.lottie.snapshots.utils.ObjectPool
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
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

    lateinit var testCaseContext: SnapshotTestCaseContext
    lateinit var snapshotter: HappoSnapshotter
    private lateinit var s3AccessKey: String
    private lateinit var s3SecretKey: String

    @Before
    fun setup() {
        LottieCompositionCache.getInstance().resize(1)
        Lottie.initialize(
            LottieConfig.Builder()
                .setDisablePathInterpolatorCache(true)
                .build()
        )
        val context = ApplicationProvider.getApplicationContext<Context>()
        s3AccessKey = BuildConfig.S3AccessKey
        s3SecretKey = BuildConfig.S3SecretKey
        var happoApiKey = BuildConfig.HappoApiKey
        var happoSecretKey = BuildConfig.HappoSecretKey
        @Suppress("KotlinConstantConditions")
        if (BuildConfig.S3AccessKey == "") {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://us-central1-lottie-snapshots.cloudfunctions.net/snapshot-env-v1/snapshots")
                .build()
            val response = client.newCall(request).execute()
            val json = JSONObject(response.body?.string() ?: "{}")
            s3AccessKey = json.getString("LOTTIE_S3_API_KEY")
            s3SecretKey = json.getString("LOTTIE_S3_SECRET_KEY")
            happoApiKey = json.getString("LOTTIE_HAPPO_API_KEY")
            happoSecretKey = json.getString("LOTTIE_HAPPO_SECRET_KEY")
        }
        snapshotter = HappoSnapshotter(context, s3AccessKey, s3SecretKey, happoApiKey, happoSecretKey) { name, variant ->
            snapshotActivityRule.scenario.onActivity { activity ->
                activity.updateUiForSnapshot(name, variant)
            }
        }
        testCaseContext = object : SnapshotTestCaseContext {
            override val context: Context = context
            override val snapshotter: HappoSnapshotter = this@LottieSnapshotTest.snapshotter
            override val bitmapPool: BitmapPool = BitmapPool()
            override val animationViewPool: ObjectPool<LottieAnimationView> = ObjectPool {
                val animationViewContainer = FrameLayout(context)
                NoCacheLottieAnimationView(context).apply {
                    animationViewContainer.addView(this)
                }
            }
            override val filmStripViewPool: ObjectPool<FilmStripView> = ObjectPool {
                FilmStripView(context)
            }

            override fun onActivity(callback: (SnapshotTestActivity) -> Unit) {
                snapshotActivityRule.scenario.onActivity(callback)
            }
        }
        snapshotActivityRule.scenario.onActivity { activity ->
            activity.registerComponentCallbacks(object : ComponentCallbacks2 {
                override fun onConfigurationChanged(newConfig: Configuration) {}

                override fun onLowMemory() {
                    testCaseContext.bitmapPool.clear()
                }

                override fun onTrimMemory(level: Int) {
                    testCaseContext.bitmapPool.clear()
                }

            })
        }
    }

    @Test
    fun testAll() = runBlocking {
        val testCases = listOf(
            CustomBoundsTestCase(),
            ColorStateListColorFilterTestCase(),
            FailureTestCase(),
            FrameBoundariesTestCase(),
            ScaleTypesTestCase(),
            ComposeScaleTypesTestCase(),
            DynamicPropertiesTestCase(),
            MarkersTestCase(),
            AssetsTestCase(),
            TextTestCase(),
            PartialFrameProgressTestCase(),
            NightModeTestCase(),
            ApplyOpacityToLayerTestCase(),
            OutlineMasksAndMattesTestCase(),
            LargeCompositionSoftwareRendering(),
            ComposeDynamicPropertiesTestCase(),
            ProdAnimationsTestCase(s3AccessKey, s3SecretKey),
            ClipChildrenTestCase(),
            SoftwareRenderingDynamicPropertiesInvalidationTestCase(),
            SeekBarTestCase(),
            CompositionFrameRate(),
        )

        withTimeout(TimeUnit.MINUTES.toMillis(45)) {
            launch {
                with(testCases.filterIsInstance<ProdAnimationsTestCase>().firstOrNull() ?: return@launch) {
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
