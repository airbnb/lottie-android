package com.airbnb.lottie.samples

import android.Manifest
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.*
import android.os.Build.VERSION
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.updateLayoutParams
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.airbnb.lottie.*
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.model.LottieCompositionCache
import com.airbnb.lottie.samples.views.FilmStripView
import com.airbnb.lottie.value.*
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.S3ObjectSummary
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream

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

    private val bitmapPool by lazy { BitmapPool() }
    private val dummyBitmap by lazy { BitmapFactory.decodeResource(activity.resources, R.drawable.airbnb); }

    private val filmStripViewPool = ObjectPool<FilmStripView> {
        FilmStripView(activity).apply {
            setImageAssetDelegate(ImageAssetDelegate { dummyBitmap })
            setFontAssetDelegate(object : FontAssetDelegate() {
                override fun getFontPath(fontFamily: String?): String {
                    return "fonts/Roboto.ttf"
                }
            })
            setLayerType(View.LAYER_TYPE_NONE, null)
        }
    }
    @Suppress("DEPRECATION")
    private val animationViewPool = ObjectPool<LottieAnimationView> {
        val animationViewContainer = FrameLayout(activity)
        NoCacheLottieAnimationView(activity).apply {
            animationViewContainer.addView(this)
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
    @ObsoleteCoroutinesApi
    fun testAll() = runBlocking {
        withTimeout(TimeUnit.MINUTES.toMillis(45)) {
            snapshotFailure()
            snapshotFrameBoundaries()
            snapshotScaleTypes()
            testDynamicProperties()
            testMarkers()
            snapshotAssets()
            testText()
            testPartialFrameProgress()
            snapshotProdAnimations()
            testNightMode()
            testApplyOpacityToLayer()
            snapshotter.finalizeReportAndUpload()
        }
    }

    private suspend fun snapshotProdAnimations() = coroutineScope {
        val s3Client = AmazonS3Client(BasicAWSCredentials(BuildConfig.S3AccessKey, BuildConfig.S3SecretKey))
        val allObjects = s3Client.fetchAllObjects("lottie-prod-animations")

        val downloadChannel = downloadAnimations(allObjects)
        val compositionsChannel = parseCompositions(downloadChannel)
        repeat(4) { snapshotCompositions(compositionsChannel) }
    }

    private fun CoroutineScope.downloadAnimations(animations: List<S3ObjectSummary>) = produce<File>(
            context = Dispatchers.IO,
            capacity = 10
    ) {
        for (animation in animations) {
            val file = File(activity.cacheDir, animation.key)
            file.deleteOnExit()
            prodAnimationsTransferUtility.download(animation.key, file).await()
            send(file)
        }
    }

    private fun CoroutineScope.parseCompositions(files: ReceiveChannel<File>) = produce<Pair<String, LottieComposition>>(
            context = Dispatchers.Default,
            capacity = 1
    ) {
        for (file in files) {
            log("Parsing ${file.nameWithoutExtension}")
            val result = if (file.name.endsWith("zip")) LottieCompositionFactory.fromZipStreamSync(ZipInputStream(FileInputStream(file)), file.name)
            else LottieCompositionFactory.fromJsonInputStreamSync(FileInputStream(file), file.name)
            val composition = result.value
                    ?: throw IllegalStateException("Unable to parse ${file.nameWithoutExtension}")
            send("prod-${file.nameWithoutExtension}" to composition)
        }
    }

    private suspend fun snapshotCompositions(channel: ReceiveChannel<Pair<String, LottieComposition>>) {
        for ((name, composition) in channel) {
            snapshotComposition(name, composition = composition)
        }
    }

    private suspend fun snapshotComposition(
            name: String,
            variant: String = "default",
            composition: LottieComposition,
            callback: ((FilmStripView) -> Unit)? = null
    ) = withContext(Dispatchers.Default) {
        log("Snapshotting $name")
        val bitmap = bitmapPool.acquire(1000, 1000)
        val canvas = Canvas(bitmap)
        val spec = View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY)
        val filmStripView = filmStripViewPool.acquire()
        callback?.invoke(filmStripView)
        filmStripView.measure(spec, spec)
        filmStripView.layout(0, 0, 1000, 1000)
        filmStripView.setComposition(composition)
        canvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR)
        withContext(Dispatchers.Main) {
            log("Drawing $name")
            filmStripView.draw(canvas)
        }
        filmStripViewPool.release(filmStripView)
        LottieCompositionCache.getInstance().clear()
        snapshotter.record(bitmap, name, variant)
        activity.recordSnapshot(name, variant)
        bitmapPool.release(bitmap)
    }

    @ObsoleteCoroutinesApi
    private suspend fun snapshotAssets() = coroutineScope {
        val assetsChannel = listAssets()
        val compositionsChannel = parseCompositionsFromAssets(assetsChannel)
        repeat(4) { snapshotCompositions(compositionsChannel) }
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

    @ObsoleteCoroutinesApi
    private fun CoroutineScope.parseCompositionsFromAssets(assets: List<String>) = produce<Pair<String, LottieComposition>>(
            context = newSingleThreadContext("Parsing"),
            capacity = 10
    ) {
        for (asset in assets) {
            log("Parsing $asset")
            val composition = LottieCompositionFactory.fromAssetSync(activity, asset).value
                    ?: throw java.lang.IllegalArgumentException("Unable to parse $asset.")
            send(asset to composition)
        }
    }

    private suspend fun snapshotFailure() {
        val animationView = animationViewPool.acquire()
        val semaphore = SuspendingSemaphore(0)
        animationView.setFailureListener { semaphore.release() }
        animationView.setFallbackResource(R.drawable.ic_close)
        animationView.setAnimationFromJson("Not Valid Json", null)
        semaphore.acquire()
        animationView.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        animationView.scale = 1f
        animationView.scaleType = ImageView.ScaleType.FIT_CENTER
        val widthSpec = View.MeasureSpec.makeMeasureSpec(activity.resources.displayMetrics.widthPixels, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(activity.resources.displayMetrics.heightPixels, View.MeasureSpec.EXACTLY)
        val animationViewContainer = animationView.parent as ViewGroup
        animationViewContainer.measure(widthSpec, heightSpec)
        animationViewContainer.layout(0, 0, animationViewContainer.measuredWidth, animationViewContainer.measuredHeight)
        val bitmap = bitmapPool.acquire(animationView.width, animationView.height)
        val canvas = Canvas(bitmap)
        animationView.draw(canvas)
        animationViewPool.release(animationView)
        val snapshotName = "Failure"
        val snapshotVariant = "Default"
        snapshotter.record(bitmap, snapshotName, snapshotVariant)
        activity.recordSnapshot(snapshotName, snapshotVariant)
        bitmapPool.release(bitmap)
    }

    private suspend fun snapshotFrameBoundaries() {
        withDrawable("Tests/Frame.json", "Frame Boundary", "Frame 16 Red") { drawable ->
            drawable.frame = 16
        }
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

        withDrawable("Tests/2FrameAnimation.json", "Float Progress", "0.0") { drawable ->
            drawable.progress = 0f
        }
    }

    private suspend fun testPartialFrameProgress() {
        withDrawable("Tests/2FrameAnimation.json", "Float Progress", "0") { drawable ->
            drawable.progress = 0f
        }

        withDrawable("Tests/2FrameAnimation.json", "Float Progress", "0.25") { drawable ->
            drawable.progress = 0.25f
        }

        withDrawable("Tests/2FrameAnimation.json", "Float Progress", "0.5") { drawable ->
            drawable.progress = 0.5f
        }

        withDrawable("Tests/2FrameAnimation.json", "Float Progress", "1.0") { drawable ->
            drawable.progress = 1f
        }
    }

    private suspend fun snapshotScaleTypes() = withContext(Dispatchers.Main) {
        withAnimationView("Lottie Logo 1.json", "Scale Types", "Wrap Content") { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = ViewGroup.LayoutParams.WRAP_CONTENT
                height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }

        withAnimationView("Lottie Logo 1.json", "Scale Types", "Match Parent") { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = ViewGroup.LayoutParams.MATCH_PARENT
                height = ViewGroup.LayoutParams.MATCH_PARENT
            }
        }

        withAnimationView("Lottie Logo 1.json", "Scale Types", "300x300@2x") { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scale = 2f
        }

        withAnimationView("Lottie Logo 1.json", "Scale Types", "300x300@4x") { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scale = 4f
        }

        withAnimationView("Lottie Logo 1.json", "Scale Types", "300x300 centerCrop") { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.CENTER_CROP
        }

        withAnimationView("Lottie Logo 1.json", "Scale Types", "300x300 centerInside") { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

        withAnimationView("Lottie Logo 1.json", "Scale Types", "300x300 fitXY") { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.FIT_XY
        }

        withAnimationView("Lottie Logo 1.json", "Scale Types", "300x300 fitXY DisableExtraScale") {
            animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.disableExtraScaleModeInFitXY()
            animationView.scaleType = ImageView.ScaleType.FIT_XY
        }

        withAnimationView("Lottie Logo 1.json", "Scale Types", "300x300 centerInside @2x") { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.CENTER_INSIDE
            animationView.scale = 2f
        }

        withAnimationView("Lottie Logo 1.json", "Scale Types", "300x300 centerCrop @2x") { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.CENTER_CROP
            animationView.scale = 2f
        }

        withAnimationView("Lottie Logo 1.json", "Scale Types", "600x300 centerInside") { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = 600.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

        withAnimationView("Lottie Logo 1.json", "Scale Types", "600x300 fitXY") { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = 600.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.FIT_XY
        }

        withAnimationView("Lottie Logo 1.json", "Scale Types", "600x300 fitXY DisableExtraScale") { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = 600.dp.toInt()
                height = 300.dp.toInt()
            }
            animationView.disableExtraScaleModeInFitXY()
            animationView.scaleType = ImageView.ScaleType.FIT_XY
        }

        withAnimationView("Lottie Logo 1.json", "Scale Types", "300x600 centerInside") { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 600.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

        withAnimationView("Lottie Logo 1.json", "Scale Types", "300x600 fitXY") { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 600.dp.toInt()
            }
            animationView.scaleType = ImageView.ScaleType.FIT_XY
        }

        withAnimationView("Lottie Logo 1.json", "Scale Types", "300x600 fitXY DisableExtraScale") { animationView ->
            animationView.progress = 1f
            animationView.updateLayoutParams {
                width = 300.dp.toInt()
                height = 600.dp.toInt()
            }
            animationView.disableExtraScaleModeInFitXY()
            animationView.scaleType = ImageView.ScaleType.FIT_XY
        }
    }

    private suspend fun testDynamicProperties() {
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
                "Rectangle corner roundedness",
                KeyPath("Shape Layer 1", "Rectangle", "Rectangle Path 1"),
                LottieProperty.CORNER_RADIUS,
                LottieValueCallback(7f))

        testDynamicProperty(
                "Rectangle position",
                KeyPath("Shape Layer 1", "Rectangle", "Rectangle Path 1"),
                LottieProperty.POSITION,
                LottieRelativePointValueCallback(PointF(20f, 20f)))

        testDynamicProperty(
                "Rectangle size",
                KeyPath("Shape Layer 1", "Rectangle", "Rectangle Path 1"),
                LottieProperty.RECTANGLE_SIZE,
                LottieRelativePointValueCallback(PointF(30f, 40f)))

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

        withDrawable("Tests/DynamicGradient.json", "Gradient Colors", "Linear Gradient Fill") { drawable ->
            val value = object : LottieValueCallback<Array<Int>>() {
                override fun getValue(frameInfo: LottieFrameInfo<Array<Int>>?): Array<Int>? {
                    return arrayOf(Color.YELLOW, Color.GREEN)
                }
            }
            drawable.addValueCallback(KeyPath("Linear", "Rectangle", "Gradient Fill"), LottieProperty.GRADIENT_COLOR, value)
        }

        withDrawable("Tests/DynamicGradient.json", "Gradient Colors", "Radial Gradient Fill") { drawable ->
            val value = object : LottieValueCallback<Array<Int>>() {
                override fun getValue(frameInfo: LottieFrameInfo<Array<Int>>?): Array<Int>? {
                    return arrayOf(Color.YELLOW, Color.GREEN)
                }
            }
            drawable.addValueCallback(KeyPath("Radial", "Rectangle", "Gradient Fill"), LottieProperty.GRADIENT_COLOR, value)
        }

        withDrawable("Tests/DynamicGradient.json", "Gradient Colors", "Linear Gradient Stroke") { drawable ->
            val value = object : LottieValueCallback<Array<Int>>() {
                override fun getValue(frameInfo: LottieFrameInfo<Array<Int>>?): Array<Int>? {
                    return arrayOf(Color.YELLOW, Color.GREEN)
                }
            }
            drawable.addValueCallback(KeyPath("Linear", "Rectangle", "Gradient Stroke"), LottieProperty.GRADIENT_COLOR, value)
        }

        withDrawable("Tests/DynamicGradient.json", "Gradient Colors", "Radial Gradient Stroke") { drawable ->
            val value = object : LottieValueCallback<Array<Int>>() {
                override fun getValue(frameInfo: LottieFrameInfo<Array<Int>>?): Array<Int>? {
                    return arrayOf(Color.YELLOW, Color.GREEN)
                }
            }
            drawable.addValueCallback(KeyPath("Radial", "Rectangle", "Gradient Stroke"), LottieProperty.GRADIENT_COLOR, value)
        }

        withDrawable("Tests/DynamicGradient.json", "Gradient Opacity", "Linear Gradient Fill") { drawable ->
            val value = object : LottieValueCallback<Int>() {
                override fun getValue(frameInfo: LottieFrameInfo<Int>?) = 50
            }
            drawable.addValueCallback(KeyPath("Linear", "Rectangle", "Gradient Fill"), LottieProperty.OPACITY, value)
        }

        withDrawable("Tests/MatteTimeStretchScan.json", "Mirror animation", "Mirror animation") {
            drawable ->
            drawable.addValueCallback(KeyPath.COMPOSITION, LottieProperty.TRANSFORM_ANCHOR_POINT,
                    { PointF(drawable.bounds.width().toFloat(), 0f) })
            drawable.addValueCallback(KeyPath.COMPOSITION, LottieProperty.TRANSFORM_SCALE,
                    { ScaleXY(-1.0f, 1.0f) })
        }

        withDrawable("Tests/Text.json", "Text", "Text Fill (Blue -> Green)") { drawable ->
            val value = object : LottieValueCallback<Int>() {
                override fun getValue(frameInfo: LottieFrameInfo<Int>?) = Color.GREEN
            }
            drawable.addValueCallback(KeyPath("Text"), LottieProperty.COLOR, value)
        }

        withDrawable("Tests/Text.json", "Text", "Text Stroke (Red -> Yellow)") { drawable ->
            val value = object : LottieValueCallback<Int>() {
                override fun getValue(frameInfo: LottieFrameInfo<Int>?) = Color.YELLOW
            }
            drawable.addValueCallback(KeyPath("Text"), LottieProperty.STROKE_COLOR, value)
        }

        withDrawable("Tests/Text.json", "Text", "Text Stroke Width") { drawable ->
            val value = object : LottieValueCallback<Float>() {
                override fun getValue(frameInfo: LottieFrameInfo<Float>?) = 200f
            }
            drawable.addValueCallback(KeyPath("Text"), LottieProperty.STROKE_WIDTH, value)
        }

        withDrawable("Tests/Text.json", "Text", "Text Tracking") { drawable ->
            val value = object : LottieValueCallback<Float>() {
                override fun getValue(frameInfo: LottieFrameInfo<Float>?) = 20f
            }
            drawable.addValueCallback(KeyPath("Text"), LottieProperty.TEXT_TRACKING, value)
        }

        withDrawable("Tests/Text.json", "Text", "Text Size") { drawable ->
            val value = object : LottieValueCallback<Float>() {
                override fun getValue(frameInfo: LottieFrameInfo<Float>?) = 60f
            }
            drawable.addValueCallback(KeyPath("Text"), LottieProperty.TEXT_SIZE, value)
        }
    }

    private suspend fun <T> testDynamicProperty(name: String, keyPath: KeyPath, property: T, callback: LottieValueCallback<T>, progress: Float = 0f) {
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

        withDrawable("Tests/RGBMarker.json", "Marker", "->[Green, Blue)") { drawable ->
            drawable.setMinAndMaxFrame("Green Section", "Blue Section", false)
            drawable.frame = drawable.minFrame.toInt()
        }

        withDrawable("Tests/RGBMarker.json", "Marker", "->[Green, Blue]") { drawable ->
            drawable.setMinAndMaxFrame("Green Section", "Blue Section", true)
            drawable.frame = drawable.minFrame.toInt()
        }

        withDrawable("Tests/RGBMarker.json", "Marker", "[Green, Blue)<-") { drawable ->
            drawable.setMinAndMaxFrame("Green Section", "Blue Section", false)
            drawable.frame = drawable.maxFrame.toInt()
        }

        withDrawable("Tests/RGBMarker.json", "Marker", "[Green, Blue]<-") { drawable ->
            drawable.setMinAndMaxFrame("Green Section", "Blue Section", true)
            drawable.frame = drawable.maxFrame.toInt()
        }
    }

    private suspend fun testText() {
        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "Hello World") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "Hello World")
        }

        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "Emoji") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "🔥💪💯")
        }

        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "Taiwanese") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "我的密碼")
        }

        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "Fire Taiwanese") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "🔥的A")
        }

        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "Family man man girl boy") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "\uD83D\uDC68\u200D\uD83D\uDC68\u200D\uD83D\uDC67\u200D\uD83D\uDC66")
        }

        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "Family woman woman girl girl") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "\uD83D\uDC69\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC67")
        }

        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "Brown Police Man") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "\uD83D\uDC6E\uD83C\uDFFF\u200D♀️")
        }

        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "Family and Brown Police Man") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "\uD83D\uDC68\u200D\uD83D\uDC68\u200D\uD83D\uDC67\u200D\uD83D\uDC67\uD83D\uDC6E\uD83C\uDFFF\u200D♀️")
        }

        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "Family, Brown Police Man, emoji and chars") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "🔥\uD83D\uDC68\u200D\uD83D\uDC68\u200D\uD83D\uDC67\u200D\uD83D\uDC67\uD83D\uDC6E\uD83C\uDFFF\u200D♀的Aabc️")
        }

        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "Fire English Fire Brown Police Man Fire") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "🔥c️🔥\uD83D\uDC6E\uD83C\uDFFF\u200D♀️\uD83D\uDD25")
        }

        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "American Flag") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "\uD83C\uDDFA\uD83C\uDDF8")
        }

        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "Checkered Flag") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "\uD83C\uDFC1")
        }

        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "Pirate Flag") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "\uD83C\uDFF4\u200D☠️")
        }

        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "3 Oclock") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "\uD83D\uDD52")
        }

        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "Woman frowning") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "\uD83D\uDE4D\u200D♀️")
        }

        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "Gay couple") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "\uD83D\uDC68\u200D❤️\u200D\uD83D\uDC68️")
        }

        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "Lesbian couple") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "\uD83D\uDC69\u200D❤️\u200D\uD83D\uDC69️")
        }

        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "Straight couple") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "\uD83D\uDC91")
        }
    }

    private suspend fun testNightMode() {
        var newConfig = Configuration(activity.getResources().getConfiguration())
		newConfig.uiMode = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv();
		newConfig.uiMode = newConfig.uiMode or Configuration.UI_MODE_NIGHT_NO;
		val dayContext = activity.createConfigurationContext(newConfig)
        var result = LottieCompositionFactory.fromRawResSync(dayContext, R.raw.day_night)
        var composition = result.value!!
        var drawable = LottieDrawable()
        drawable.setComposition(composition)
        var bitmap = bitmapPool.acquire(drawable.intrinsicWidth, drawable.intrinsicHeight)
        var canvas = Canvas(bitmap)
        log("Drawing day_night day")
        drawable.draw(canvas)
        snapshotter.record(bitmap, "Day/Night", "Day")
        activity.recordSnapshot("Day/Night", "Day")
        LottieCompositionCache.getInstance().clear()
        bitmapPool.release(bitmap)

        newConfig = Configuration(activity.getResources().getConfiguration())
        newConfig.uiMode = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv();
        newConfig.uiMode = newConfig.uiMode or Configuration.UI_MODE_NIGHT_YES;
        val nightContext = activity.createConfigurationContext(newConfig)
        result = LottieCompositionFactory.fromRawResSync(nightContext, R.raw.day_night)
        composition = result.value!!
        drawable = LottieDrawable()
        drawable.setComposition(composition)
        bitmap = bitmapPool.acquire(drawable.intrinsicWidth, drawable.intrinsicHeight)
        canvas = Canvas(bitmap)
        log("Drawing day_night day")
        drawable.draw(canvas)
        snapshotter.record(bitmap, "Day/Night", "Night")
        activity.recordSnapshot("Day/Night", "Night")
        LottieCompositionCache.getInstance().clear()
        bitmapPool.release(bitmap)
    }

    private suspend fun testApplyOpacityToLayer() {
        withFilmStripView(
                "Tests/OverlapShapeWithOpacity.json",
                "Apply Opacity To Layer",
                "Enabled"
        ) { filmStripView ->
            filmStripView.setApplyingOpacityToLayersEnabled(true)
        }
        withFilmStripView(
                "Tests/OverlapShapeWithOpacity.json",
                "Apply Opacity To Layer",
                "Disabled"
        ) { filmStripView ->
            filmStripView.setApplyingOpacityToLayersEnabled(false)
        }
    }

    private suspend fun withDrawable(assetName: String, snapshotName: String, snapshotVariant: String, callback: (LottieDrawable) -> Unit) {
        val result = LottieCompositionFactory.fromAssetSync(activity, assetName)
        val composition = result.value
                ?: throw IllegalArgumentException("Unable to parse $assetName.", result.exception)
        val drawable = LottieDrawable()
        drawable.setComposition(composition)
        callback(drawable)
        val bitmap = bitmapPool.acquire(drawable.intrinsicWidth, drawable.intrinsicHeight)
        val canvas = Canvas(bitmap)
        log("Drawing $assetName")
        drawable.draw(canvas)
        snapshotter.record(bitmap, snapshotName, snapshotVariant)
        activity.recordSnapshot(snapshotName, snapshotVariant)
        LottieCompositionCache.getInstance().clear()
        bitmapPool.release(bitmap)
    }

    private suspend fun withAnimationView(
            assetName: String,
            snapshotName: String = assetName,
            snapshotVariant: String = "default",
            callback: (LottieAnimationView) -> Unit
    ) {
        val result = LottieCompositionFactory.fromAssetSync(activity, assetName)
        val composition = result.value
                ?: throw IllegalArgumentException("Unable to parse $assetName.", result.exception)
        val animationView = animationViewPool.acquire()
        animationView.setComposition(composition)
        animationView.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        animationView.scale = 1f
        animationView.scaleType = ImageView.ScaleType.FIT_CENTER
        callback(animationView)
        val widthSpec = View.MeasureSpec.makeMeasureSpec(activity.resources.displayMetrics.widthPixels, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(activity.resources.displayMetrics.heightPixels, View.MeasureSpec.EXACTLY)
        val animationViewContainer = animationView.parent as ViewGroup
        animationViewContainer.measure(widthSpec, heightSpec)
        animationViewContainer.layout(0, 0, animationViewContainer.measuredWidth, animationViewContainer.measuredHeight)
        val bitmap = bitmapPool.acquire(animationView.width, animationView.height)
        val canvas = Canvas(bitmap)
        log("Drawing $assetName")
        animationView.draw(canvas)
        animationViewPool.release(animationView)
        snapshotter.record(bitmap, snapshotName, snapshotVariant)
        activity.recordSnapshot(snapshotName, snapshotVariant)
        bitmapPool.release(bitmap)
    }

    private suspend fun withFilmStripView(
            assetName: String,
            snapshotName: String = assetName,
            snapshotVariant: String = "default",
            callback: (FilmStripView) -> Unit
    ) {
        val result = LottieCompositionFactory.fromAssetSync(activity, assetName)
        val composition = result.value
                ?: throw IllegalArgumentException("Unable to parse $assetName.", result.exception)
        snapshotComposition(snapshotName, snapshotVariant, composition, callback)
    }

    private fun log(message: String) {
        Log.d("LottieTest", message)
    }

    private val Number.dp get() = this.toFloat() / (Resources.getSystem().displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}
