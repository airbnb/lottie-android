package com.airbnb.lottie.samples

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.model.LottieCompositionCache
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
@LargeTest
class FragmentVisibilityTests {

    @Before
    fun setup() {
        LottieCompositionCache.getInstance().clear()
    }

    @Test
    fun testAutoPlay() {
        class TestFragment : Fragment() {
            override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
                return inflater.inflate(R.layout.auto_play, container, false)
            }

            override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
                IdlingRegistry.getInstance().register(LottieIdlingResource(view.findViewById(R.id.animation_view)))
            }
        }
        launchFragmentInContainer<TestFragment>()
        onView(withId(R.id.animation_view)).check(matches(isAnimating()))
    }

    /**
     * https://github.com/airbnb/lottie-android/issues/1216
     */
    @Test
    fun testPauseBeforeCompositionLoadedStopsAutoPlay() {
        class TestFragment : Fragment() {
            override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
                return inflater.inflate(R.layout.auto_play, container, false)
            }

            override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
                val animationView = requireView().findViewById<LottieAnimationView>(R.id.animation_view)
                animationView.pauseAnimation()
                IdlingRegistry.getInstance().register(LottieIdlingResource(animationView))
            }
        }
        launchFragmentInContainer<TestFragment>()
        onView(withId(R.id.animation_view)).check(matches(isNotAnimating()))
    }

    @Test
    fun testDoesntAnimateWithAutoplayWhenGone() {
        class TestFragment : Fragment() {
            override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
                return inflater.inflate(R.layout.auto_play_gone, container, false)
            }

            override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
                IdlingRegistry.getInstance().register(LottieIdlingResource(view.findViewById(R.id.animation_view)))
            }
        }

        val scenario = launchFragmentInContainer<TestFragment>()
        onView(withId(R.id.animation_view)).check(matches(isNotAnimating()))
        scenario.onFragment { frag ->
            frag.requireView().findViewById<LottieAnimationView>(R.id.animation_view).isVisible = true
        }
        onView(withId(R.id.animation_view)).check(matches(isAnimating()))
    }

    @Test
    fun testDoesntStopOnPause() {
        class TestFragment : Fragment() {
            lateinit var animationView: LottieAnimationView
            override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
                return inflater.inflate(R.layout.auto_play, container, false)
            }

            override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
                animationView = view.findViewById<LottieAnimationView>(R.id.animation_view)
                IdlingRegistry.getInstance().register(LottieIdlingResource(animationView))
                AlertDialog.Builder(requireContext()).setTitle("This is a dialog").show()
            }
        }

        val scenario = launchFragmentInContainer<TestFragment>(themeResId = R.style.Theme_AppCompat)
        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onFragment { fragment ->
            // Wait for the animation view.
            onView(withId(R.id.animation_view))
            // We have to use a property reference because the Fragment isn't resumed.
            assertTrue(fragment.animationView.isAnimating)
        }
    }

    @Test
    fun testStopsWhenLaunchingAnotherActivity() {
        class TestFragment : Fragment() {
            lateinit var animationView: LottieAnimationView
            val animationListener = mock<Animator.AnimatorListener>()

            override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
                return inflater.inflate(R.layout.auto_play, container, false)
            }

            override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
                animationView = view.findViewById<LottieAnimationView>(R.id.animation_view)
                animationView.addAnimatorListener(animationListener)
                IdlingRegistry.getInstance().register(LottieIdlingResource(animationView))
            }
        }

        val scenario1 = launchFragmentInContainer<TestFragment>()
        // Wait for the animation view.
        onView(withId(R.id.animation_view))

        // Launch a new activity
        scenario1.onFragment { fragment ->
            fragment.requireContext().startActivity(Intent(fragment.requireContext(), EmptyActivity::class.java))
        }
        onView(withId(R.id.finish))

        // Wait for the original Fragment to go from RESUMED to CREATED (Stopped).
        scenario1.waitForState(Lifecycle.State.CREATED)
        scenario1.onFragment { fragment ->
            assertFalse(fragment.animationView.isAnimating)
        }
        onView(withId(R.id.finish)).perform(ViewActions.click())

        scenario1.waitForState(Lifecycle.State.RESUMED)
        scenario1.onFragment { fragment ->
            onView(withId(R.id.animation_view))
            assertTrue(fragment.animationView.isAnimating)
        }
    }

    @Test
    fun testRecyclerViewCanAutoPlayInOnBind() {
        class TestFragment : Fragment() {
            override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
                return RecyclerView(requireContext()).apply {
                    layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                    adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                            return object : RecyclerView.ViewHolder(LottieAnimationView(parent.context).apply { id = R.id.animation_view }) {}
                        }

                        override fun getItemCount(): Int = 1

                        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                            (holder.itemView as LottieAnimationView).apply {
                                repeatMode = LottieDrawable.RESTART
                                setAnimation(R.raw.heart)
                                playAnimation()
                                IdlingRegistry.getInstance().register(LottieIdlingResource(this))
                            }
                        }
                    }
                }
            }
        }

        launchFragmentInContainer<TestFragment>()
        onView(withId(R.id.animation_view)).check(matches(isAnimating()))
    }

    @Test
    fun testDoesntAutoplay() {
        class TestFragment : Fragment() {
            override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
                return inflater.inflate(R.layout.no_auto_play, container, false)
            }

            override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
                IdlingRegistry.getInstance().register(LottieIdlingResource(view.findViewById(R.id.animation_view)))
            }
        }
        launchFragmentInContainer<TestFragment>()
        onView(withId(R.id.animation_view)).check(matches(isNotAnimating()))
    }

    @Test
    fun testDoesntAutoplayWhenManuallyPausedHiddenAndShown() {
        class TestFragment : Fragment() {
            override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
                return inflater.inflate(R.layout.auto_play, container, false)
            }

            override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
                IdlingRegistry.getInstance().register(LottieIdlingResource(view.findViewById(R.id.animation_view)))
            }
        }

        val scenario = launchFragmentInContainer<TestFragment>()
        onView(withId(R.id.animation_view)).check(matches(isAnimating()))
        scenario.onFragment { frag ->
            frag.animationView.pauseAnimation()
            frag.animationView.isVisible = false
        }
        scenario.onFragment { frag ->
            frag.animationView.isVisible = true
        }
        onView(withId(R.id.animation_view)).check(matches(isNotAnimating()))
    }

    @Test
    fun testCanPlayWhenCalledRightAway() {
        class TestFragment : Fragment() {
            override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
                return inflater.inflate(R.layout.no_auto_play, container, false)
            }

            override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
                val animationView = view.findViewById<LottieAnimationView>(R.id.animation_view)
                animationView.playAnimation()
                IdlingRegistry.getInstance().register(LottieIdlingResource(animationView))
            }
        }
        launchFragmentInContainer<TestFragment>()
        onView(withId(R.id.animation_view)).check(matches(isAnimating()))
    }

    @Test
    fun testResumesWhenManuallyPlayedHiddenAndShown() {
        class TestFragment : Fragment() {
            override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
                return inflater.inflate(R.layout.no_auto_play, container, false)
            }

            override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
                val animationView = view.findViewById<LottieAnimationView>(R.id.animation_view)
                animationView.playAnimation()
                IdlingRegistry.getInstance().register(LottieIdlingResource(animationView))
            }
        }

        val scenario = launchFragmentInContainer<TestFragment>()
        onView(withId(R.id.animation_view)).check(matches(isAnimating()))
        scenario.onFragment { it.animationView.isVisible = false }
        onView(withId(R.id.animation_view)).check(matches(isNotAnimating()))
        scenario.onFragment { it.animationView.isVisible = true }
        onView(withId(R.id.animation_view)).check(matches(isAnimating()))

    }

    @Test
    fun testDoesntPlayWhenAncestorIsNotVisibleButResumesWhenMadeVisible() {
        class TestFragment : Fragment() {
            override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
                return inflater.inflate(R.layout.auto_play, container, false)
            }

            override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
                view.findViewById<View>(R.id.container).isVisible = false
                IdlingRegistry.getInstance().register(LottieIdlingResource(view.findViewById(R.id.animation_view)))
            }
        }

        val scenario = launchFragmentInContainer<TestFragment>()
        onView(withId(R.id.animation_view)).check(matches(isNotAnimating()))
        scenario.onFragment { fragment ->
            fragment.requireView().findViewById<View>(R.id.container).isVisible = true
        }
        onView(withId(R.id.animation_view)).check(matches(isAnimating()))
    }

    @Test
    fun testPausesWhenScrolledOffScreenAndResumesWhenComesBack() {
        class TestFragment : Fragment() {
            var animationView: LottieAnimationView? = null

            override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
                return RecyclerView(requireContext()).apply {
                    layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                    adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                        var animationWasPlayed = false

                        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                            return when (viewType) {
                                0 -> object : RecyclerView.ViewHolder(LottieAnimationView(parent.context).apply { id = R.id.animation_view }) {}
                                else -> object : RecyclerView.ViewHolder(TextView(parent.context)) {}
                            }
                        }

                        override fun getItemCount(): Int = 1000

                        override fun getItemViewType(position: Int) = if (position == 0) 0 else 1

                        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                            if (holder.itemViewType == 0) bindLottieHolder(holder)
                            else bindOtherViewHolder(holder, position)
                        }

                        private fun bindLottieHolder(holder: RecyclerView.ViewHolder) {
                            if (!animationWasPlayed) {
                                animationView = holder.itemView as LottieAnimationView
                                (holder.itemView as LottieAnimationView).apply {
                                    //                                    repeatCount = LottieDrawable.INFINITE
                                    setAnimation(R.raw.heart)
                                    playAnimation()
                                    animationWasPlayed = true
                                    IdlingRegistry.getInstance().register(LottieIdlingResource(this, name = "Lottie ${Random.nextFloat()}"))
                                }
                            } else {
                                IdlingRegistry.getInstance().register(LottieIdlingAnimationResource(animationView, name = "Lottie finished animation ${Random.nextFloat()}"))
                            }
                        }

                        private fun bindOtherViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                            (holder.itemView as TextView).text = "Item $position"
                        }
                    }
                }
            }
        }

        val scenario = launchFragmentInContainer<TestFragment>()
        onView(isAssignableFrom(RecyclerView::class.java)).check(matches(isDisplayed()))
        scenario.onFragment { assertTrue(it.animationView!!.isAnimating) }
        scenario.onFragment { it.requireView().scrollBy(0, 10_000) }
        scenario.onFragment { assertFalse(it.animationView!!.isAnimating) }
        scenario.onFragment { it.requireView().scrollBy(0, -10_000) }
        scenario.onFragment { assertTrue(it.animationView!!.isAnimating) }
        onView(withId(R.id.animation_view)).check(matches(isDisplayed()))
        scenario.onFragment { assertFalse(it.animationView!!.isAnimating) }
    }

    @Test
    fun testPausesWhenScrolledOffScreenAndResumesWhenComesBackWithoutRepeatingWhenFinished() {

        class TestFragment : Fragment() {
            var animationView: LottieAnimationView? = null
            override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
                return RecyclerView(requireContext()).apply {
                    layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                    adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

                        var animationWasPlayed = false

                        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                            return when (viewType) {
                                0 -> object : RecyclerView.ViewHolder(
                                        LottieAnimationView(parent.context)
                                                .apply { id = R.id.animation_view }
                                ) {}
                                else -> object : RecyclerView.ViewHolder(TextView(parent.context)) {}
                            }
                        }

                        override fun getItemCount(): Int = 1000

                        override fun getItemViewType(position: Int) = position

                        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                            if (holder.itemViewType == 0) bindLottieHolder(holder)
                            else bindOtherViewHolder(holder, position)
                        }

                        private fun bindLottieHolder(holder: RecyclerView.ViewHolder) {
                            if (!animationWasPlayed) {
                                animationView = holder.itemView as LottieAnimationView
                                (holder.itemView as LottieAnimationView).apply {
                                    setAnimation(R.raw.heart)
                                    playAnimation()
                                    animationWasPlayed = true
                                    IdlingRegistry.getInstance().register(LottieIdlingResource(this, name = "Lottie ${Random.nextFloat()}"))
                                }
                            } else {
                                IdlingRegistry.getInstance().register(LottieIdlingAnimationResource(animationView, name = "Lottie finished animation ${Random.nextFloat()}"))
                            }
                        }

                        private fun bindOtherViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                            (holder.itemView as TextView).text = "Item $position"
                        }
                    }
                }
            }
        }

        val scenario = launchFragmentInContainer<TestFragment>()
        onView(isAssignableFrom(RecyclerView::class.java)).check(matches(isDisplayed()))
        scenario.onFragment { assertTrue(it.animationView!!.isAnimating) }
        scenario.onFragment { it.requireView().scrollBy(0, 10_000) }
        scenario.onFragment { assertFalse(it.animationView!!.isAnimating) }
        scenario.onFragment { it.requireView().scrollBy(0, -10_000) }
        scenario.onFragment { assertTrue(it.animationView!!.isAnimating) }
        onView(withId(R.id.animation_view)).check(matches(isDisplayed()))
        scenario.onFragment { assertFalse(it.animationView!!.isAnimating) }
        scenario.onFragment { it.requireView().scrollBy(0, 10_000) }
        scenario.onFragment { it.requireView().scrollBy(0, -10_000) }
        scenario.onFragment { assertFalse(it.animationView!!.isAnimating) }
    }

    @Test
    fun testResumesWhenStateSavedAndRestored() {
        class TestFragment : Fragment() {
            override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
                return inflater.inflate(R.layout.auto_play, container, false)
            }

            override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
                IdlingRegistry.getInstance().register(LottieIdlingResource(view.findViewById(R.id.animation_view)))
            }
        }

        val scenario = launchFragmentInContainer<TestFragment>()
        onView(withId(R.id.animation_view)).check(matches(isAnimating()))
        scenario.onFragment { frag ->
            val savedState = SparseArray<Parcelable>()
            val animationView = frag.requireView().findViewById<LottieAnimationView>(R.id.animation_view)
            val parent = animationView.parent as ViewGroup
            val fragmentViewParent = frag.requireView().parent as ViewGroup
            fragmentViewParent.removeView(parent)
            parent.saveHierarchyState(savedState)
            fragmentViewParent.addView(parent)
            parent.restoreHierarchyState(savedState)

        }
        onView(withId(R.id.animation_view)).check(matches(isAnimating()))
    }

    private fun FragmentScenario<*>.waitForState(desiredState: Lifecycle.State) {
        var isState = false
        while (!isState) {
            onFragment { fragment ->
                isState = fragment.lifecycle.currentState == desiredState
                Thread.sleep(200)
            }
        }
    }

    private val Fragment.animationView get() = requireView().findViewById<LottieAnimationView>(R.id.animation_view)
}