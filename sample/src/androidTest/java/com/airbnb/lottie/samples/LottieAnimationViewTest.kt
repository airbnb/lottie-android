package com.airbnb.lottie.samples

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.model.LottieCompositionCache
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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

    @Test
    fun testDoesNotLoadSameUrlTwice() {
        val url = "https://raw.githubusercontent.com/airbnb/lottie-android/master/sample/src/main/res/raw/heart.json"
        val cacheKey = "url_${url}"

        val idlingResource = LottieIdlingResource()
        IdlingRegistry.getInstance().register(idlingResource)

        class TestFragment : Fragment(R.layout.lottie_activity_main)

        val scenario = launchFragmentInContainer<TestFragment>()
        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onFragment { fragment ->
            val view = fragment.requireView().findViewById<LottieAnimationView>(R.id.animation_view)
            view.setAnimationFromUrl(url)
        }

        onIdle()
        assertNotNull(LottieCompositionCache.getInstance()[cacheKey])
        LottieCompositionCache.getInstance().clear()

        scenario.onFragment { fragment ->
            val view = fragment.requireView().findViewById<LottieAnimationView>(R.id.animation_view)
            view.setAnimationFromUrl(url)
        }

        // The second call to setAnimationFromUrl() using the same url should avoid reloading the composition
        // and thus the underlying cache would not have been re-populated.
        onIdle()
        assertNull(LottieCompositionCache.getInstance()[cacheKey])

        IdlingRegistry.getInstance().unregister(idlingResource)
    }
}
