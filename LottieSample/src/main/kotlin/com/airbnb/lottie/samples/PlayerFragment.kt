package com.airbnb.lottie.samples

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.transition.AutoTransition
import android.support.transition.TransitionManager
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.widget.RelativeLayout
import androidx.view.isInvisible
import androidx.view.isVisible
import com.airbnb.lottie.L
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.OnCompositionLoadedListener
import com.airbnb.lottie.samples.views.BackgroundColorView
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.fragment_player.*

class PlayerFragment : Fragment(), OnCompositionLoadedListener {

    private val transition = AutoTransition().apply {
        duration = 250
    }
    private var renderTimeGraphRange = 4f
    private val lineDataSet by lazy {
        val entries = ArrayList<Entry>(101)
        repeat(101) { i ->
            entries.add(Entry(i.toFloat(), 0f))
        }
        LineDataSet(entries, "Render Times").apply {
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.3f
            setDrawCircles(false)
            lineWidth = 1.8f
            color = Color.BLACK
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_player, container, false)

    private val animatorListener = AnimatorListenerAdapter(
            onStart = { playButton.isActivated = true },
            onEnd = {
                playButton.isActivated = false
                animationView.performanceTracker?.logRenderTimes()
            },
            onCancel = { playButton.isActivated = false },
            onRepeat = {
                animationView.performanceTracker?.logRenderTimes()
                animationView.performanceTracker?.clearRenderTimes()
            }
    )

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)
        toolbar.title = ""

        val compositionArgs = arguments?.getParcelable<CompositionArgs>(EXTRA_ANIMATION_ARGS) ?: throw IllegalArgumentException("No composition args specified")
        CompositionLoader(requireContext(), compositionArgs, this)

        backgroundButton.setOnClickListener { showContainer(backgroundColorContainer) }
        scaleButton.setOnClickListener { showContainer(scaleContainer) }
        closeBackgroundColorButton.setOnClickListener { showContainer(toolbarContainer) }
        closeScaleButton.setOnClickListener { showContainer(toolbarContainer) }

        seekBar.setOnSeekBarChangeListener(OnSeekBarChangeListenerAdapter(
                onProgressChanged = { _, progress, _ ->
                    if (animationView.isAnimating) return@OnSeekBarChangeListenerAdapter
                    animationView.progress = progress / seekBar.max.toFloat()
                }
        ))

        L.setTraceEnabled(true)
        animationView.repeatCount = ValueAnimator.INFINITE
        animationView.useHardwareAcceleration()
        animationView.addAnimatorUpdateListener {
            if (seekBar.isPressed) return@addAnimatorUpdateListener
            seekBar.progress = ((it.animatedValue as Float) * seekBar.max).toInt()
        }
        animationView.addAnimatorListener(animatorListener)
        playButton.setOnClickListener {
            if (animationView.isAnimating) animationView.pauseAnimation() else animationView.resumeAnimation()
            playButton.isActivated = animationView.isAnimating
        }

        loopButton.isActivated = animationView.repeatCount == ValueAnimator.INFINITE
        loopButton.setOnClickListener {
            val repeatCount = if (animationView.repeatCount == ValueAnimator.INFINITE) 0 else ValueAnimator.INFINITE
            animationView.repeatCount = repeatCount
            loopButton.isActivated = repeatCount == ValueAnimator.INFINITE
        }

        scaleSeekBar.setOnSeekBarChangeListener(OnSeekBarChangeListenerAdapter(
                onProgressChanged = { _, progress, _ ->
                    val scale = (20f + progress * 3.8f) / 100f
                    Log.d("Gabe", "#\t$progress -> $scale")
                    animationView.scale = progress / 100f
                    scaleText.text = "%.0f%%".format(scale * 100f)
                }
        ))
        // This maps to a scale of 1
        scaleSeekBar.progress = 21

        arrayOf<BackgroundColorView>(
                backgroundButton1,
                backgroundButton2,
                backgroundButton3,
                backgroundButton4,
                backgroundButton5,
                backgroundButton6
        ).forEach { bb ->
            bb.setOnClickListener { animationContainer.setBackgroundColor(bb.getColor()) }
        }

        renderTimesGraph.axisRight.isEnabled = false
        renderTimesGraph.xAxis.isEnabled = false
        renderTimesGraph.legend.isEnabled = false
        renderTimesGraph.description = null
        renderTimesGraph.data = LineData(lineDataSet)
        renderTimesGraph.axisLeft.setDrawGridLines(false)
        renderTimesGraph.axisLeft.labelCount = 4
        val ll1 = LimitLine(16f, "60fps")
        ll1.lineColor = Color.RED
        ll1.lineWidth = 1.2f
        ll1.textColor = Color.BLACK
        ll1.textSize = 8f
        renderTimesGraph.axisLeft.addLimitLine(ll1)

        val ll2 = LimitLine(32f, "30fps")
        ll2.lineColor = Color.RED
        ll2.lineWidth = 1.2f
        ll2.textColor = Color.BLACK
        ll2.textSize = 8f
        renderTimesGraph.axisLeft.addLimitLine(ll2)
    }

    override fun onDestroyView() {
        animationView.removeAnimatorListener(animatorListener)
        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_player, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.isCheckable) item.isChecked = !item.isChecked
        when (item.itemId) {
            android.R.id.home -> requireFragmentManager().popBackStack()
            R.id.border -> animationView.setBackgroundResource(if (item.isChecked) R.drawable.outline else 0)
            R.id.hardware_acceleration -> animationView.useHardwareAcceleration(item.isChecked)
            R.id.merge_paths -> animationView.enableMergePathsForKitKatAndAbove(item.isChecked)
            R.id.performance_graph -> {
                beginDelayedTransition()
                renderTimesGraphContainer.isInvisible = !item.isChecked
                val lp = renderTimesGraphContainer.layoutParams as RelativeLayout.LayoutParams
                if (item.isChecked) {
                    lp.addRule(RelativeLayout.ABOVE, R.id.controlsContainer)
                    lp.addRule(RelativeLayout.ALIGN_TOP, 0)
                } else {
                    lp.addRule(RelativeLayout.ABOVE, 0)
                    lp.addRule(RelativeLayout.ALIGN_TOP, R.id.controlsContainer)
                }
                renderTimesGraphContainer.layoutParams = lp
            } else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onCompositionLoaded(composition: LottieComposition?) {
        if (composition == null) {
            Snackbar.make(coordinatorLayout, R.string.composition_load_error, Snackbar.LENGTH_LONG)
            return
        }
        animationView.setComposition(composition)
        animationView.setPerformanceTrackingEnabled(true)
        animationView.performanceTracker?.addFrameListener { ms ->
            lineDataSet.getEntryForIndex((animationView.progress * 100).toInt()).y = ms
            renderTimeGraphRange = Math.max(renderTimeGraphRange, ms * 1.2f)
            renderTimesGraph.setVisibleYRange(0f, renderTimeGraphRange, YAxis.AxisDependency.LEFT)
            renderTimesGraph.invalidate()
        }
    }

    private fun showContainer(container: View) {
        beginDelayedTransition()
        arrayOf(toolbarContainer, backgroundColorContainer, scaleContainer).forEach { it.isVisible = it == container }
    }

    private fun beginDelayedTransition() = TransitionManager.beginDelayedTransition(container, transition)

    companion object {
        const val EXTRA_ANIMATION_ARGS = "animation_args"

        fun forAsset(args: CompositionArgs): Fragment {
            return PlayerFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(EXTRA_ANIMATION_ARGS, args)
                }
            }
        }
    }
}