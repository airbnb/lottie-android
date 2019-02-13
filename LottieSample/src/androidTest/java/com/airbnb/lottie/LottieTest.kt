package com.airbnb.lottie

import android.Manifest
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PointF
import android.graphics.PorterDuff
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
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
import com.airbnb.lottie.samples.views.FilmStripView
import com.airbnb.lottie.value.LottieInterpolatedIntegerValue
import com.airbnb.lottie.value.LottieRelativeFloatValueCallback
import com.airbnb.lottie.value.LottieRelativePointValueCallback
import com.airbnb.lottie.value.LottieValueCallback
import com.airbnb.lottie.value.ScaleXY
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.S3ObjectSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Before
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
import com.airbnb.lottie.samples.R as SampleAppR

private const val SIZE_PX = 200

/**
 * Run these with: ./gradlew recordMode screenshotTests
 * If you run that command, it completes successfully, and nothing shows up in git, then you
 * haven't broken anything!
 */
@ExperimentalCoroutinesApi
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

    private val bitmapPool by lazy { BitmapPool(activity.resources) }
    private val dummyBitmap by lazy { BitmapFactory.decodeResource(activity.resources, com.airbnb.lottie.samples.R.drawable.airbnb); }
    @Suppress("DEPRECATION")
    private val animationView by lazy {
        LottieAnimationView(activity).apply {
            isDrawingCacheEnabled = false
        }
    }
    @Suppress("DEPRECATION")
    private val filmStripView by lazy {
        FilmStripView(activity).apply {
            setImageAssetDelegate(ImageAssetDelegate { dummyBitmap })
            setFontAssetDelegate(object : FontAssetDelegate() {
                override fun getFontPath(fontFamily: String?): String {
                    return "fonts/Roboto.ttf"
                }
            })
        }
    }

    @Before
    fun setup() {
        L.DBG = false
        snapshotter = HappoSnapshotter(activity)
        prodAnimationsTransferUtility = TransferUtility.builder()
                .context(activity)
                .s3Client(AmazonS3Client(BasicAWSCredentials(BuildConfig.S3AccessKey, BuildConfig.S3SecretKey)))
                .defaultBucket("lottie-prod-animations")
                .build()
        LottieCompositionCache.getInstance().resize(5)
    }

    @Test
    fun testAll() {
        runBlocking {
            withTimeout(TimeUnit.MINUTES.toMillis(45)) {
                snapshotProdAnimations()
                snapshotAssets()
                snapshotFrameBoundaries()
                snapshotScaleTypes()
                testDynamicProperties()
                testMarkers()
                snapshotter.finalizeReportAndUpload()
            }
        }
    }

    private suspend fun snapshotProdAnimations() = coroutineScope {
        log("Downloading prod animation keys from S3.")
        val s3Client = AmazonS3Client(BasicAWSCredentials(BuildConfig.S3AccessKey, BuildConfig.S3SecretKey))
        val allObjects = s3Client.fetchAllObjects("lottie-prod-animations")
        log("Downloaded prod animation keys from S3.")

        val downloadChannel = downloadAnimations(allObjects)
        val compositionsChannel = parseCompositions(downloadChannel)
        snapshotCompositions(compositionsChannel)
    }

    private fun CoroutineScope.downloadAnimations(animations: List<S3ObjectSummary>) = produce<File>(
            context = Dispatchers.IO,
            capacity = 10
    ) {
        animations.forEach { animation ->
            val file = File(activity.cacheDir, animation.key)
            file.deleteOnExit()
            log("Downloading ${animation.key}")
            prodAnimationsTransferUtility.download(animation.key, file).await()
            send(file)
        }
    }

    private fun CoroutineScope.parseCompositions(files: ReceiveChannel<File>) = produce<Pair<String, LottieComposition>>(
            context = Dispatchers.Default,
            capacity = 10
    ) {
        while (!files.isClosedForReceive) {
            val file = files.receive()
            log("Parsing ${file.nameWithoutExtension}")
            send(file.nameWithoutExtension to parseComposition(file))
        }
        log("Parsed all animations.")
    }

    private suspend fun snapshotCompositions(compositions: ReceiveChannel<Pair<String, LottieComposition>>) = coroutineScope {
        for ((name, composition) in compositions) {
            log("Snapshotting $name")
            val bitmap = bitmapPool.acquire(1000, 1000)
            val canvas = Canvas(bitmap)
            val spec = View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY)
            filmStripView.measure(spec, spec)
            filmStripView.layout(0, 0, 1000, 1000)
            filmStripView.setComposition(composition)
            canvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR)
            filmStripView.draw(canvas)
            LottieCompositionCache.getInstance().clear()
            log("Recording $name")
            snapshotter.record(bitmap, name, "default")
            activity.recordSnapshot(name, "default")
            bitmapPool.release(bitmap)
            log("Snapshotted $name")
        }
    }

    private suspend fun snapshotAssets() = coroutineScope {
        log("Starting assets")
        val assetsChannel = listAssets()
        val compositionsChannel = parseCompositionsFromAssets(assetsChannel)
        snapshotCompositions(compositionsChannel)
        log("Finished assets")
    }

    private fun listAssets(assets: MutableList<String> = mutableListOf(), pathPrefix: String = ""): List<String> {
        activity.getAssets().list(pathPrefix)?.forEach { animation ->
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

    private fun CoroutineScope.parseCompositionsFromAssets(assets: List<String>) = produce<Pair<String, LottieComposition>>(
            context = Dispatchers.Default,
            capacity = 10
    ) {
        for (asset in assets) {
            log("Parsing $asset")
            send(asset to parseComposition(asset))
        }
        log("Parsed all animations.")
    }

    private suspend fun snapshotFrameBoundaries() {
        log("snapshotFrameBoundaries")
        withDrawable("Tests/Frame.json", "Frame Boundary", "Frame 16 Red") { drawable ->
            log("Setting frame to 16")
            drawable.frame = 16
        }
        log("Finished setting frame to 16")
        withDrawable("Tests/Frame.json", "Frame Boundary", "Frame 17 Blue") { drawable ->
            drawable.frame = 17
        }
        withDrawable("Tests/Frame.json", "Frame Boundary", "Frame 50 Blue") { drawable ->
            drawable.frame = 50
        }
        withDrawable("Tests/Frame.json", "Frame Boundary", "Frame 51 Green") { drawable ->
            drawable.frame = 51
        }

        withDrawable("Tests/RGB.json", "Frame Boundary", "Frame 0 Red") { drawable ->
            drawable.frame = 0
        }

        withDrawable("Tests/RGB.json", "Frame Boundary", "Frame 1 Green") { drawable ->
            drawable.frame = 1
        }
        withDrawable("Tests/RGB.json", "Frame Boundary", "Frame 2 Blue") { drawable ->
            drawable.frame = 2
        }
    }

    private suspend fun snapshotScaleTypes() = withContext(Dispatchers.Main) {
        withAnimationView("LottieLogo1.json", "Scale Types", "Wrap Content") { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = ViewGroup.LayoutParams.WRAP_CONTENT
                height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }

        withAnimationView("LottieLogo1.json", "Scale Types", "Match Parent") { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = ViewGroup.LayoutParams.MATCH_PARENT
                height = ViewGroup.LayoutParams.MATCH_PARENT
            }
        }

        withAnimationView("LottieLogo1.json", "Scale Types", "300x300@2x") { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scale = 2f
        }

        withAnimationView("LottieLogo1.json", "Scale Types", "300x300@4x") { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scale = 4f
        }

        withAnimationView("LottieLogo1.json", "Scale Types", "300x300 centerCrop") { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.CENTER_CROP
        }

        withAnimationView("LottieLogo1.json", "Scale Types", "300x300 centerInside") { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

        withAnimationView("LottieLogo1.json", "Scale Types", "300x300 centerInside @2x") { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.CENTER_INSIDE
            animationView.scale = 2f
        }

        withAnimationView("LottieLogo1.json", "Scale Types", "300x300 centerCrop @2x") { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.CENTER_CROP
            animationView.scale = 2f
        }

        withAnimationView("LottieLogo1.json", "Scale Types", "600x300 centerInside") { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = 600.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

        withAnimationView("LottieLogo1.json", "Scale Types", "300x600 centerInside") { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 600.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
    }

    private suspend fun testDynamicProperties() {
        log("Testing dynamic properties")
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

    private fun <T> testDynamicProperty(name: String, keyPath: KeyPath, property: T, callback: LottieValueCallback<T>, progress: Float = 0f) {
        withDrawable("Tests/Shapes.json", "Dynamic Properties", name) { drawable ->
            drawable.addValueCallback(keyPath, property, callback)
            drawable.progress = progress
        }
    }

    private suspend fun testMarkers() {
        withDrawable("Tests/Marker.json", "Marker", "startFrame") { drawable ->
            drawable.setMinAndMaxFrame("Marker A")
            drawable.frame = drawable.minFrame.toInt()
        }

        withDrawable("Tests/Marker.json", "Marker", "endFrame") { drawable ->
            drawable.setMinAndMaxFrame("Marker A")
            drawable.frame = drawable.maxFrame.toInt()
        }
    }

    private fun withDrawable(assetName: String, snapshotName: String, snapshotVariant: String, callback: (LottieDrawable) -> Unit) {
        val result = LottieCompositionFactory.fromAssetSync(activity, assetName)
        val composition = result.value
                ?: throw IllegalArgumentException("Unable to parse $assetName.", result.exception)
        val drawable = LottieDrawable()
        drawable.setComposition(composition)
        callback(drawable)
        val bitmap = bitmapPool.acquire(drawable.intrinsicWidth, drawable.intrinsicHeight)
        val canvas = Canvas(bitmap)
        drawable.draw(canvas)
        snapshotter.record(bitmap, snapshotName, snapshotVariant)
        activity.recordSnapshot(snapshotName, snapshotVariant)
        LottieCompositionCache.getInstance().clear()
        bitmapPool.release(bitmap)
    }

    private fun withAnimationView(
            assetName: String,
            snapshotName: String = assetName,
            snapshotVariant: String = "default",
            callback: (LottieAnimationView) -> Unit
    ) {
        val result = LottieCompositionFactory.fromAssetSync(activity, assetName)
        val composition = result.value
                ?: throw IllegalArgumentException("Unable to parse $assetName.", result.exception)
        animationView.setComposition(composition)
        animationView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        animationView.scale = 1f
        animationView.scaleType = ImageView.ScaleType.FIT_CENTER
        callback(animationView)
        val lp = animationView.layoutParams
        val widthSpec = if (lp.width > 0) {
            View.MeasureSpec.makeMeasureSpec(lp.width, View.MeasureSpec.EXACTLY)
        } else {
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        }
        val heightSpec = if (lp.height > 0) {
            View.MeasureSpec.makeMeasureSpec(lp.height, View.MeasureSpec.EXACTLY)
        } else {
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        }
        animationView.measure(widthSpec, heightSpec)
        animationView.layout(0, 0, animationView.measuredWidth, animationView.measuredHeight)
        val bitmap = bitmapPool.acquire(animationView.width, animationView.height)
        val canvas = Canvas(bitmap)
        Log.d(L.TAG, "Drawing $assetName $snapshotName $snapshotVariant")
        animationView.draw(canvas)
        snapshotter.record(bitmap, snapshotName, snapshotVariant)
        activity.recordSnapshot(snapshotName, snapshotVariant)
        bitmapPool.release(bitmap)
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

    private fun log(message: String) {
        Log.d(L.TAG, message)
    }

    private val Number.dp get() = this.toFloat() / (Resources.getSystem().displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}
