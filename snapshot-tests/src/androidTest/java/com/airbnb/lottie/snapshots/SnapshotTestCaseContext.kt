package com.airbnb.lottie.snapshots

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.doOnLayout
import com.airbnb.lottie.FontAssetDelegate
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.RenderMode
import com.airbnb.lottie.model.LottieCompositionCache
import com.airbnb.lottie.snapshots.utils.BitmapPool
import com.airbnb.lottie.snapshots.utils.HappoSnapshotter
import com.airbnb.lottie.snapshots.utils.ObjectPool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * Set of properties that are available to all [SnapshotTestCase] runs.
 */
interface SnapshotTestCaseContext {
    val context: Context
    val snapshotter: HappoSnapshotter
    val bitmapPool: BitmapPool
    val animationViewPool: ObjectPool<LottieAnimationView>
    val filmStripViewPool: ObjectPool<FilmStripView>
    fun onActivity(callback: (SnapshotTestActivity) -> Unit)
}

@Suppress("unused")
fun SnapshotTestCaseContext.log(message: String) {
    Log.d("LottieTestCase", message)
}

suspend fun SnapshotTestCaseContext.withDrawable(
    assetName: String,
    snapshotName: String,
    snapshotVariant: String,
    callback: (LottieDrawable) -> Unit,
) {
    val result = LottieCompositionFactory.fromAssetSync(context, assetName)
    val composition = result.value ?: throw IllegalArgumentException("Unable to parse $assetName.", result.exception)
    val drawable = LottieDrawable()
    drawable.composition = composition
    callback(drawable)
    val bitmap = bitmapPool.acquire(drawable.intrinsicWidth, drawable.intrinsicHeight)
    val canvas = Canvas(bitmap)
    log("Drawing $assetName")
    drawable.draw(canvas)
    snapshotter.record(bitmap, snapshotName, snapshotVariant)
    LottieCompositionCache.getInstance().clear()
    bitmapPool.release(bitmap)
}

suspend fun SnapshotTestCaseContext.withAnimationView(
    assetName: String,
    snapshotName: String = assetName,
    snapshotVariant: String = "default",
    widthPx: Int = context.resources.displayMetrics.widthPixels,
    heightPx: Int = context.resources.displayMetrics.heightPixels,
    renderHardwareAndSoftware: Boolean = false,
    callback: (LottieAnimationView) -> Unit,
) {
    val result = LottieCompositionFactory.fromAssetSync(context, assetName)
    val composition = result.value ?: throw IllegalArgumentException("Unable to parse $assetName.", result.exception)
    val animationView = animationViewPool.acquire()
    animationView.setComposition(composition)
    animationView.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    animationView.scale = 1f
    animationView.scaleType = ImageView.ScaleType.FIT_CENTER
    callback(animationView)
    val animationViewContainer = animationView.parent as ViewGroup
    val widthSpec = View.MeasureSpec.makeMeasureSpec(
        widthPx,
        View.MeasureSpec.EXACTLY,
    )
    val heightSpec: Int = View.MeasureSpec.makeMeasureSpec(
        heightPx,
        View.MeasureSpec.EXACTLY,
    )
    animationViewContainer.measure(widthSpec, heightSpec)
    animationViewContainer.layout(0, 0, animationViewContainer.measuredWidth, animationViewContainer.measuredHeight)
    val bitmap = bitmapPool.acquire(animationView.width, animationView.height)
    val canvas = Canvas(bitmap)
    if (renderHardwareAndSoftware) {
        log("Drawing $assetName - hardware")
        val renderMode = animationView.renderMode
        animationView.renderMode = RenderMode.HARDWARE
        animationView.draw(canvas)
        snapshotter.record(bitmap, snapshotName, "$snapshotVariant - Hardware")

        bitmap.eraseColor(0)
        animationView.renderMode = RenderMode.SOFTWARE
        animationView.draw(canvas)
        animationViewPool.release(animationView)
        snapshotter.record(bitmap, snapshotName, "$snapshotVariant - Software")
        animationView.renderMode = renderMode
    } else {
        log("Drawing $assetName")
        animationView.draw(canvas)
        animationViewPool.release(animationView)
        snapshotter.record(bitmap, snapshotName, snapshotVariant)
    }
    bitmapPool.release(bitmap)
}

suspend fun SnapshotTestCaseContext.withFilmStripView(
    assetName: String,
    snapshotName: String = assetName,
    snapshotVariant: String = "default",
    callback: (FilmStripView) -> Unit,
) {
    val result = LottieCompositionFactory.fromAssetSync(context, assetName)
    val composition = result.value ?: throw IllegalArgumentException("Unable to parse $assetName.", result.exception)
    snapshotComposition(snapshotName, snapshotVariant, composition, callback)
}

