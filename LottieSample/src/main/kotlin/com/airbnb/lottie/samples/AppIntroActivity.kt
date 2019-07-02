package com.airbnb.lottie.samples

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Interpolator
import android.widget.Scroller
import com.airbnb.lottie.LottieAnimationView
import com.matthewtamlin.sliding_intro_screen_library.buttons.IntroButton
import com.matthewtamlin.sliding_intro_screen_library.core.IntroActivity
import com.matthewtamlin.sliding_intro_screen_library.core.LockableViewPager


class AppIntroActivity : IntroActivity() {
    private val ANIMATION_TIMES = floatArrayOf(0f, 0.3333f, 0.6666f, 1f, 1f)

    private val animationView: LottieAnimationView by lazy {
        rootView.inflate(R.layout.app_intro_animation_view, false) as LottieAnimationView
    }
    private val viewPager: LockableViewPager by lazy {
        findViewById<LockableViewPager>(R.id.intro_activity_viewPager)
    }

    override fun generatePages(savedInstanceState: Bundle?): Collection<Fragment> {
        return listOf(
                EmptyFragment.newInstance(),
                EmptyFragment.newInstance(),
                EmptyFragment.newInstance(),
                EmptyFragment.newInstance()
        )
    }

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

    class EmptyFragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return container!!.inflate(R.layout.fragment_empty, false)
        }

        companion object {
            internal fun newInstance(): EmptyFragment {
                return EmptyFragment()
            }
        }
    }

    private fun setViewPagerScroller() {
        try {
            val scrollerField = ViewPager::class.java.getDeclaredField("mScroller")
            scrollerField.isAccessible = true
            val interpolator = ViewPager::class.java.getDeclaredField("sInterpolator")
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
}
