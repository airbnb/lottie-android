package com.airbnb.lottie

import android.Manifest
import android.content.res.Resources
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PointF
import android.util.DisplayMetrics
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.updateLayoutParams
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.model.LottieCompositionCache
import com.airbnb.lottie.samples.BuildConfig
import com.airbnb.lottie.samples.SnapshotTestActivity
import com.airbnb.lottie.value.*
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ListObjectsV2Request
import com.amazonaws.services.s3.model.S3ObjectSummary
import kotlinx.coroutines.*

import org.junit.Before
import com.airbnb.lottie.samples.R as SampleAppR

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream
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

    private lateinit var prodAnimationsTransferUtility: TransferUtility

    private lateinit var snapshotter: HappoSnapshotter

    @Before
    fun setup() {
        snapshotter = HappoSnapshotter(activity)
        prodAnimationsTransferUtility = TransferUtility.builder()
                .context(activity)
                .s3Client(AmazonS3Client(BasicAWSCredentials(BuildConfig.S3AccessKey, BuildConfig.S3SecretKey)))
                .defaultBucket("lottie-prod-animations")
                .build()

    }

    @Test
    fun testAll() {
        runBlocking {
            withTimeout(TimeUnit.MINUTES.toMillis(30)) {
                snapshotProdAnimations()
                snapshotAssets()
                snapshotFrameBoundaries()
                snapshotScaleTypes()
                testDynamicProperties()
                snapshotter.finalizeReportAndUpload()
            }
        }
    }

    private suspend fun snapshotProdAnimations() {
        Log.d(L.TAG, "Downloading prod animations from S3.")
        val allObjects = mutableListOf<S3ObjectSummary>()
        val s3Client = AmazonS3Client(BasicAWSCredentials(BuildConfig.S3AccessKey, BuildConfig.S3SecretKey))
        var request = ListObjectsV2Request().apply {
            bucketName = "lottie-prod-animations"
        }
        var result = s3Client.listObjectsV2(request)
        allObjects.addAll(result.objectSummaries)
        var startAfter = result.objectSummaries.lastOrNull()?.key
        while (startAfter != null) {
            request = ListObjectsV2Request().apply {
                bucketName = "lottie-prod-animations"
                this.startAfter = startAfter
            }
            result = s3Client.listObjectsV2(request)
            allObjects.addAll(result.objectSummaries)
            startAfter = result.objectSummaries.lastOrNull()?.key
        }

        allObjects.forEach { snapshotProdAnimation(it) }
    }

    private suspend fun snapshotProdAnimation(objectSummary: S3ObjectSummary) {
        val (fileName, extension) = objectSummary.key.split(".")
        val file = File(activity.cacheDir, fileName.md5 + ".$extension")
        prodAnimationsTransferUtility.download(objectSummary.key, file).await()
        Log.d(L.TAG, "Downloaded ${objectSummary.key}")

        val composition = parseComposition(file)
        val bitmap = activity.snapshotFilmstrip(composition)
        snapshotter.record(bitmap, "prod-" + objectSummary.key, "default")
        file.delete()
        LottieCompositionCache.getInstance().clear()
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
            LottieCompositionCache.getInstance().clear()
        }
    }

    private suspend fun CoroutineScope.snapshotFrameBoundaries() {
        Log.d(L.TAG, "snapshotFrameBoundaries")
        withAnimationView("Tests/Frame.json", "Frame Boundary", "Frame 16 Red") { animationView ->
            Log.d(L.TAG, "Setting frame to 16")
            animationView.frame = 16
        }
        Log.d(L.TAG, "Finished setting frame to 16")
        withAnimationView("Tests/Frame.json", "Frame Boundary", "Frame 17 Blue") { animationView ->
            animationView.frame = 17
        }
        withAnimationView("Tests/Frame.json", "Frame Boundary", "Frame 50 Blue") { animationView ->
            animationView.frame = 50
        }
        withAnimationView("Tests/Frame.json", "Frame Boundary", "Frame 51 Green") { animationView ->
            animationView.frame = 51
        }

        withAnimationView("Tests/RGB.json", "Frame Boundary", "Frame 0 Red") { animationView ->
            animationView.frame = 0
        }

        withAnimationView("Tests/RGB.json", "Frame Boundary", "Frame 1 Green") { animationView ->
            animationView.frame = 1
        }
        withAnimationView("Tests/RGB.json", "Frame Boundary", "Frame 2 Blue") { animationView ->
            animationView.frame = 2
        }
    }

    private suspend fun CoroutineScope.snapshotScaleTypes() {
        withAnimationView("LottieLogo1.json", "Scale Types", "Wrap Content") { animationView ->
            animationView.updateLayoutParams {
                width = ViewGroup.LayoutParams.WRAP_CONTENT
                height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }

        withAnimationView("LottieLogo1.json", "Scale Types", "Match Parent") { animationView ->
            animationView.updateLayoutParams {
                width = ViewGroup.LayoutParams.MATCH_PARENT
                height = ViewGroup.LayoutParams.MATCH_PARENT
            }
        }

        withAnimationView("LottieLogo1.json", "Scale Types", "300x300@2x") { animationView ->
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scale = 2f
        }

        withAnimationView("LottieLogo1.json", "Scale Types", "300x300@4x") { animationView ->
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scale = 4f
        }

        withAnimationView("LottieLogo1.json", "Scale Types", "300x300 centerCrop") { animationView ->
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.CENTER_CROP
        }

        withAnimationView("LottieLogo1.json", "Scale Types", "300x300 centerInside") { animationView ->
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

        withAnimationView("LottieLogo1.json", "Scale Types", "300x300 centerInside @2x") { animationView ->
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.CENTER_INSIDE
            animationView.scale = 2f
        }

        withAnimationView("LottieLogo1.json", "Scale Types", "300x300 centerCrop @2x") { animationView ->
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.CENTER_CROP
            animationView.scale = 2f
        }

        withAnimationView("LottieLogo1.json", "Scale Types", "600x300 centerInside") { animationView ->
            animationView.updateLayoutParams {
                width = 600.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

        withAnimationView("LottieLogo1.json", "Scale Types", "300x600 centerInside") { animationView ->
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 600.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
    }

    private suspend fun CoroutineScope.testDynamicProperties() {
        testDynamicProperty(
                "Fill color (Green)",
                KeyPath("Shape Layer 1", "Rectangle", "Fill 1"),
                LottieProperty.COLOR,
                LottieValueCallback(Color.GREEN))

        testDynamicProperty(
                "Fill color (Yellow)",
                KeyPath("Shape Layer 1", "Rectangle", "Fill 1"),
                LottieProperty.COLOR,
                LottieValueCallback(Color.YELLOW))

        testDynamicProperty(
                "Fill opacity",
                KeyPath("Shape Layer 1", "Rectangle", "Fill 1"),
                LottieProperty.OPACITY,
                LottieValueCallback(50))

        testDynamicProperty(
                "Stroke color",
                KeyPath("Shape Layer 1", "Rectangle", "Stroke 1"),
                LottieProperty.STROKE_COLOR,
                LottieValueCallback(Color.GREEN))

        testDynamicProperty(
                "Stroke width",
                KeyPath("Shape Layer 1", "Rectangle", "Stroke 1"),
                LottieProperty.STROKE_WIDTH,
                LottieRelativeFloatValueCallback(50f))

        testDynamicProperty(
                "Stroke opacity",
                KeyPath("Shape Layer 1", "Rectangle", "Stroke 1"),
                LottieProperty.OPACITY,
                LottieValueCallback(50))

        testDynamicProperty(
                "Transform anchor point",
                KeyPath("Shape Layer 1", "Rectangle"),
                LottieProperty.TRANSFORM_ANCHOR_POINT,
                LottieRelativePointValueCallback(PointF(20f, 20f)))

        testDynamicProperty(
                "Transform position",
                KeyPath("Shape Layer 1", "Rectangle"),
                LottieProperty.TRANSFORM_POSITION,
                LottieRelativePointValueCallback(PointF(20f, 20f)))

        testDynamicProperty(
                "Transform position (relative)",
                KeyPath("Shape Layer 1", "Rectangle"),
                LottieProperty.TRANSFORM_POSITION,
                LottieRelativePointValueCallback(PointF(20f, 20f)))

        testDynamicProperty(
                "Transform opacity",
                KeyPath("Shape Layer 1", "Rectangle"),
                LottieProperty.TRANSFORM_OPACITY,
                LottieValueCallback(50))

        testDynamicProperty(
                "Transform rotation",
                KeyPath("Shape Layer 1", "Rectangle"),
                LottieProperty.TRANSFORM_ROTATION,
                LottieValueCallback(45f))

        testDynamicProperty(
                "Transform scale",
                KeyPath("Shape Layer 1", "Rectangle"),
                LottieProperty.TRANSFORM_SCALE,
                LottieValueCallback(ScaleXY(0.5f, 0.5f)))

        testDynamicProperty(
                "Ellipse position",
                KeyPath("Shape Layer 1", "Ellipse", "Ellipse Path 1"),
                LottieProperty.POSITION,
                LottieRelativePointValueCallback(PointF(20f, 20f)))

        testDynamicProperty(
                "Ellipse size",
                KeyPath("Shape Layer 1", "Ellipse", "Ellipse Path 1"),
                LottieProperty.ELLIPSE_SIZE,
                LottieRelativePointValueCallback(PointF(40f, 60f)))

        testDynamicProperty(
                "Star points",
                KeyPath("Shape Layer 1", "Star", "Polystar Path 1"),
                LottieProperty.POLYSTAR_POINTS,
                LottieValueCallback(8f))

        testDynamicProperty(
                "Star rotation",
                KeyPath("Shape Layer 1", "Star", "Polystar Path 1"),
                LottieProperty.POLYSTAR_ROTATION,
                LottieValueCallback(10f))

        testDynamicProperty(
                "Star position",
                KeyPath("Shape Layer 1", "Star", "Polystar Path 1"),
                LottieProperty.POSITION,
                LottieRelativePointValueCallback(PointF(20f, 20f)))

        testDynamicProperty(
                "Star inner radius",
                KeyPath("Shape Layer 1", "Star", "Polystar Path 1"),
                LottieProperty.POLYSTAR_INNER_RADIUS,
                LottieValueCallback(10f))

        testDynamicProperty(
                "Star inner roundedness",
                KeyPath("Shape Layer 1", "Star", "Polystar Path 1"),
                LottieProperty.POLYSTAR_INNER_ROUNDEDNESS,
                LottieValueCallback(100f))

        testDynamicProperty(
                "Star outer radius",
                KeyPath("Shape Layer 1", "Star", "Polystar Path 1"),
                LottieProperty.POLYSTAR_OUTER_RADIUS,
                LottieValueCallback(60f))

        testDynamicProperty(
                "Star outer roundedness",
                KeyPath("Shape Layer 1", "Star", "Polystar Path 1"),
                LottieProperty.POLYSTAR_OUTER_ROUNDEDNESS,
                LottieValueCallback(100f))

        testDynamicProperty(
                "Polygon points",
                KeyPath("Shape Layer 1", "Polygon", "Polystar Path 1"),
                LottieProperty.POLYSTAR_POINTS,
                LottieValueCallback(8f))

        testDynamicProperty(
                "Polygon rotation",
                KeyPath("Shape Layer 1", "Polygon", "Polystar Path 1"),
                LottieProperty.POLYSTAR_ROTATION,
                LottieValueCallback(10f))

        testDynamicProperty(
                "Polygon position",
                KeyPath("Shape Layer 1", "Polygon", "Polystar Path 1"),
                LottieProperty.POSITION,
                LottieRelativePointValueCallback(PointF(20f, 20f)))

        testDynamicProperty(
                "Polygon radius",
                KeyPath("Shape Layer 1", "Polygon", "Polystar Path 1"),
                LottieProperty.POLYSTAR_OUTER_RADIUS,
                LottieRelativeFloatValueCallback(60f))

        testDynamicProperty(
                "Polygon roundedness",
                KeyPath("Shape Layer 1", "Polygon", "Polystar Path 1"),
                LottieProperty.POLYSTAR_OUTER_ROUNDEDNESS,
                LottieValueCallback(100f))

        testDynamicProperty(
                "Repeater transform position",
                KeyPath("Shape Layer 1", "Repeater Shape", "Repeater 1"),
                LottieProperty.TRANSFORM_POSITION,
                LottieRelativePointValueCallback(PointF(100f, 100f)))

        testDynamicProperty(
                "Repeater transform start opacity",
                KeyPath("Shape Layer 1", "Repeater Shape", "Repeater 1"),
                LottieProperty.TRANSFORM_START_OPACITY,
                LottieValueCallback(25f))

        testDynamicProperty(
                "Repeater transform end opacity",
                KeyPath("Shape Layer 1", "Repeater Shape", "Repeater 1"),
                LottieProperty.TRANSFORM_END_OPACITY,
                LottieValueCallback(25f))

        testDynamicProperty(
                "Repeater transform rotation",
                KeyPath("Shape Layer 1", "Repeater Shape", "Repeater 1"),
                LottieProperty.TRANSFORM_ROTATION,
                LottieValueCallback(45f))

        testDynamicProperty(
                "Repeater transform scale",
                KeyPath("Shape Layer 1", "Repeater Shape", "Repeater 1"),
                LottieProperty.TRANSFORM_SCALE,
                LottieValueCallback(ScaleXY(2f, 2f)))

        testDynamicProperty(
                "Time remapping",
                KeyPath("Circle 1"),
                LottieProperty.TIME_REMAP,
                LottieValueCallback(1f))

        testDynamicProperty(
                "Color Filter",
                KeyPath("**"),
                LottieProperty.COLOR_FILTER,
                LottieValueCallback<ColorFilter>(SimpleColorFilter(Color.GREEN)))

        withAnimationView("Tests/Shapes.json", "Dynamic Propertiers", "Color Filter after blue") { animationView ->
            val blueColorFilter = LottieValueCallback<ColorFilter>(SimpleColorFilter(Color.GREEN))
            animationView.addValueCallback(KeyPath("**"), LottieProperty.COLOR_FILTER, blueColorFilter)
            val bitmap = activity.snapshotAnimationView()
            snapshotter.record(bitmap, "Dynamic Propertiers", "Color Filter before blue")
            blueColorFilter.setValue(SimpleColorFilter(Color.BLUE))
        }

        testDynamicProperty(
                "Null Color Filter",
                KeyPath("**"),
                LottieProperty.COLOR_FILTER,
                LottieValueCallback<ColorFilter>(null))

        testDynamicProperty(
                "Opacity interpolation (0)",
                KeyPath("Shape Layer 1", "Rectangle"),
                LottieProperty.TRANSFORM_OPACITY,
                LottieInterpolatedIntegerValue(10, 100),
                0f)

        testDynamicProperty(
                "Opacity interpolation (0.5)",
                KeyPath("Shape Layer 1", "Rectangle"),
                LottieProperty.TRANSFORM_OPACITY,
                LottieInterpolatedIntegerValue(10, 100),
                0.5f)

        testDynamicProperty(
                "Opacity interpolation (1)",
                KeyPath("Shape Layer 1", "Rectangle"),
                LottieProperty.TRANSFORM_OPACITY,
                LottieInterpolatedIntegerValue(10, 100),
                1f)
    }

    private suspend fun <T> CoroutineScope.testDynamicProperty(name: String, keyPath: KeyPath, property: T, callback: LottieValueCallback<T>, progress: Float = 0f) {
        withAnimationView("Tests/Shapes.json", "Dynamic Properties", name) { animationView ->
            animationView.progress = progress
            animationView.addValueCallback(keyPath, property, callback)
        }
    }

    private suspend fun CoroutineScope.withAnimationView(
            animationName: String,
            snapshotName: String? = null,
            variant: String = "default",
            block: suspend CoroutineScope.(LottieAnimationView) -> Unit
    ) {
        withContext(Dispatchers.Main) {
            val animationView = activity.getAnimationView()
            animationView.setComposition(parseComposition(animationName))
            val layoutParams = animationView.layoutParams
            animationView.frame = 0
            animationView.scale = 1f
            animationView.scaleType
            animationView.scaleType = ImageView.ScaleType.FIT_CENTER

            Log.d(L.TAG, "Waiting for layout")
            animationView.requestLayout()
            withContext(Dispatchers.Default) {
                suspendCoroutine<Unit> { continuation ->
                    animationView.post {
                        continuation.resume(Unit)
                    }
                }
            }

            block(animationView)
            val bitmap = activity.snapshotAnimationView()
            snapshotter.record(bitmap, snapshotName ?: animationName, variant)

            animationView.layoutParams = layoutParams
            animationView.requestLayout()
            animationView.scale = 1f
            animationView.scaleType = ImageView.ScaleType.FIT_CENTER
            LottieCompositionCache.getInstance().clear()
        }
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

    private suspend fun parseComposition(file: File) = suspendCoroutine<LottieComposition> { continuation ->
        var isResumed = false
        val task = if (file.name.endsWith("zip")) LottieCompositionFactory.fromZipStream(ZipInputStream(FileInputStream(file)), file.name)
                else LottieCompositionFactory.fromJsonInputStream(FileInputStream(file), file.name)
        task
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
