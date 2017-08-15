package com.airbnb.lottie.samples

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.Toast
import com.airbnb.lottie.L
import com.airbnb.lottie.LottieComposition
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.fragment_animation.*
import kotlinx.android.synthetic.main.fragment_animation.view.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList

private inline fun consume(f: () -> Unit): Boolean {
    f()
    return true
}

class AnimationFragment : Fragment() {
    private val TAG = AnimationFragment::class.java.simpleName
    private val RC_CAMERA = 1341
    private val RC_ASSET = 1337
    private val RC_FILE = 1338
    private val RC_QR = 1340
    private val SCALE_SLIDER_FACTOR = 50f

    private val assetFolders = HashMap<String, String>().apply {
        put("WeAccept.json", "Images/WeAccept")
    }

    private val handler = Handler()
    private val client: OkHttpClient by lazy { OkHttpClient() }
    private val application: ILottieApplication
        get() = activity.application as ILottieApplication
    private var renderTimeGraphRange = 4f
    private val lineDataSet by lazy {
        val entries = ArrayList<Entry>(101)
        repeat(101) { i ->
            entries.add(Entry(i.toFloat(), 0f))
        }
        val dataSet = LineDataSet(entries, "Render Times")
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        dataSet.cubicIntensity = 0.3f
        dataSet.setDrawCircles(false)
        dataSet.lineWidth = 1.8f
        dataSet.color = Color.BLACK
        dataSet
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = container!!.inflate(R.layout.fragment_animation, false)

        L.setTraceEnabled(true)
        view.animationView.setPerformanceTrackingEnabled(true)

        (activity as AppCompatActivity).setSupportActionBar(view.toolbar)
        view.toolbar.setNavigationIcon(R.drawable.ic_back)
        view.toolbar.setNavigationOnClickListener { fragmentManager.popBackStack() }
        setHasOptionsMenu(true)
        postUpdatePlayButtonText()

        view.qrCode.setDrawableLeft(R.drawable.ic_qr_scan, activity)
        view.sampleAnimations.setDrawableLeft(R.drawable.ic_assets, activity)
        view.loadAnimation.setDrawableLeft(R.drawable.ic_file, activity)
        view.loadFromJson.setDrawableLeft(R.drawable.ic_network, activity)
        view.overflowMenu.setDrawableLeft(R.drawable.ic_more_vert, activity)

        view.animationView.addAnimatorListener(AnimatorListenerAdapter(
                onStart = { startRecordingDroppedFrames() },
                onEnd = {
                    recordDroppedFrames()
                    postUpdatePlayButtonText()
                    animationView.performanceTracker?.logRenderTimes()
                },
                onCancel = { postUpdatePlayButtonText() },
                onRepeat =  {
                    animationView.performanceTracker?.logRenderTimes()
                    animationView.performanceTracker?.clearRenderTimes()
                    recordDroppedFrames()
                    startRecordingDroppedFrames()
                }
        ))

        view.animationView.addAnimatorUpdateListener {
            animation -> seekBar.progress = ((animation.animatedValue as Float) * 100f).toInt()
        }

        view.seekBar.setOnSeekBarChangeListener(OnSeekBarChangeListenerAdapter(
            onProgressChanged = { _, progress, _ ->
                if (!animationView.isAnimating) {
                    animationView.progress = progress / 100f
                }
            }
        ))

        view.trimView.setCallback({ startProgress, endProgress ->
            animationView.setMinAndMaxProgress(startProgress, endProgress)
            animationView.progress = startProgress
        })

        view.scaleSeekBar.setOnSeekBarChangeListener(OnSeekBarChangeListenerAdapter(
                onProgressChanged = { _, progress, _ ->
                    animationView.scale = progress / SCALE_SLIDER_FACTOR
                    scaleText.text = String.format(Locale.US, "%.2f", animationView.scale)
                }
        ))

        view.playButton.setOnClickListener {
            if (animationView.isAnimating) {
                animationView.pauseAnimation()
                postUpdatePlayButtonText()
            } else {
                if (animationView.progress == 1f) {
                    animationView.progress = 0f
                }
                animationView.resumeAnimation()
                postUpdatePlayButtonText()
            }
        }

        view.loop.setOnClickListener {
            view.loop.isActivated = !view.loop.isActivated
            view.animationView.loop(view.loop.isActivated)
        }
        view.loop.callOnClick()

        view.qrScan.setOnClickListener {
            animationView.cancelAnimation()
            if (!Manifest.permission.CAMERA.hasPermission(context)) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), RC_CAMERA)

            } else {
                startActivityForResult(Intent(context, QRScanActivity::class.java), RC_QR)
            }
        }

        view.invertColors.setOnClickListener {
            animationContainer.isActivated = !animationContainer.isActivated
            invertColors.isActivated = animationContainer.isActivated
        }

        view.loadAsset.setOnClickListener {
            animationView.cancelAnimation()
            val assetFragment = ChooseAssetDialogFragment.newInstance()
            assetFragment.setTargetFragment(this, RC_ASSET)
            assetFragment.show(fragmentManager, "assets")
        }

        view.loadFile.setOnClickListener {
            animationView.cancelAnimation()
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)

            try {
                startActivityForResult(Intent.createChooser(intent, "Select a JSON file"), RC_FILE)
            } catch (ex: android.content.ActivityNotFoundException) {
                // Potentially direct the user to the Market with a Dialog
                Toast.makeText(context, "Please install a File Manager.", Toast.LENGTH_SHORT).show()
            }
        }

        view.loadUrlOrJson.setOnClickListener {
            animationView.cancelAnimation()
            val urlOrJsonView = EditText(context)
            AlertDialog.Builder(context)
                    .setTitle("Enter a URL or JSON string")
                    .setView(urlOrJsonView)
                    .setPositiveButton("Load") { _, _ -> loadUrlOrJson(urlOrJsonView.text.toString()) }
                    .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                    .show()
        }

        view.renderTimesGraph.axisRight.isEnabled = false
        view.renderTimesGraph.xAxis.isEnabled = false
        view.renderTimesGraph.legend.isEnabled = false
        view.renderTimesGraph.description = null
        view.renderTimesGraph.data = LineData(lineDataSet)
        view.renderTimesGraph.axisLeft.setDrawGridLines(false)
        view.renderTimesGraph.axisLeft.labelCount = 4
        val ll1 = LimitLine(16f, "60fps")
        ll1.lineColor = Color.RED
        ll1.lineWidth = 1.2f
        ll1.textColor = Color.BLACK
        ll1.textSize = 8f
        view.renderTimesGraph.axisLeft.addLimitLine(ll1)

        val ll2 = LimitLine(32f, "30fps")
        ll2.lineColor = Color.RED
        ll2.lineWidth = 1.2f
        ll2.textColor = Color.BLACK
        ll2.textSize = 8f
        view.renderTimesGraph.axisLeft.addLimitLine(ll2)

        return view
    }

    override fun onStop() {
        animationView.cancelAnimation()
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) =
        inflater.inflate(R.menu.fragment_animation, menu)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.isChecked = !item.isChecked
        return when (item.itemId) {
            R.id.hardware_acceleration -> consume {
                animationView.useHardwareAcceleration(item.isChecked)
            }
            R.id.merge_paths -> consume {
                animationView.enableMergePathsForKitKatAndAbove(item.isChecked)
            }
            R.id.render_times_graph -> consume {
                renderTimesGraphContainer.setVisibleIf(item.isChecked)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null) {
            return
        }

        when (requestCode) {
            RC_ASSET -> {
                val assetName = data.getStringExtra(EXTRA_ANIMATION_NAME)
                animationView.imageAssetsFolder = assetFolders[assetName]
                LottieComposition.Factory.fromAssetFileName(context, assetName, { composition ->
                            if (composition == null) {
                                onLoadError()
                            } else {
                                setComposition(composition, assetName)
                            }
                        })
            }
            RC_FILE -> onFileLoaded(data.data)
            RC_QR -> loadUrl(data.extras.getString(EXTRA_URL))
        }
    }

    private fun setComposition(composition: LottieComposition, name: String) {
        if (composition.hasImages() && TextUtils.isEmpty(animationView.imageAssetsFolder)) {
            view!!.showSnackbarLong("This animation has images and no image folder was set")
            return
        }
        instructions.visibility = View.GONE
        seekBar.progress = 0
        animationView.setComposition(composition)
        animationName.text = name
        scaleText.text = String.format(Locale.US, "%.2f", animationView.scale)
        scaleSeekBar.progress = (animationView.scale * SCALE_SLIDER_FACTOR).toInt()
        setWarnings(composition.warnings)
        renderTimeGraphRange = 8f
        for (i in 1..lineDataSet.entryCount - 1) {
            lineDataSet.getEntryForIndex(i).y = 0f
        }
        renderTimesGraph.invalidate()
        animationView.performanceTracker?.addFrameListener { ms ->
            if (renderTimesGraph == null) {
                return@addFrameListener
            }
            lineDataSet.getEntryForIndex((animationView.progress * 100).toInt()).y = ms
            renderTimeGraphRange = Math.max(renderTimeGraphRange, ms * 1.2f)
            renderTimesGraph.setVisibleYRange(0f, renderTimeGraphRange, YAxis.AxisDependency.LEFT)
            renderTimesGraph.invalidate()
        }
    }

    private fun setWarnings(warningsList: ArrayList<String>) {
        val size = warningsList.size
        warnings.visibility = if (size == 0) View.GONE else View.VISIBLE
        warnings.text = resources.getQuantityString(R.plurals.warnings, size, size)
        warnings.setOnClickListener {
            WarningsDialogFragment.newInstance(warningsList).show(fragmentManager, null)
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == RC_CAMERA && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            startActivityForResult(Intent(context, QRScanActivity::class.java), RC_QR)
        } else {
            view!!.showSnackbarLong(R.string.permission_required)
        }
    }

    private fun postUpdatePlayButtonText() = handler.post {
        if (playButton != null) {
            updatePlayButtonText()
        }
    }

    private fun updatePlayButtonText() {
        playButton.isActivated = animationView.isAnimating
    }

    private fun onFileLoaded(uri: Uri) {
        val fis: InputStream

        try {
            when (uri.scheme) {
                "file" -> fis = FileInputStream(uri.path)
                "content" -> fis = context.contentResolver.openInputStream(uri)
                else -> {
                    onLoadError()
                    return
                }
            }
        } catch (e: FileNotFoundException) {
            onLoadError()
            return
        }

        LottieComposition.Factory.fromInputStream(context, fis, { composition ->
                    if (composition == null) {
                        onLoadError()
                    } else {
                        setComposition(composition, uri.path)
                    }
                })
    }

    private fun loadUrlOrJson(text: String) {
        if (text[0] == '{') {
            // Assume JSON
            loadJsonString(text)
        } else {
            loadUrl(text)
        }
    }

    private fun loadJsonString(jsonString: String) {
        try {
            val json = JSONObject(jsonString)
            LottieComposition.Factory.fromJson(resources, json, { composition ->
                        if (composition == null) {
                            onLoadError()
                        } else {
                            setComposition(composition, "Animation")
                        }
                    })
        } catch (e: JSONException) {
            onLoadError()
        }

    }

    private fun loadUrl(url: String) {
        val request: Request
        try {
            request = Request.Builder()
                    .url(url)
                    .build()
        } catch (e: IllegalArgumentException) {
            onLoadError()
            return
        }
        client.newCall(request)?.enqueue(OkHttpCallback(
                onFailure = { _, _ -> onLoadError() },
                onResponse = { _, response ->
                    if (!response.isSuccessful) {
                        onLoadError()
                    } else {
                        loadJsonString(response.body().string())
                    }

                }))
    }

    private fun onLoadError() = view!!.showSnackbarLong("Failed to load animation")

    private fun startRecordingDroppedFrames() = application.startRecordingDroppedFrames()

    private fun recordDroppedFrames() {
        val droppedFrames = application.stopRecordingDroppedFrames()
        Log.d(TAG, "Dropped frames: " + droppedFrames.first)
    }

    companion object {
        @JvmField internal val EXTRA_ANIMATION_NAME = "animation_name"
        @JvmField internal val EXTRA_URL = "json_url"

        internal fun newInstance(): AnimationFragment {
            return AnimationFragment()
        }
    }
}