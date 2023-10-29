package com.airbnb.lottie.sample.compose.benchmark

import android.content.Context
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LottieBaselineProfiles {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    private lateinit var context: Context
    private lateinit var device: UiDevice

    @Before
    fun setUp() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        context = ApplicationProvider.getApplicationContext()
        device = UiDevice.getInstance(instrumentation)
    }

    @Test
    fun baselineProfiles() {
        baselineProfileRule.collect(
            packageName = "com.airbnb.lottie.benchmark.app",
        ) {
            pressHome()
            startActivityAndWait()
            Thread.sleep(5_000L)
        }
    }
}
