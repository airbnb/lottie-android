package com.airbnb.lottie.sample.compose.benchmark

import android.content.Context
import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

@RunWith(AndroidJUnit4::class)
class LottieBaselineBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    private lateinit var context: Context
    private lateinit var device: UiDevice

    @Before
    fun setUp() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        context = ApplicationProvider.getApplicationContext()
        device = UiDevice.getInstance(instrumentation)
    }

    @Test
    fun benchmarkNoCompilation() {
        benchmark(CompilationMode.None())
    }

    @Test
    fun benchmarkBaselineProfiles() {
        benchmark(CompilationMode.Partial(baselineProfileMode = BaselineProfileMode.Require))
    }

    fun benchmark(compilationMode: CompilationMode) {
        benchmarkRule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(StartupTimingMetric(), FrameTimingMetric()),
            iterations = 1,
            startupMode = StartupMode.COLD,
            compilationMode = compilationMode,
            setupBlock = {
                pressHome()

            }
        ) {
            startActivityAndWait()
            // Find the ScrollView in the Showcase route
            val selector = UiSelector().className("android.widget.ScrollView")
            val scrollable = UiScrollable(selector)
            scrollable.waitUntilReady()
            for (i in 0 until scrollable.childCount) {
                val childSelector = UiSelector()
                    .enabled(true)
                    .clickable(true)
                    .instance(i)
                val child = scrollable.getChild(childSelector)
                scrollable.scrollIntoView(child)
                child.clickAndWait()
            }
        }
    }

    private fun UiScrollable.waitUntilReady() {
        this.waitUntil {
            // We know that there are at least 9 children
            childCount >= EXPECTED_ITEM_INDEX_COUNT
        }
    }

    private fun UiObject.clickAndWait(maxWaitTime: Duration = 5.seconds) {
        val maxWaitTimeMs = maxWaitTime.toLong(DurationUnit.MILLISECONDS)
        click()
        device.waitForIdle(maxWaitTimeMs)
        Thread.sleep(maxWaitTimeMs)
        device.pressBack()
    }

    private fun <T> T.waitUntil(maxWaitTime: Duration = 5.seconds, condition: (T) -> Boolean) {
        var waitTime = 0L
        val maxWaitTimeMs = maxWaitTime.toLong(DurationUnit.MILLISECONDS)
        val incrementalDelay = 150L
        while (waitTime <= maxWaitTimeMs) {
            val ready = condition(this)
            if (ready) {
                break
            }
            Thread.sleep(incrementalDelay)
            waitTime += incrementalDelay
        }
    }

    companion object {
        private const val PACKAGE_NAME = "com.airbnb.lottie.sample.compose"
        private const val EXPECTED_ITEM_INDEX_COUNT = 9
    }
}
