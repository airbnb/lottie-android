package com.airbnb.lottie.samples

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyRecyclerView
import com.airbnb.lottie.*
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.samples.model.CompositionArgs
import com.airbnb.lottie.samples.views.BackgroundColorView
import com.airbnb.lottie.samples.views.BottomSheetItemView
import com.airbnb.lottie.samples.views.BottomSheetItemViewModel_
import com.airbnb.lottie.samples.views.ControlBarItemToggleView
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
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

class PlayerFragment : BaseMvRxFragment() {

    private val transition = AutoTransition().apply { duration = 175 }
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

    private val animatorListener = AnimatorListenerAdapter(
            onStart = { playButton.isActivated = true },
            onEnd = {
                playButton.isActivated = false
                animationView.performanceTracker?.logRenderTimes()
                updateRenderTimesPerLayer()
            },
            onCancel = {
                playButton.isActivated = false
            },
            onRepeat = {
                animationView.performanceTracker?.logRenderTimes()
                updateRenderTimesPerLayer()
            }
    )

    private val viewModel: PlayerViewModel by fragmentViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_player, container, false)

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        setHasOptionsMenu(true)

        lottieVersionView.text = getString(R.string.lottie_version, BuildConfig.VERSION_NAME)

        animationView.setFontAssetDelegate(object : FontAssetDelegate() {
            override fun fetchFont(fontFamily: String?): Typeface {
                return Typeface.DEFAULT
            }
        })

        val args = arguments?.getParcelable<CompositionArgs>(EXTRA_ANIMATION_ARGS)
                ?: throw IllegalArgumentException("No composition args specified")
        args.animationData?.bgColorInt?.let {
            backgroundButton1.setBackgroundColor(it)
            animationContainer.setBackgroundColor(it)
            invertColor(it)
        }

        args.animationDataV2?.bgColorInt?.let {
            backgroundButton1.setBackgroundColor(it)
            animationContainer.setBackgroundColor(it)
            invertColor(it)
        }

        minFrameView.setOnClickListener { showMinFrameDialog() }
        maxFrameView.setOnClickListener { showMaxFrameDialog() }
        viewModel.selectSubscribe(PlayerState::minFrame, PlayerState::maxFrame) { minFrame, maxFrame ->
            animationView.setMinAndMaxFrame(minFrame, maxFrame)
            // I think this is a lint bug. It complains about int being <ErrorType>
            //noinspection StringFormatMatches
            minFrameView.setText(resources.getString(R.string.min_frame, animationView.minFrame.toInt()))
            //noinspection StringFormatMatches
            maxFrameView.setText(resources.getString(R.string.max_frame, animationView.maxFrame.toInt()))
        }

        viewModel.fetchAnimation(args)
        viewModel.asyncSubscribe(PlayerState::composition, onFail = {
            Snackbar.make(coordinatorLayout, R.string.composition_load_error, Snackbar.LENGTH_LONG).show()
            Log.w(L.TAG, "Error loading composition.", it)
        }) {
            loadingView.isVisible = false
            onCompositionLoaded(it)
        }

        borderToggle.setOnClickListener { viewModel.toggleBorderVisible() }
        viewModel.selectSubscribe(PlayerState::borderVisible) {
            borderToggle.isActivated = it
            borderToggle.setImageResource(
                    if (it) R.drawable.ic_border_on
                    else R.drawable.ic_border_off
            )
            animationView.setBackgroundResource(if (it) R.drawable.outline else 0)
        }

        hardwareAccelerationToggle.setOnClickListener {
            val renderMode = if (animationView.layerType == View.LAYER_TYPE_HARDWARE) {
                RenderMode.SOFTWARE
            } else {
                RenderMode.HARDWARE
            }
            animationView.setRenderMode(renderMode)
            hardwareAccelerationToggle.isActivated = animationView.layerType == View.LAYER_TYPE_HARDWARE
        }

        enableApplyingOpacityToLayers.setOnClickListener {
            val isApplyingOpacityToLayersEnabled = !enableApplyingOpacityToLayers.isActivated
            animationView.setApplyingOpacityToLayersEnabled(isApplyingOpacityToLayersEnabled)
            enableApplyingOpacityToLayers.isActivated = isApplyingOpacityToLayersEnabled
        }

        viewModel.selectSubscribe(PlayerState::controlsVisible) { controlsContainer.animateVisible(it) }

        viewModel.selectSubscribe(PlayerState::controlBarVisible) { controlBar.animateVisible(it) }

        renderGraphToggle.setOnClickListener { viewModel.toggleRenderGraphVisible() }
        viewModel.selectSubscribe(PlayerState::renderGraphVisible) {
            renderGraphToggle.isActivated = it
            renderTimesGraphContainer.animateVisible(it)
            renderTimesPerLayerButton.animateVisible(it)
            lottieVersionView.animateVisible(!it)
        }

        backgroundColorToggle.setOnClickListener { viewModel.toggleBackgroundColorVisible() }
        closeBackgroundColorButton.setOnClickListener { viewModel.setBackgroundColorVisible(false) }
        viewModel.selectSubscribe(PlayerState::backgroundColorVisible) {
            backgroundColorToggle.isActivated = it
            backgroundColorContainer.animateVisible(it)
        }

        scaleToggle.setOnClickListener { viewModel.toggleScaleVisible() }
        closeScaleButton.setOnClickListener { viewModel.setScaleVisible(false) }
        viewModel.selectSubscribe(PlayerState::scaleVisible) {
            scaleToggle.isActivated = it
            scaleContainer.animateVisible(it)
        }

        trimToggle.setOnClickListener { viewModel.toggleTrimVisible() }
        closeTrimButton.setOnClickListener { viewModel.setTrimVisible(false) }
        viewModel.selectSubscribe(PlayerState::trimVisible) {
            trimToggle.isActivated = it
            trimContainer.animateVisible(it)
        }

        mergePathsToggle.setOnClickListener { viewModel.toggleMergePaths() }
        viewModel.selectSubscribe(PlayerState::useMergePaths) {
            animationView.enableMergePathsForKitKatAndAbove(it)
            mergePathsToggle.isActivated = it
        }

        speedToggle.setOnClickListener { viewModel.toggleSpeedVisible() }
        closeSpeedButton.setOnClickListener { viewModel.setSpeedVisible(false) }
        viewModel.selectSubscribe(PlayerState::speedVisible) {
            speedToggle.isActivated = it
            speedContainer.isVisible = it
        }
        viewModel.selectSubscribe(PlayerState::speed) {
            animationView.speed = it
            speedButtonsContainer
                    .children
                    .filterIsInstance<ControlBarItemToggleView>()
                    .forEach { toggleView ->
                        toggleView.isActivated = toggleView.getText().replace("x", "").toFloat() == animationView.speed
                    }
        }
        speedButtonsContainer
                .children
                .filterIsInstance(ControlBarItemToggleView::class.java)
                .forEach { child ->
                    child.setOnClickListener {
                        val speed = (it as ControlBarItemToggleView)
                                .getText()
                                .replace("x", "")
                                .toFloat()
                        viewModel.setSpeed(speed)
                    }
                }


        loopButton.setOnClickListener { viewModel.toggleLoop() }
        viewModel.selectSubscribe(PlayerState::repeatCount) {
            animationView.repeatCount = it
            loopButton.isActivated = animationView.repeatCount == ValueAnimator.INFINITE
        }

        playButton.isActivated = animationView.isAnimating

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

        animationView.addAnimatorUpdateListener {
            currentFrameView.text = updateFramesAndDurationLabel(animationView)

            if (seekBar.isPressed) return@addAnimatorUpdateListener
            seekBar.progress = ((it.animatedValue as Float) * seekBar.max).roundToInt()
        }
        animationView.addAnimatorListener(animatorListener)
        playButton.setOnClickListener {
            if (animationView.isAnimating) animationView.pauseAnimation() else animationView.resumeAnimation()
            playButton.isActivated = animationView.isAnimating
            postInvalidate()
        }

        animationView.setOnClickListener {
            // Click the animation view to re-render it for debugging purposes.
            animationView.invalidate()
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

        renderTimesPerLayerButton.setOnClickListener {
            updateRenderTimesPerLayer()
            renderTimesBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        closeRenderTimesBottomSheetButton.setOnClickListener {
            renderTimesBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
        renderTimesBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        warningsButton.setOnClickListener {
            withState(viewModel) { state ->
                if (state.composition()?.warnings?.isEmpty() != true) {
                    warningsBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

                }
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
    }

    private fun showMinFrameDialog() {
        val minFrameView = EditText(context)
        minFrameView.setText(animationView.minFrame.toInt().toString())
        AlertDialog.Builder(context)
                .setTitle(R.string.min_frame_dialog)
                .setView(minFrameView)
                .setPositiveButton("Load") { _, _ ->
                    viewModel.setMinFrame(minFrameView.text.toString().toIntOrNull() ?: 0)
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .show()
    }

    private fun showMaxFrameDialog() {
        val maxFrameView = EditText(context)
        maxFrameView.setText(animationView.maxFrame.toInt().toString())
        AlertDialog.Builder(context)
                .setTitle(R.string.max_frame_dialog)
                .setView(maxFrameView)
                .setPositiveButton("Load") { _, _ ->
                    viewModel.setMaxFrame(maxFrameView.text.toString().toIntOrNull() ?: 0)
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .show()
    }

    private fun View.animateVisible(visible: Boolean) {
        beginDelayedTransition()
        isVisible = visible
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
                viewModel.setDistractionFree(item.isChecked)
                val menuIcon = if (item.isChecked) R.drawable.ic_eye_teal else R.drawable.ic_eye_selector
                item.icon = ContextCompat.getDrawable(requireContext(), menuIcon)
            }
        }
        return true
    }

    private fun onCompositionLoaded(composition: LottieComposition?) {
        composition ?: return

        animationView.setComposition(composition)
        hardwareAccelerationToggle.isActivated = animationView.layerType == View.LAYER_TYPE_HARDWARE
        animationView.setPerformanceTrackingEnabled(true)
        var renderTimeGraphRange = 4f
        animationView.performanceTracker?.addFrameListener { ms ->
            if (lifecycle.currentState != Lifecycle.State.RESUMED) return@addFrameListener
            lineDataSet.getEntryForIndex((animationView.progress * 100).toInt()).y = ms
            renderTimeGraphRange = Math.max(renderTimeGraphRange, ms * 1.2f)
            renderTimesGraph.setVisibleYRange(0f, renderTimeGraphRange, YAxis.AxisDependency.LEFT)
            renderTimesGraph.invalidate()
        }

        // Scale up to fill the screen
        scaleSeekBar.progress = 100

        keyPathsRecyclerView.buildModelsWith(object : EpoxyRecyclerView.ModelBuilderCallback {
            override fun buildModels(controller: EpoxyController) {
                animationView.resolveKeyPath(KeyPath("**")).forEachIndexed { index, keyPath ->
                    BottomSheetItemViewModel_()
                            .id(index)
                            .text(keyPath.keysToString())
                            .addTo(controller)
                }
            }
        })

        updateWarnings()
    }

    override fun invalidate() {
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

    private fun updateWarnings() = withState(viewModel) { state ->
        // Force warning to update
        warningsContainer.removeAllViews()

        val warnings = state.composition()?.warnings ?: emptySet<String>()
        if (!warnings.isEmpty() && warnings.size == warningsContainer.childCount) return@withState

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

    private fun minScale() = 0.05f

    private fun maxScale(): Float = withState(viewModel) { state ->
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val screenHeight = resources.displayMetrics.heightPixels.toFloat()
        val bounds = state.composition()?.bounds
        return@withState min(
                screenWidth / (bounds?.width()?.toFloat() ?: screenWidth),
                screenHeight / (bounds?.height()?.toFloat() ?: screenHeight)
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
