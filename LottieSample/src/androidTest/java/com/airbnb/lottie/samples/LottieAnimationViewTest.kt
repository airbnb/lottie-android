package com.airbnb.lottie.samples

import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class LottieAnimationViewTest {
    @Test
    fun inflateShouldNotCrash() {
        class TestFragment : Fragment(R.layout.lottie_activity_main)
        launchFragmentInContainer<TestFragment>()
        onView(withId(R.id.animation_view)).check(matches(isDisplayed()))
    }
}
