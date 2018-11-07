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
import com.airbnb.lottie.samples.R as SampleAppR

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.IllegalStateException

private const val SIZE_PX = 200

/**
 * Run these with: ./gradlew recordMode screenshotTests
 * If you run that command, it completes successfully, and nothing shows up in git, then you
 * haven't broken anything!
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class LottieTest {

    private val dummyBitmap by lazy { BitmapFactory.decodeResource(activity.getResources(), SampleAppR.drawable.airbnb); }

    @get:Rule
    var snapshotActivityRule = ActivityTestRule(FilmStripSnapshotTestActivity::class.java)
    private val activity get() = snapshotActivityRule.activity

    @get:Rule
    var permissionRule = GrantPermissionRule.grant(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    )

    @Test
    fun testAll() {
        Log.d(L.TAG, "Beginning tests")
        val snapshotter = HappoSnapshotter(activity)
        runBlocking {
            val composition = LottieCompositionFactory.fromRawResSync(activity, SampleAppR.raw.hamburger_arrow).value
                    ?: throw IllegalStateException("Unable to parse animation")
            val bitmap = activity.snapshot(composition)
            snapshotter.record("Hamburger Arrow", bitmap)
            snapshotter.finalizeReportAndUpload()
        }
    }

    private fun newView(): FilmStripView {
        val view = FilmStripView(activity)
        val parent = FrameLayout(activity)
        parent.addView(view)
        view.updateLayoutParams<FrameLayout.LayoutParams> {
            width = SIZE_PX
            height = SIZE_PX
        }
        return view
    }
}
