package com.airbnb.lottie

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Build
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams

import com.airbnb.happo.HappoRunner
import com.airbnb.lottie.samples.MainActivity
import com.airbnb.lottie.samples.TestColorFilterActivity
import com.airbnb.lottie.samples.views.FilmStripView
import kotlinx.coroutines.runBlocking

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.IllegalStateException

private const val SIZE_PX = 1000

/**
 * Run these with: ./gradlew recordMode screenshotTests
 * If you run that command, it completes successfully, and nothing shows up in git, then you
 * haven't broken anything!
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class LottieTest {

    @Rule
    val mainActivityRule = ActivityTestRule(MainActivity::class.java)

    private val context get() = mainActivityRule.activity
    private val dummyBitmap by lazy { BitmapFactory.decodeResource(context.getResources(), R.drawable.airbnb); }
    private val bitmap = Bitmap.createBitmap(SIZE_PX, SIZE_PX, Bitmap.Config.ARGB_8888)
    private val canvas = Canvas(bitmap)

    @Rule
    var colorFilterActivityRule = ActivityTestRule(TestColorFilterActivity::class.java)

    @Rule
    var permissionRule = GrantPermissionRule.grant(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    )

    @Test
    fun testAll() {
        Log.d(L.TAG, "Beginning tests")
        val snapshotter = HappoSnapshotter(context)
        val view = newView()
        val composition = LottieCompositionFactory.fromRawResSync(context, R.raw.hamburger_arrow).value
                ?: throw IllegalStateException("Unable to parse animatioj")
        view.setComposition(composition)
        view.draw(canvas)
        snapshotter.record("Hamburger Arrow", bitmap)
        runBlocking {
            snapshotter.finalizeReportAndUpload()
        }
    }

    private fun newView() = FilmStripView(context).apply {
        setImageAssetDelegate(ImageAssetDelegate { dummyBitmap })
        val spec = View.MeasureSpec.makeMeasureSpec(SIZE_PX, View.MeasureSpec.EXACTLY)
        measure(spec, spec)
    }
}
