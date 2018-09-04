package com.airbnb.lottie

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Environment
import android.util.Log
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.airbnb.happo.SnapshotProvider
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.*
import java.io.File
import java.io.IOException
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

private class SnapshotInfo(val canvas: Canvas, val progress: Float)

class LottieSnapshotProvider internal constructor(private val context: Context) : SnapshotProvider() {

    private val queue = LinkedBlockingQueue<Runnable>()
    private val executor = ThreadPoolExecutor(CORES, CORES, 15, TimeUnit.MINUTES, queue)
    // Bitmap to return from an ImageAssetDelegate to make testing animations with images easier.
    private val dummyBitmap: Bitmap = BitmapFactory.decodeResource(context.resources, com.airbnb.lottie.samples.R.drawable.airbnb)
    private val renderBitmap: Bitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888)
    private val clearPaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }
    private val renderCanvas = Canvas(renderBitmap)
    private val snapshotInfos: List<SnapshotInfo> by lazy {
        ArrayList<SnapshotInfo>(25).apply {
            for (row in 0..4) {
                for (col in 0..4) {
                    val matrix = Matrix()
                    matrix.setRectToRect(
                            RectF(0f, 0f, 1000f, 1000f),
                            RectF(col * 200f, row * 200f, col * 200f + 200f, row * 200f + 200f),
                            Matrix.ScaleToFit.CENTER
                    )
                    val canvas = Canvas(renderBitmap)
                    canvas.matrix = matrix
                    canvas.scale(0.2f, 0.2f, 0f, 0f)
                    canvas.translate(col * 1000f, row * 1000f)

                    add(SnapshotInfo(canvas, 0.04f * (row * 5f + col)))
                }
            }
        }
    }

    private var remainingTasks = 0

    override fun beginSnapshotting() {
        Log.d(L.TAG, "beginSnapshotting")
        try {
            snapshotAssets(context.assets.list(""))
            val tests = context.assets.list("Tests")
            for (i in tests!!.indices) {
                tests[i] = "Tests/" + tests[i]
            }
            snapshotAssets(tests)

            val lottiefiles = context.assets.list("lottiefiles")
            for (i in lottiefiles!!.indices) {
                lottiefiles[i] = "lottiefiles/" + lottiefiles[i]
            }
            snapshotAssets(lottiefiles)
        } catch (e: IOException) {
            onError(e)
        }

        testFrameBoundary()
        testFrameBoundary2()
        testScaleTypes()
        testDynamicProperties()
        testSwitchingToDrawableAndBack()
        testStartEndFrameWithStartEndProgress()
        testUrl()
    }

    private fun snapshotAssets(animations: Array<String>?) {
        val dir = File(Environment.getExternalStorageDirectory().toString() + "/Snapshots")

        dir.mkdirs()
        for (file in dir.listFiles()) {

            file.delete()
        }
        for (animation in animations!!) {
            if (!animation.contains(".json") && !animation.contains(".zip")) {
                continue
            }
            remainingTasks += 1
            Log.d(L.TAG, "Enqueueing $animation")
            executor.execute {
                runAnimation(animation)
                decrementAndCompleteIfDone()
            }
        }
    }

    private fun runAnimation(name: String) {
        Log.d(L.TAG, "Running $name")
        val result = LottieCompositionFactory.fromAssetSync(context, name)
        if (result.exception != null) throw IllegalStateException(result.exception)
        val composition = result.value ?: return

        val bounds = composition.bounds
        val width = bounds.width()
        val height = bounds.height()
        val displayMetrics = Resources.getSystem().displayMetrics
        if (width > 4 * displayMetrics.widthPixels || height > 4 * displayMetrics.heightPixels) {
            Log.d("Happo", name + " is too large. Skipping (" + width + "x" + height + ")")
            return
        }
        drawComposition(composition, name)
    }

    private fun drawComposition(composition: LottieComposition, name: String) {
        Log.d(L.TAG, "Drawing $name")
        val drawable = LottieDrawable()
        drawable.composition = composition
        drawable.setImageAssetDelegate { dummyBitmap }
        drawable.scale = maxOf(composition.bounds.width(), composition.bounds.height()) / 200f

        val strokeWidth = 7f
        val outlinePaint = Paint().apply {
            style = Paint.Style.STROKE
            color = Color.RED
            this.strokeWidth = strokeWidth
        }

        clearBitmap()
        snapshotInfos.forEach {
            drawable.progress = it.progress
            drawable.draw(it.canvas)
            it.canvas.drawRect(strokeWidth, strokeWidth, it.canvas.width - strokeWidth, it.canvas.height - strokeWidth, outlinePaint)
        }
        recordSnapshot(renderBitmap, "android", name, "Main")
    }

    override fun stopSnapshotting() {
        queue.clear()
    }

    private fun decrementAndCompleteIfDone() {
        remainingTasks--
        Log.d(L.TAG, "There are $remainingTasks tasks left.")
        Log.d("Happo", "There are $remainingTasks remaining tasks.")
        if (remainingTasks < 0) {
            throw IllegalStateException("Remaining tasks cannot be negative.")
        }
        if (remainingTasks == 0) {
            onComplete()
        }
    }

    private fun testScaleTypes() {
        val composition = LottieComposition.Factory.fromFileSync(
                context, "LottieLogo1.json")

        var params = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        testScaleTypeView(context, composition, "Wrap Content", params, null, null)

        params = FrameLayout.LayoutParams(dpToPx(300), dpToPx(300))
        testScaleTypeView(context, composition, "300x300 @4x", params, null, 4f)

        params = FrameLayout.LayoutParams(dpToPx(300), dpToPx(300))
        testScaleTypeView(context, composition, "300x300 centerCrop", params,
                ImageView.ScaleType.CENTER_CROP, null)

        params = FrameLayout.LayoutParams(dpToPx(300), dpToPx(300))
        testScaleTypeView(context, composition, "300x300 centerInside", params,
                ImageView.ScaleType.CENTER_INSIDE, null)

        params = FrameLayout.LayoutParams(dpToPx(300), dpToPx(300))
        testScaleTypeView(context, composition, "300x300 centerInside @2x", params,
                ImageView.ScaleType.CENTER_INSIDE, 2f)

        params = FrameLayout.LayoutParams(dpToPx(300), dpToPx(300))
        testScaleTypeView(context, composition, "300x300 centerCrop @2x", params,
                ImageView.ScaleType.CENTER_CROP, 2f)

        params = FrameLayout.LayoutParams(dpToPx(300), dpToPx(300))
        testScaleTypeView(context, composition, "300x300 @2x", params, null, 2f)

        params = FrameLayout.LayoutParams(dpToPx(600), dpToPx(300))
        testScaleTypeView(context, composition, "600x300 centerInside", params,
                ImageView.ScaleType.CENTER_INSIDE, null)

        params = FrameLayout.LayoutParams(dpToPx(300), dpToPx(600))
        testScaleTypeView(context, composition, "300x600 centerInside", params,
                ImageView.ScaleType.CENTER_INSIDE, null)

        params = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        testScaleTypeView(context, composition, "Match Parent", params, null, null)
    }

    private fun testScaleTypeView(context: Context, composition: LottieComposition?,
                                  name: String, params: FrameLayout.LayoutParams, scaleType: ImageView.ScaleType?,
                                  scale: Float?) {
        val container = FrameLayout(context)
        val animationView = LottieAnimationView(context)
        animationView.setComposition(composition!!)
        animationView.progress = 1f
        if (scaleType != null) {
            animationView.scaleType = scaleType
        }
        if (scale != null) {
            animationView.scale = scale
        }
        container.addView(animationView, params)

        recordSnapshot(container, 1080, "android", "Scale Types", name, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
    }

    private fun testFrameBoundary() {
        val animationView = LottieAnimationView(context)
        val composition = LottieComposition.Factory.fromFileSync(context, "Tests/Frame.json")
        animationView.setComposition(composition!!)
        val params = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        animationView.frame = 16
        recordSnapshot(animationView, 1080, "android", "Frame Boundary", "Frame 16 Red", params)
        animationView.frame = 17
        recordSnapshot(animationView, 1080, "android", "Frame Boundary", "Frame 17 Blue", params)
        animationView.frame = 50
        recordSnapshot(animationView, 1080, "android", "Frame Boundary", "Frame 50 Blue", params)
        animationView.frame = 51
        recordSnapshot(animationView, 1080, "android", "Frame Boundary", "Frame 51 Green", params)
    }

    private fun testFrameBoundary2() {
        val animationView = LottieAnimationView(context)
        val composition = LottieComposition.Factory.fromFileSync(context, "Tests/RGB.json")
        animationView.setComposition(composition!!)
        val params = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        animationView.frame = 0
        recordSnapshot(animationView, 1080, "android", "Frame Boundary", "Frame 0 Red", params)
        animationView.frame = 1
        recordSnapshot(animationView, 1080, "android", "Frame Boundary", "Frame 1 Green", params)
        animationView.frame = 2
        recordSnapshot(animationView, 1080, "android", "Frame Boundary", "Frame 2 Blue", params)
    }

    private fun testDynamicProperties() {
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

        val blueColorFilter = LottieValueCallback<ColorFilter>(SimpleColorFilter(Color.GREEN))
        val animationView = LottieAnimationView(context)
        val composition = LottieComposition.Factory.fromFileSync(context, "Tests/Shapes.json")
        animationView.setComposition(composition!!)
        animationView.progress = 0f
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        animationView.addValueCallback(KeyPath("**"), LottieProperty.COLOR_FILTER, blueColorFilter)
        recordSnapshot(animationView, 1080, "android", "Dynamic Properties", "Color Filter before blue", params)
        blueColorFilter.setValue(SimpleColorFilter(Color.BLUE))
        recordSnapshot(animationView, 1080, "android", "Dynamic Properties", "Color Filter after blue", params)

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

    private fun <T> testDynamicProperty(
            name: String, keyPath: KeyPath, property: T, callback: LottieValueCallback<T>) {
        testDynamicProperty(name, keyPath, property, callback, 0f)
    }

    private fun <T> testDynamicProperty(
            name: String, keyPath: KeyPath, property: T, callback: LottieValueCallback<T>, progress: Float) {
        val animationView = LottieAnimationView(context)
        val composition = LottieComposition.Factory.fromFileSync(context, "Tests/Shapes.json")
        animationView.setComposition(composition!!)
        animationView.progress = progress
        val params = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        animationView.addValueCallback(keyPath, property, callback)
        recordSnapshot(animationView, 1080, "android", "Dynamic Properties", name, params)
    }

    private fun testSwitchingToDrawableAndBack() {
        val composition = LottieComposition.Factory.fromFileSync(context, "Tests/Shapes.json")
        val view = LottieAnimationView(context)
        view.setComposition(composition!!)
        view.setImageDrawable(ColorDrawable(Color.RED))
        view.setComposition(composition)
        val params = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        recordSnapshot(view, 1080, "android", "Reset Animation", "Drawable and back", params)
    }

    private fun testStartEndFrameWithStartEndProgress() {
        var composition = LottieComposition.Factory.fromFileSync(context, "Tests/StartEndFrame.json")
        var view = LottieAnimationView(context)
        view.setComposition(composition!!)
        val params = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        view.setMinProgress(0f)
        view.progress = 0f
        recordSnapshot(view, 1080, "android", "MinMaxFrame", "minProgress 0", params)
        view.setMinProgress(0.25f)
        view.progress = 0f
        recordSnapshot(view, 1080, "android", "MinMaxFrame", "minProgress 0.25", params)
        view.setMinProgress(0.75f)
        view.progress = 0f
        recordSnapshot(view, 1080, "android", "MinMaxFrame", "minProgress 0.75", params)
        view.setMinProgress(1f)
        view.progress = 0f
        recordSnapshot(view, 1080, "android", "MinMaxFrame", "minProgress 1", params)

        view.setMaxProgress(0f)
        view.progress = 1f
        recordSnapshot(view, 1080, "android", "MinMaxFrame", "maxProgress 0", params)
        view.setMaxProgress(0.25f)
        view.progress = 1f
        recordSnapshot(view, 1080, "android", "MinMaxFrame", "maxProgress 0.25", params)
        view.setMaxProgress(0.75f)
        view.progress = 1f
        recordSnapshot(view, 1080, "android", "MinMaxFrame", "maxProgress 0.75", params)
        view.setMaxProgress(1f)
        view.progress = 1f
        recordSnapshot(view, 1080, "android", "MinMaxFrame", "maxProgress 1", params)

        composition = LottieComposition.Factory.fromFileSync(context, "Tests/EndFrame.json")
        view = LottieAnimationView(context)
        view.setComposition(composition!!)
        view.frame = 29
        recordSnapshot(view, 1080, "android", "EndFrame", "End Frame (red)", params)
        view.frame = 30
        recordSnapshot(view, 1080, "android", "EndFrame", "End Frame (blue)", params)

    }

    private fun testUrl() {
        val composition = LottieCompositionFactory.fromUrlSync(context, "https://www.lottiefiles.com/download/427").value ?: return
        drawComposition(composition, "GiftBox from LottieFiles URL (427)")
    }

    private fun dpToPx(dp: Int): Int {
        val resources = context.resources
        return TypedValue.applyDimension(1, dp.toFloat(), resources.displayMetrics).toInt()
    }

    private fun clearBitmap() {
        renderCanvas.drawRect(0f, 0f, renderBitmap.width.toFloat(), renderBitmap.height.toFloat(), clearPaint)
    }

    companion object {
        private val CORES = 1 //Runtime.getRuntime().availableProcessors();
    }
}
