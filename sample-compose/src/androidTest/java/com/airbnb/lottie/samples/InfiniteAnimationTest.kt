package com.airbnb.lottie.samples

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.sample.compose.ComposeActivity
import com.airbnb.lottie.sample.compose.R
import org.junit.Rule
import org.junit.Test

class InfiniteAnimationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule(ComposeActivity::class.java)

    @Test
    fun testInfiniteAnimation() {
        val composition = LottieCompositionFactory.fromRawResSync(composeTestRule.activity, R.raw.heart).value!!
        composeTestRule.setContent {
            val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                LottieAnimation(
                    composition,
                    { progress },
                )
                Text(
                    "Composition Loaded!",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(
                            top = 16.dp
                        )
                )
            }
        }


        composeTestRule
            .onNodeWithText("Composition Loaded!")
            .assertIsDisplayed()
    }
}