package com.airbnb.lottie.samples

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyRecyclerView
import com.airbnb.lottie.*
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.samples.databinding.PlayerFragmentBinding
import com.airbnb.lottie.samples.model.CompositionArgs
import com.airbnb.lottie.samples.utils.viewBinding
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
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

class PlayerFragment : BaseMvRxFragment(R.layout.player_fragment) {
    private val binding: PlayerFragmentBinding by viewBinding()
    private val viewModel: PlayerViewModel by fragmentViewModel()

    private val transition = AutoTransition().apply { duration = 175 }
    private val renderTimesBehavior by lazy {
        BottomSheetBehavior.from(binding.bottomSheetRenderTimes.root).apply {
            peekHeight = resources.getDimensionPixelSize(R.dimen.bottom_bar_peek_height)
        }
    }
    private val warningsBehavior by lazy {
        BottomSheetBehavior.from(binding.bottomSheetWarnings.root).apply {
            peekHeight = resources.getDimensionPixelSize(R.dimen.bottom_bar_peek_height)
        }
    }
    private val keyPathsBehavior by lazy {
        BottomSheetBehavior.from(binding.bottomSheetKeyPaths.root).apply {
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
            onStart = { binding.controlBarPlayerControls.playButton.isActivated = true },
            onEnd = {
                binding.controlBarPlayerControls.playButton.isActivated = false
                binding.animationView.performanceTracker?.logRenderTimes()
                updateRenderTimesPerLayer()
            },
            onCancel = {
                binding.controlBarPlayerControls.playButton.isActivated = false
            },
            onRepeat = {
                binding.animationView.performanceTracker?.logRenderTimes()
                updateRenderTimesPerLayer()
            }
    )

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        setHasOptionsMenu(true)

        binding.controlBarPlayerControls.lottieVersionView.text = getString(R.string.lottie_version, BuildConfig.VERSION_NAME)

        binding.animationView.setFontAssetDelegate(object : FontAssetDelegate() {
            override fun fetchFont(fontFamily: String?): Typeface {
                return Typeface.DEFAULT
            }
        })

        val args = arguments?.getParcelable<CompositionArgs>(EXTRA_ANIMATION_ARGS)
                ?: throw IllegalArgumentException("No composition args specified")
        args.animationData?.bgColorInt?.let {
            binding.controlBarBackgroundColor.backgroundButton1.setBackgroundColor(it)
            binding.animationContainer.setBackgroundColor(it)
            invertColor(it)
        }

        args.animationDataV2?.bgColorInt?.let {
            binding.controlBarBackgroundColor.backgroundButton1.setBackgroundColor(it)
            binding.animationContainer.setBackgroundColor(it)
            invertColor(it)
        }

        binding.controlBarTrim.minFrameView.setOnClickListener { showMinFrameDialog() }
        binding.controlBarTrim.maxFrameView.setOnClickListener { showMaxFrameDialog() }
        viewModel.selectSubscribe(PlayerState::minFrame, PlayerState::maxFrame) { minFrame, maxFrame ->
            binding.animationView.setMinAndMaxFrame(minFrame, maxFrame)
            // I think this is a lint bug. It complains about int being <ErrorType>
            //noinspection StringFormatMatches
            binding.controlBarTrim.minFrameView.setText(resources.getString(R.string.min_frame, binding.animationView.minFrame.toInt()))
            //noinspection StringFormatMatches
            binding.controlBarTrim.maxFrameView.setText(resources.getString(R.string.max_frame, binding.animationView.maxFrame.toInt()))
        }

        viewModel.fetchAnimation(args)
        viewModel.asyncSubscribe(PlayerState::composition, onFail = {
            Snackbar.make(binding.coordinatorLayout, R.string.composition_load_error, Snackbar.LENGTH_LONG).show()
            Log.w(L.TAG, "Error loading composition.", it)
        }) {
            binding.loadingView.isVisible = false
            onCompositionLoaded(it)
        }

        binding.controlBar.borderToggle.setOnClickListener { viewModel.toggleBorderVisible() }
        viewModel.selectSubscribe(PlayerState::borderVisible) {
            binding.controlBar.borderToggle.isActivated = it
            binding.controlBar.borderToggle.setImageResource(
                    if (it) R.drawable.ic_border_on
                    else R.drawable.ic_border_off
            )
            binding.animationView.setBackgroundResource(if (it) R.drawable.outline else 0)
        }

        binding.controlBar.hardwareAccelerationToggle.setOnClickListener {
            val renderMode = if (binding.animationView.layerType == View.LAYER_TYPE_HARDWARE) {
                RenderMode.SOFTWARE
            } else {
                RenderMode.HARDWARE
            }
            binding.animationView.setRenderMode(renderMode)
            binding.controlBar.hardwareAccelerationToggle.isActivated = binding.animationView.layerType == View.LAYER_TYPE_HARDWARE
        }

        binding.controlBar.enableApplyingOpacityToLayers.setOnClickListener {
            val isApplyingOpacityToLayersEnabled = !binding.controlBar.enableApplyingOpacityToLayers.isActivated
            binding.animationView.setApplyingOpacityToLayersEnabled(isApplyingOpacityToLayersEnabled)
            binding.controlBar.enableApplyingOpacityToLayers.isActivated = isApplyingOpacityToLayersEnabled
        }

        viewModel.selectSubscribe(PlayerState::controlsVisible) { binding.controlBarPlayerControls.controlsContainer.animateVisible(it) }

        viewModel.selectSubscribe(PlayerState::controlBarVisible) { binding.controlBar.root.animateVisible(it) }

        binding.controlBar.renderGraphToggle.setOnClickListener { viewModel.toggleRenderGraphVisible() }
        viewModel.selectSubscribe(PlayerState::renderGraphVisible) {
            binding.controlBar.renderGraphToggle.isActivated = it
            binding.controlBarPlayerControls.renderTimesGraphContainer.animateVisible(it)
            binding.controlBarPlayerControls.renderTimesPerLayerButton.animateVisible(it)
            binding.controlBarPlayerControls.lottieVersionView.animateVisible(!it)
        }

        binding.controlBar.masksAndMattesToggle.setOnClickListener { viewModel.toggleOutlineMasksAndMattes() }
        viewModel.selectSubscribe(PlayerState::outlineMasksAndMattes) {
            binding.controlBar.masksAndMattesToggle.isActivated = it
            binding.animationView.setOutlineMasksAndMattes(it)
        }

        binding.controlBar.backgroundColorToggle.setOnClickListener { viewModel.toggleBackgroundColorVisible() }
        binding.controlBarBackgroundColor.closeBackgroundColorButton.setOnClickListener { viewModel.setBackgroundColorVisible(false) }
        viewModel.selectSubscribe(PlayerState::backgroundColorVisible) {
            binding.controlBar.backgroundColorToggle.isActivated = it
            binding.controlBarBackgroundColor.backgroundColorContainer.animateVisible(it)
        }

        binding.controlBar.scaleToggle.setOnClickListener { viewModel.toggleScaleVisible() }
        binding.controlBarScale.closeScaleButton.setOnClickListener { viewModel.setScaleVisible(false) }
        viewModel.selectSubscribe(PlayerState::scaleVisible) {
            binding.controlBar.scaleToggle.isActivated = it
            binding.controlBarScale.scaleContainer.animateVisible(it)
        }

        binding.controlBar.trimToggle.setOnClickListener { viewModel.toggleTrimVisible() }
        binding.controlBarTrim.closeTrimButton.setOnClickListener { viewModel.setTrimVisible(false) }
        viewModel.selectSubscribe(PlayerState::trimVisible) {
            binding.controlBar.trimToggle.isActivated = it
            binding.controlBarTrim.trimContainer.animateVisible(it)
        }

        binding.controlBar.mergePathsToggle.setOnClickListener { viewModel.toggleMergePaths() }
        viewModel.selectSubscribe(PlayerState::useMergePaths) {
            binding.animationView.enableMergePathsForKitKatAndAbove(it)
            binding.controlBar.mergePathsToggle.isActivated = it
        }

        binding.controlBar.speedToggle.setOnClickListener { viewModel.toggleSpeedVisible() }
        binding.controlBarSpeed.closeSpeedButton.setOnClickListener { viewModel.setSpeedVisible(false) }
        viewModel.selectSubscribe(PlayerState::speedVisible) {
            binding.controlBar.speedToggle.isActivated = it
            binding.controlBarSpeed.speedContainer.isVisible = it
        }
        viewModel.selectSubscribe(PlayerState::speed) {
            binding.animationView.speed = it
            binding.controlBarSpeed.speedButtonsContainer
                    .children
                    .filterIsInstance<ControlBarItemToggleView>()
                    .forEach { toggleView ->
                        toggleView.isActivated = toggleView.getText().replace("x", "").toFloat() == binding.animationView.speed
                    }
        }
        binding.controlBarSpeed.speedButtonsContainer
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


        binding.controlBarPlayerControls.loopButton.setOnClickListener { viewModel.toggleLoop() }
        viewModel.selectSubscribe(PlayerState::repeatCount) {
            binding.animationView.repeatCount = it
            binding.controlBarPlayerControls.loopButton.isActivated = binding.animationView.repeatCount == ValueAnimator.INFINITE
        }

        binding.controlBarPlayerControls.playButton.isActivated = binding.animationView.isAnimating

        binding.controlBarPlayerControls.seekBar.setOnSeekBarChangeListener(OnSeekBarChangeListenerAdapter(
                onProgressChanged = { _, progress, _ ->
                    if (binding.controlBarPlayerControls.seekBar.isPressed && progress in 1..4) {
                        binding.controlBarPlayerControls.seekBar.progress = 0
                        return@OnSeekBarChangeListenerAdapter
                    }
                    if (binding.animationView.isAnimating) return@OnSeekBarChangeListenerAdapter
                    binding.animationView.progress = progress / binding.controlBarPlayerControls.seekBar.max.toFloat()
                }
        ))

        binding.animationView.addAnimatorUpdateListener {
            binding.controlBarPlayerControls.currentFrameView.text = updateFramesAndDurationLabel(binding.animationView)

            if (binding.controlBarPlayerControls.seekBar.isPressed) return@addAnimatorUpdateListener
            binding.controlBarPlayerControls.seekBar.progress = ((it.animatedValue as Float) * binding.controlBarPlayerControls.seekBar.max).roundToInt()
        }
        binding.animationView.addAnimatorListener(animatorListener)
        binding.controlBarPlayerControls.playButton.setOnClickListener {
            if (binding.animationView.isAnimating) binding.animationView.pauseAnimation() else binding.animationView.resumeAnimation()
            binding.controlBarPlayerControls.playButton.isActivated = binding.animationView.isAnimating
            postInvalidate()
        }

        binding.animationView.setOnClickListener {
            // Click the animation view to re-render it for debugging purposes.
            binding.animationView.invalidate()
        }

        binding.controlBarScale.scaleSeekBar.setOnSeekBarChangeListener(OnSeekBarChangeListenerAdapter(
                onProgressChanged = { _, progress, _ ->
                    val minScale = minScale()
                    val maxScale = maxScale()
                    val scale = minScale + progress / 100f * (maxScale - minScale)
                    binding.animationView.scale = scale
                    binding.controlBarScale.scaleText.text = "%.0f%%".format(scale * 100)
                }
        ))

        arrayOf(
                binding.controlBarBackgroundColor.backgroundButton1,
                binding.controlBarBackgroundColor.backgroundButton2,
                binding.controlBarBackgroundColor.backgroundButton3,
                binding.controlBarBackgroundColor.backgroundButton4,
                binding.controlBarBackgroundColor.backgroundButton5,
                binding.controlBarBackgroundColor.backgroundButton6
        ).forEach { bb ->
            bb.setOnClickListener {
                binding.animationContainer.setBackgroundColor(bb.getColor())
                invertColor(bb.getColor())
            }
        }

        binding.controlBarPlayerControls.renderTimesGraph.apply {
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

        binding.controlBarPlayerControls.renderTimesPerLayerButton.setOnClickListener {
            updateRenderTimesPerLayer()
            renderTimesBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        binding.bottomSheetRenderTimes.closeRenderTimesBottomSheetButton.setOnClickListener {
            renderTimesBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
        renderTimesBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        binding.controlBar.warningsButton.setOnClickListener {
            withState(viewModel) { state ->
                if (state.composition()?.warnings?.isEmpty() != true) {
                    warningsBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

                }
            }
        }

        binding.bottomSheetWarnings.closeWarningsBottomSheetButton.setOnClickListener {
            warningsBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
        warningsBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        binding.controlBar.keyPathsToggle.setOnClickListener {
            keyPathsBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        binding.bottomSheetKeyPaths.closeKeyPathsBottomSheetButton.setOnClickListener {
            keyPathsBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
        keyPathsBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun showMinFrameDialog() {
        val minFrameView = EditText(context)
        minFrameView.setText(binding.animationView.minFrame.toInt().toString())
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
        maxFrameView.setText(binding.animationView.maxFrame.toInt().toString())
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
        binding.animationView.isActivated = isDarkBg
        binding.toolbar.isActivated = isDarkBg
    }

    private fun Int.isDark(): Boolean {
        val y = (299 * Color.red(this) + 587 * Color.green(this) + 114 * Color.blue(this)) / 1000
        return y < 128
    }

    override fun onDestroyView() {
        binding.animationView.removeAnimatorListener(animatorListener)
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

        binding.animationView.setComposition(composition)
        binding.controlBar.hardwareAccelerationToggle.isActivated = binding.animationView.layerType == View.LAYER_TYPE_HARDWARE
        binding.animationView.setPerformanceTrackingEnabled(true)
        var renderTimeGraphRange = 4f
        binding.animationView.performanceTracker?.addFrameListener { ms ->
            if (lifecycle.currentState != Lifecycle.State.RESUMED) return@addFrameListener
            lineDataSet.getEntryForIndex((binding.animationView.progress * 100).toInt()).y = ms
            renderTimeGraphRange = renderTimeGraphRange.coerceAtLeast(ms * 1.2f)
            binding.controlBarPlayerControls.renderTimesGraph.setVisibleYRange(0f, renderTimeGraphRange, YAxis.AxisDependency.LEFT)
            binding.controlBarPlayerControls.renderTimesGraph.invalidate()
        }

        // Scale up to fill the screen
        binding.controlBarScale.scaleSeekBar.progress = 100

        binding.bottomSheetKeyPaths.keyPathsRecyclerView.buildModelsWith(object : EpoxyRecyclerView.ModelBuilderCallback {
            override fun buildModels(controller: EpoxyController) {
                binding.animationView.resolveKeyPath(KeyPath("**")).forEachIndexed { index, keyPath ->
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
        binding.bottomSheetRenderTimes.renderTimesContainer.removeAllViews()
        binding.animationView.performanceTracker?.sortedRenderTimes?.forEach {
            val view = BottomSheetItemView(requireContext()).apply {
                set(
                        it.first!!.replace("__container", "Total"),
                        "%.2f ms".format(it.second!!)
                )
            }
            binding.bottomSheetRenderTimes.renderTimesContainer.addView(view)
        }
    }

    private fun updateWarnings() = withState(viewModel) { state ->
        // Force warning to update
        binding.bottomSheetWarnings.warningsContainer.removeAllViews()

        val warnings = state.composition()?.warnings ?: emptySet<String>()
        if (!warnings.isEmpty() && warnings.size == binding.bottomSheetWarnings.warningsContainer.childCount) return@withState

        binding.bottomSheetWarnings.warningsContainer.removeAllViews()
        warnings.forEach {
            val view = BottomSheetItemView(requireContext()).apply {
                set(it)
            }
            binding.bottomSheetWarnings.warningsContainer.addView(view)
        }

        val size = warnings.size
        binding.controlBar.warningsButton.setText(resources.getQuantityString(R.plurals.warnings, size, size))
        binding.controlBar.warningsButton.setImageResource(
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

    private fun beginDelayedTransition() = TransitionManager.beginDelayedTransition(binding.container, transition)

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

        val animationSpeed: Float = abs(animation.speed)

        val totalTime = ((animation.duration / animationSpeed) / 1000.0)
        val totalTimeFormatted = ("%.1f").format(totalTime)

        val progress = (totalTime / 100.0) * ((animation.progress * 100.0).roundToInt())
        val progressFormatted = ("%.1f").format(progress)

        return "$currentFrame/$totalFrames\n$progressFormatted/$totalTimeFormatted"
    }
}
