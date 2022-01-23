package com.airbnb.lottie.samples

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

    @Test
    fun testCanSetAnAnimationAndChangeItBack() {
        class TestFragment : Fragment(R.layout.lottie_activity_main)

        val scenario = launchFragmentInContainer<TestFragment>()
        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onFragment { fragment ->
            val composition = LottieCompositionFactory.fromRawResSync(fragment.requireContext(), R.raw.hamburger_arrow).value!!
            val view = fragment.requireView().findViewById<LottieAnimationView>(R.id.animation_view)
            view.setComposition(composition)
            assertTrue(view.drawable is LottieDrawable)
            view.setImageDrawable(ColorDrawable(Color.GREEN))
            assertTrue(view.drawable is ColorDrawable)
            view.setComposition(composition)
            assertTrue(view.drawable is LottieDrawable)
        }
    }

    @Test
    fun testStopsPlayingWhenDrawableSwitched() {
        class TestFragment : Fragment(R.layout.lottie_activity_main)

        val scenario = launchFragmentInContainer<TestFragment>()
        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onFragment { fragment ->
            val composition = LottieCompositionFactory.fromRawResSync(fragment.requireContext(), R.raw.hamburger_arrow).value!!
            val view = fragment.requireView().findViewById<LottieAnimationView>(R.id.animation_view)
            view.setComposition(composition)
            view.playAnimation()
            view.setImageDrawable(ColorDrawable(Color.GREEN))
            assertFalse(view.isAnimating)
        }
    }
}
