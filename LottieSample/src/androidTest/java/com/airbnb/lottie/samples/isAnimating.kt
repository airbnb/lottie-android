package com.airbnb.lottie.samples


import android.view.View
import com.airbnb.lottie.LottieAnimationView
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

fun isAnimating(): Matcher<View> = IsAnimating()

class IsAnimating : TypeSafeMatcher<View>() {
    override fun describeTo(description: Description) {
        description.appendText("is animating")
    }

    override fun matchesSafely(item: View) = (item as? LottieAnimationView)?.isAnimating == true
}

fun isNotAnimating(): Matcher<View> = IsNotAnimating()

class IsNotAnimating : TypeSafeMatcher<View>() {
    override fun describeTo(description: Description) {
        description.appendText("is not animating")
    }

    override fun matchesSafely(item: View) = (item as? LottieAnimationView)?.isAnimating == false
}