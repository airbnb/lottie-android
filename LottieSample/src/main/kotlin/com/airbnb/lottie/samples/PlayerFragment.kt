package com.airbnb.lottie.samples

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.Snackbar
import android.support.transition.AutoTransition
import android.support.transition.TransitionManager
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.widget.EditText
import androidx.view.children
import androidx.view.isVisible
import com.airbnb.lottie.L
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.samples.model.CompositionArgs
import com.airbnb.lottie.samples.views.BackgroundColorView
import com.airbnb.lottie.samples.views.BottomSheetItemView
import com.airbnb.lottie.samples.views.BottomSheetItemViewModel_
import com.airbnb.lottie.samples.views.ControlBarItemToggleView
import com.airbnb.lottie.utils.MiscUtils
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.bottom_sheet_key_paths.*
import kotlinx.android.synthetic.main.bottom_sheet_render_times.*
import kotlinx.android.synthetic.main.bottom_sheet_warnings.*
import kotlinx.android.synthetic.main.control_bar.*
import kotlinx.android.synthetic.main.control_bar_background_color.*
import kotlinx.android.synthetic.main.control_bar_player_controls.*
import kotlinx.android.synthetic.main.control_bar_scale.*
import kotlinx.android.synthetic.main.control_bar_speed.*
import kotlinx.android.synthetic.main.control_bar_trim.*
import kotlinx.android.synthetic.main.fragment_player.*
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.properties.ObservableProperty
import kotlin.reflect.KProperty

private class UiState(private val callback: () -> Unit) {

    private inner class BooleanProperty(initialValue: Boolean) : ObservableProperty<Boolean>(initialValue) {
        override fun afterChange(property: KProperty<*>, oldValue: Boolean, newValue: Boolean) {
            callback()
        }
    }

    var controls by BooleanProperty(true)
    var controlBar by BooleanProperty(true)
    var renderGraph by BooleanProperty(false)
    var border by BooleanProperty(false)
    var backgroundColor by BooleanProperty(false)
    var scale by BooleanProperty(false)
    var speed by BooleanProperty(false)
    var trim by BooleanProperty(false)
}

class PlayerFragment : Fragment() {

