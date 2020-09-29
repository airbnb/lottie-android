package com.airbnb.lottie.samples

import android.os.Bundle
import android.view.animation.Interpolator
import android.widget.Scroller
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.samples.utils.inflate
import com.airbnb.lottie.samples.utils.lerp
import com.matthewtamlin.sliding_intro_screen_library.buttons.IntroButton
import com.matthewtamlin.sliding_intro_screen_library.core.IntroActivity
import com.matthewtamlin.sliding_intro_screen_library.core.LockableViewPager


class AppIntroActivity : IntroActivity() {
    private val animationView: LottieAnimationView by lazy {
        rootView.inflate(R.layout.app_intro_animation_view, false) as LottieAnimationView
    }
    private val viewPager: LockableViewPager by lazy {
        findViewById(R.id.intro_activity_viewPager)
    }

    override fun generatePages(savedInstanceState: Bundle?) = listOf(EmptyFragment(), EmptyFragment(), EmptyFragment(), EmptyFragment())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootView.addView(animationView, 0)
        setViewPagerScroller()

        addPageChangeListener(OnPageChangeListenerAdapter(
            onPageScrolled = { position, positionOffset, _ ->
                setAnimationProgress(position, positionOffset)
            }
        ))
    }

    override fun generateFinalButtonBehaviour(): IntroButton.Behaviour {
        return object : IntroButton.Behaviour {
            override fun setActivity(activity: IntroActivity) { finish() }
            override fun getActivity(): IntroActivity? = null
            override fun run() {}
        }
    }

    private fun setAnimationProgress(position: Int, positionOffset: Float) {
        val startProgress = ANIMATION_TIMES[position]
        val endProgress = ANIMATION_TIMES[position + 1]

        animationView.progress = startProgress.lerp(endProgress, positionOffset)
    }

    class EmptyFragment : Fragment(R.layout.empty_fragment)

    private fun setViewPagerScroller() {
        try {
            val scrollerField = androidx.viewpager.widget.ViewPager::class.java.getDeclaredField("mScroller")
            scrollerField.isAccessible = true
            val interpolator = androidx.viewpager.widget.ViewPager::class.java.getDeclaredField("sInterpolator")
            interpolator.isAccessible = true

            val scroller = object : Scroller(this, interpolator.get(null) as Interpolator) {
                override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
                    super.startScroll(startX, startY, dx, dy, duration * 7)
                }
            }
            scrollerField.set(viewPager, scroller)
        } catch (e: NoSuchFieldException) {
            // Do nothing.
        } catch (e: IllegalAccessException) {
            // Do nothing.
        }
    }

    companion object {
        private val ANIMATION_TIMES = floatArrayOf(0f, 0.3333f, 0.6666f, 1f, 1f)
    }
}
