package com.airbnb.lottie.samples

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.sample.compose.ComposeActivity
import com.airbnb.lottie.sample.compose.R
import org.junit.Rule
import org.junit.Test

class WalkthroughAnimationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule(ComposeActivity::class.java)

    @Test
    fun testWalkthroughCompletes() {
        val composition = LottieCompositionFactory.fromRawResSync(composeTestRule.activity, R.raw.walkthrough).value!!
        var animationCompleted = false

        composeTestRule.setContent {
            val progress by animateLottieCompositionAsState(composition, iterations = 1)

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                LottieAnimation(
                    composition,
                    { progress },
                )
            }

            if (progress == 1f) {
                animationCompleted = true
            }
        }

        composeTestRule.mainClock.advanceTimeUntil { animationCompleted }
    }
}