    private val transition = AutoTransition().apply { duration = 175 }
    private val uiState = UiState { updateUiFromState() }
    private val renderTimesBehavior by lazy {
        BottomSheetBehavior.from(renderTimesBottomSheet).apply {
            peekHeight = resources.getDimensionPixelSize(R.dimen.bottom_bar_peek_height)
        }
    }
    private val warningsBehavior by lazy {
        BottomSheetBehavior.from(warningsBottomSheet).apply {
            peekHeight = resources.getDimensionPixelSize(R.dimen.bottom_bar_peek_height)
        }
    }
    private val keyPathsBehavior by lazy {
        BottomSheetBehavior.from(keyPathsBottomSheet).apply {
            peekHeight = resources.getDimensionPixelSize(R.dimen.bottom_bar_peek_height)
        }
    }
    private val lineDataSet by lazy {
        val entries = ArrayList<Entry>(101)
        repeat(101) { i -> entries.add(Entry(i.toFloat(), 0f)) }
        LineDataSet(entries, "Render Times").apply {
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.3f
            setDrawCircles(false)
            lineWidth = 1.8f
            color = Color.BLACK
        }
    }
    private var composition: LottieComposition? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_player, container, false)

    private val animatorListener = AnimatorListenerAdapter(
            onStart = { playButton.isActivated = true },
            onEnd = {
                playButton.isActivated = false
                animationView.performanceTracker?.logRenderTimes()
                updateRenderTimesPerLayer()
                updateWarnings()
            },
            onCancel = {
                playButton.isActivated = false
                updateWarnings()
            },
            onRepeat = {
                animationView.performanceTracker?.logRenderTimes()
                updateRenderTimesPerLayer()
                updateWarnings()
            }
    )

    private val viewModel by lazy { ViewModelProviders.of(this).get(PlayerViewModel::class.java) }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        setHasOptionsMenu(true)

        L.setTraceEnabled(true)

        lottieVersionView.text = getString(R.string.lottie_version, com.airbnb.lottie.BuildConfig.VERSION_NAME)

        val args = arguments?.getParcelable<CompositionArgs>(EXTRA_ANIMATION_ARGS) ?: throw IllegalArgumentException("No composition args specified")
        args.animationData?.bgColorInt()?.let {
            backgroundButton1.setBackgroundColor(it)
            animationContainer.setBackgroundColor(it)
            invertColor(it)
        }

        viewModel.composition.observe(this, Observer {
            loadingView.isVisible = false
            onCompositionLoaded(it)
        })
        viewModel.error.observe(this, Observer {
            Snackbar.make(coordinatorLayout, R.string.composition_load_error, Snackbar.LENGTH_LONG)
        })
        viewModel.fetchAnimation(args)

        borderToggle.setOnClickListener { uiState.border++ }
        backgroundColorToggle.setOnClickListener { uiState.backgroundColor++ }
        scaleToggle.setOnClickListener { uiState.scale++ }
        speedToggle.setOnClickListener { uiState.speed++ }
        trimToggle.setOnClickListener { uiState.trim++ }
        renderGraphToggle.setOnClickListener { uiState.renderGraph++ }

        closeBackgroundColorButton.setOnClickListener { uiState.backgroundColor = false }
        closeScaleButton.setOnClickListener { uiState.scale = false }
        closeSpeedButton.setOnClickListener { uiState.speed = false }
        closeTrimButton.setOnClickListener { uiState.trim = false }

        hardwareAccelerationToggle.setOnClickListener {
            animationView.useHardwareAcceleration(!animationView.useHardwareAcceleration)
            updateUiFromState()
        }

        mergePathsToggle.setOnClickListener {
            animationView.enableMergePathsForKitKatAndAbove(!animationView.isMergePathsEnabledForKitKatAndAbove)
            updateUiFromState()
        }

        seekBar.setOnSeekBarChangeListener(OnSeekBarChangeListenerAdapter(
                onProgressChanged = { _, progress, _ ->
                    if (seekBar.isPressed && progress in 1..4) {
                        seekBar.progress = 0
                        return@OnSeekBarChangeListenerAdapter
                    }
                    if (animationView.isAnimating) return@OnSeekBarChangeListenerAdapter
                    animationView.progress = progress / seekBar.max.toFloat()
                }
        ))

        animationView.repeatCount = ValueAnimator.INFINITE

        animationView.addAnimatorUpdateListener {
            currentFrameView.text = updateFramesAndDurationLabel(animationView)

            if (seekBar.isPressed) return@addAnimatorUpdateListener
            seekBar.progress = ((it.animatedValue as Float) * seekBar.max).roundToInt()
        }
        animationView.addAnimatorListener(animatorListener)
        playButton.setOnClickListener {
            if (animationView.isAnimating) animationView.pauseAnimation() else animationView.resumeAnimation()
            updateUiFromState()
        }

        loopButton.setOnClickListener {
            val repeatCount = if (animationView.repeatCount == ValueAnimator.INFINITE) 0 else ValueAnimator.INFINITE
            animationView.repeatCount = repeatCount
            updateUiFromState()
        }

        scaleSeekBar.setOnSeekBarChangeListener(OnSeekBarChangeListenerAdapter(
                onProgressChanged = { _, progress, _ ->
                    val minScale = minScale()
                    val maxScale = maxScale()
                    val scale = minScale + progress / 100f * (maxScale - minScale)
                    animationView.scale = scale
                    scaleText.text = "%.0f%%".format(scale * 100)
                }
        ))

        minFrame.setOnClickListener {
            val minFrameView = EditText(context)
            minFrameView.setText(animationView.minFrame.toInt().toString())
            AlertDialog.Builder(context)
                    .setTitle(R.string.min_frame_dialog)
                    .setView(minFrameView)
                    .setPositiveButton("Load") { _, _ ->
                        var frame = minFrameView.text.toString().toFloatOrNull() ?: 0f
                        frame = MiscUtils.clamp(frame, composition?.startFrame ?: frame, animationView.maxFrame)

                        animationView.setMinFrame(frame.toInt())
                        updateUiFromState()
                    }
                    .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                    .show()
        }

        maxFrame.setOnClickListener {
            val maxFrameView = EditText(context)
            maxFrameView.setText(animationView.maxFrame.toInt().toString())
            AlertDialog.Builder(context)
                    .setTitle(R.string.max_frame_dialog)
                    .setView(maxFrameView)
                    .setPositiveButton("Load") { _, _ ->
                        var frame = maxFrameView.text.toString().toFloatOrNull() ?: 0f
                        frame = MiscUtils.clamp(frame, animationView.minFrame, composition?.endFrame ?: frame)
                        animationView.setMaxFrame(frame.toInt())
                        updateUiFromState()
                    }
                    .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                    .show()
        }

        arrayOf<BackgroundColorView>(
                backgroundButton1,
                backgroundButton2,
                backgroundButton3,
                backgroundButton4,
                backgroundButton5,
                backgroundButton6
        ).forEach { bb ->
            bb.setOnClickListener {
                animationContainer.setBackgroundColor(bb.getColor())
                invertColor(bb.getColor())
            }
        }

        renderTimesGraph.apply {
            setTouchEnabled(false)
            axisRight.isEnabled = false
            xAxis.isEnabled = false
            legend.isEnabled = false
            description = null
            data = LineData(lineDataSet)
            axisLeft.setDrawGridLines(false)
            axisLeft.labelCount = 4
            val ll1 = LimitLine(16f, "60fps")
            ll1.lineColor = Color.RED
            ll1.lineWidth = 1.2f
            ll1.textColor = Color.BLACK
            ll1.textSize = 8f
            axisLeft.addLimitLine(ll1)

            val ll2 = LimitLine(32f, "30fps")
            ll2.lineColor = Color.RED
            ll2.lineWidth = 1.2f
            ll2.textColor = Color.BLACK
            ll2.textSize = 8f
            axisLeft.addLimitLine(ll2)
        }

        speedButtonsContainer.children.forEach {
            if (it is ControlBarItemToggleView) {
                it.setOnClickListener {
                    animationView.speed = (it as ControlBarItemToggleView)
                            .getText()
                            .replace("x", "")
                            .toFloat()
                    updateUiFromState()
                }
            }
        }

        renderTimesPerLayerButton.setOnClickListener {
            updateRenderTimesPerLayer()
            renderTimesBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        closeRenderTimesBottomSheetButton.setOnClickListener {
            renderTimesBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
        renderTimesBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        warningsButton.setOnClickListener {
            updateWarnings()
            if (composition?.warnings?.isEmpty() != true) {
                warningsBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }

        closeWarningsBottomSheetButton.setOnClickListener {
            warningsBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
        warningsBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        keyPathsToggle.setOnClickListener {
            keyPathsBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        closeKeyPathsBottomSheetButton.setOnClickListener {
            keyPathsBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
        keyPathsBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        keyPathsRecyclerView.buildModelsWith { controller ->
            composition?.let {
                animationView.resolveKeyPath(KeyPath("**")).forEachIndexed { index, keyPath ->
                    BottomSheetItemViewModel_()
                            .id(index)
                            .text(keyPath.keysToString())
                            .addTo(controller)

                }
            }
        }

        updateUiFromState()
    }

    private fun invertColor(color: Int) {
        val isDarkBg = color.isDark()
        animationView.isActivated = isDarkBg
        toolbar.isActivated = isDarkBg
    }

    private fun Int.isDark(): Boolean {
        val y = (299 * Color.red(this) + 587 * Color.green(this) + 114 * Color.blue(this)) / 1000
        return y < 128
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
            android.R.id.home -> requireActivity().finish()
            R.id.info -> Unit
            R.id.visibility -> {
                uiState.controls = !item.isChecked
                uiState.controlBar = !item.isChecked
                uiState.renderGraph = false
                uiState.border = false
                uiState.backgroundColor = false
                uiState.scale = false
                uiState.speed = false
                uiState.trim = false
                updateUiFromState()
                val menuIcon = if (item.isChecked) R.drawable.ic_eye_teal else R.drawable.ic_eye_selector
                item.icon = ContextCompat.getDrawable(requireContext(), menuIcon)
            }
        }
        return true
    }

    private fun onCompositionLoaded(compositionData: CompositionData?) {
        if (this.composition != null) return
        if (compositionData?.composition == null) {
            Snackbar.make(coordinatorLayout, R.string.composition_load_error, Snackbar.LENGTH_LONG)
            return
        }

        val composition = compositionData.composition!!
        this.composition = composition

        animationView.setImageAssetDelegate {
            compositionData.images[it.fileName]
        }
        animationView.setComposition(composition)
        animationView.setMinAndMaxFrame(composition.startFrame.toInt(), composition.endFrame.toInt())
        animationView.setPerformanceTrackingEnabled(true)
        var renderTimeGraphRange = 4f
        animationView.performanceTracker?.addFrameListener { ms ->
            if (lifecycle.currentState != Lifecycle.State.RESUMED) return@addFrameListener
            lineDataSet.getEntryForIndex((animationView.progress * 100).toInt()).y = ms
            renderTimeGraphRange = Math.max(renderTimeGraphRange, ms * 1.2f)
            renderTimesGraph.setVisibleYRange(0f, renderTimeGraphRange, YAxis.AxisDependency.LEFT)
            renderTimesGraph.invalidate()
        }

        // Force warning to update
        warningsContainer.removeAllViews()
        updateWarnings()

        // Scale up to fill the screen
        scaleSeekBar.progress = 100

        keyPathsRecyclerView.requestModelBuild()
    }

    private fun updateUiFromState() {
        beginDelayedTransition()

        controlsContainer.isVisible = uiState.controls
        controlBar.isVisible = uiState.controlBar

        renderGraphToggle.isActivated = uiState.renderGraph
        renderTimesGraphContainer.isVisible = uiState.renderGraph
        renderTimesPerLayerButton.isVisible = uiState.renderGraph
        lottieVersionView.isVisible = !uiState.renderGraph

        borderToggle.isActivated = uiState.border
        borderToggle.setImageResource(
                if (uiState.border) R.drawable.ic_border_on
                else R.drawable.ic_border_off
        )
        animationView.setBackgroundResource(if (borderToggle.isActivated) R.drawable.outline else 0)

        backgroundColorToggle.isActivated = uiState.backgroundColor
        backgroundColorContainer.isVisible = uiState.backgroundColor

        scaleToggle.isActivated = uiState.scale
        scaleContainer.isVisible = uiState.scale

        trimToggle.isActivated = uiState.trim
        trimContainer.isVisible = uiState.trim
        // I think this is a lint bug. It complains about int being <ErrorType>
        //noinspection StringFormatMatches
        minFrame.setText(resources.getString(R.string.min_frame, animationView.minFrame.toInt()))
        //noinspection StringFormatMatches
        maxFrame.setText(resources.getString(R.string.max_frame, animationView.maxFrame.toInt()))

        hardwareAccelerationToggle.isActivated = animationView.useHardwareAcceleration
        mergePathsToggle.isActivated = animationView.isMergePathsEnabledForKitKatAndAbove

        speedToggle.isActivated = uiState.speed
        speedContainer.isVisible = uiState.speed
        speedButtonsContainer.children.forEach {
            if (it is ControlBarItemToggleView) {
                it.isActivated = it.getText().replace("x", "").toFloat() == animationView.speed
            }
        }

        loopButton.isActivated = animationView.repeatCount == ValueAnimator.INFINITE
        playButton.isActivated = animationView.isAnimating
    }

    private fun updateRenderTimesPerLayer() {
        renderTimesContainer.removeAllViews()
        animationView.performanceTracker?.sortedRenderTimes?.forEach {
            val view = BottomSheetItemView(requireContext()).apply {
                set(
                        it.first!!.replace("__container", "Total"),
                        "%.2f ms".format(it.second!!)
                )
            }
            renderTimesContainer.addView(view)
        }
    }

    private fun updateWarnings() {
        val warnings = composition?.warnings ?: emptySet<String>()
        if (!warnings.isEmpty() && warnings.size == warningsContainer.childCount) return

        warningsContainer.removeAllViews()
        warnings.forEach {
            val view = BottomSheetItemView(requireContext()).apply {
                set(it)
            }
            warningsContainer.addView(view)
        }

        val size = warnings.size
        warningsButton.setText(resources.getQuantityString(R.plurals.warnings, size, size))
        warningsButton.setImageResource(
                if (warnings.isEmpty()) R.drawable.ic_sentiment_satisfied
                else R.drawable.ic_sentiment_dissatisfied
        )
    }

    private fun minScale() = 0.15f

    private fun maxScale(): Float {
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val screenHeight = resources.displayMetrics.heightPixels.toFloat()
        return min(
                screenWidth / (composition?.bounds?.width()?.toFloat() ?: screenWidth),
                screenHeight / (composition?.bounds?.height()?.toFloat() ?: screenHeight)
        )
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

    private fun updateFramesAndDurationLabel(animation: LottieAnimationView): String {
        val currentFrame = animation.frame.toString()
        val totalFrames = ("%.0f").format(animation.maxFrame)

        val animationSpeed: Float = Math.abs(animation.speed)

		val totalTime = ((animation.duration / animationSpeed) / 1000.0)
        val totalTimeFormatted = ("%.1f").format(totalTime)

        val progress = (totalTime / 100.0) * (Math.round(animation.progress * 100.0))
        val progressFormatted = ("%.1f").format(progress)

        return "$currentFrame/$totalFrames\n$progressFormatted/$totalTimeFormatted"
    }
}