suspend fun SnapshotTestCaseContext.snapshotComposition(
    name: String,
    variant: String = "default",
    composition: LottieComposition,
    callback: ((FilmStripView) -> Unit)? = null,
) = withContext(Dispatchers.Default) {
    log("Snapshotting $name")
    val spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    val filmStripView = filmStripViewPool.acquire()
    filmStripView.setOutlineMasksAndMattes(false)
    filmStripView.setApplyingOpacityToLayersEnabled(false)
    filmStripView.setImageAssetDelegate { BitmapFactory.decodeResource(context.resources, R.drawable.airbnb) }
    filmStripView.setFontAssetDelegate(object : FontAssetDelegate() {
        override fun getFontPath(fontFamily: String?): String {
            return "fonts/Roboto.ttf"
        }
    })
    callback?.invoke(filmStripView)
    filmStripView.measure(spec, spec)
    filmStripView.layout(0, 0, filmStripView.measuredWidth, filmStripView.measuredHeight)
    val bitmap = bitmapPool.acquire(filmStripView.width, filmStripView.height)
    val canvas = Canvas(bitmap)
    filmStripView.setComposition(composition, name)
    canvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR)
    withContext(Dispatchers.Main) {
        log("Drawing $name")
        filmStripView.draw(canvas)
    }
    filmStripViewPool.release(filmStripView)
    LottieCompositionCache.getInstance().clear()
    snapshotter.record(bitmap, name, variant)
    bitmapPool.release(bitmap)
}

/**
 * Use this to signal that the composition is not ready to be snapshot yet.
 * This use useful if you are using things like `rememberLottieComposition` which parses a composition asynchronously.
 */
val LocalSnapshotReady = compositionLocalOf { MutableStateFlow<Boolean?>(true) }

fun SnapshotTestCaseContext.loadCompositionFromAssetsSync(fileName: String): LottieComposition {
    return LottieCompositionFactory.fromAssetSync(context, fileName).value!!
}

suspend fun SnapshotTestCaseContext.snapshotComposable(
    name: String,
    variant: String = "default",
    renderHardwareAndSoftware: Boolean = false,
    content: @Composable (RenderMode) -> Unit,
) = withContext(Dispatchers.Default) {
    log("Snapshotting $name")
    val composeView = ComposeView(context)
    composeView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    var bitmap = withContext(Dispatchers.Main) {
        val readyFlow = MutableStateFlow<Boolean?>(null)
        composeView.setContent {
            CompositionLocalProvider(LocalSnapshotReady provides readyFlow) {
                content(RenderMode.SOFTWARE)
            }
            if (readyFlow.value == null) readyFlow.value = true
        }
        lateinit var onDrawListener: ViewTreeObserver.OnDrawListener
        suspendCancellableCoroutine<Bitmap> { cont ->
            onDrawListener = ViewTreeObserver.OnDrawListener {
                composeView.viewTreeObserver.addOnDrawListener(onDrawListener)
                composeView.post {
                    if (readyFlow.value != true) {
                        return@post
                    }
                    log("Drawing $name - Software")
                    val b = bitmapPool.acquire(composeView.width, composeView.height)
                    val canvas = Canvas(b)
                    composeView.viewTreeObserver.removeOnDrawListener(onDrawListener)
                    composeView.draw(canvas)
                    cont.resume(b)
                }
            }
            onActivity { activity ->
                activity.binding.content.addView(composeView)
            }
        }
    }
    onActivity { activity ->
        activity.binding.content.removeView(composeView)
    }
    snapshotter.record(bitmap, name, if (renderHardwareAndSoftware) "$variant - Software" else variant)
    bitmapPool.release(bitmap)

    if (renderHardwareAndSoftware) {
        bitmap = withContext(Dispatchers.Main) {
            val readyFlow = MutableStateFlow<Boolean?>(null)
            composeView.setContent {
                CompositionLocalProvider(LocalSnapshotReady provides readyFlow) {
                    content(RenderMode.HARDWARE)
                }
                if (readyFlow.value == null) readyFlow.value = true
            }
            lateinit var onDrawListener: ViewTreeObserver.OnDrawListener
            suspendCancellableCoroutine { cont ->
                onDrawListener = ViewTreeObserver.OnDrawListener {
                    composeView.viewTreeObserver.addOnDrawListener(onDrawListener)
                    composeView.post {
                        if (readyFlow.value != true) {
                            return@post
                        }
                        log("Drawing $name - Hardware")
                        val b = bitmapPool.acquire(composeView.width, composeView.height)
                        val canvas = Canvas(b)
                        composeView.viewTreeObserver.removeOnDrawListener(onDrawListener)
                        composeView.draw(canvas)
                        cont.resume(b)
                    }
                }
                onActivity { activity ->
                    activity.binding.content.addView(composeView)
                }
            }
        }
        onActivity { activity ->
            activity.binding.content.removeView(composeView)
        }
        snapshotter.record(bitmap, name, if (renderHardwareAndSoftware) "$variant - Hardware" else variant)
        bitmapPool.release(bitmap)
    }

    LottieCompositionCache.getInstance().clear()
}