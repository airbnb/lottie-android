package com.airbnb.lottie.samples

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import com.google.android.material.snackbar.Snackbar
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import com.airbnb.epoxy.EpoxyController
import com.airbnb.lottie.samples.model.CompositionArgs
import com.airbnb.lottie.samples.utils.BaseEpoxyFragment
import com.airbnb.lottie.samples.utils.hasPermission
import com.airbnb.lottie.samples.views.marquee

private const val RC_FILE = 1000
private const val RC_CAMERA_PERMISSION = 1001
class PreviewFragment : BaseEpoxyFragment() {

    override fun EpoxyController.buildModels() {
        marquee {
            id("marquee")
            title(R.string.preview_title)
        }

        previewItemView {
            id("qr")
            title(R.string.preview_qr)
            icon(R.drawable.ic_qr_scan)
            clickListener { _ ->
                if (requireContext().hasPermission(Manifest.permission.CAMERA)) {
                    startActivity(QRScanActivity.intent(requireContext()))
                } else {
                    requestPermissions(arrayOf(Manifest.permission.CAMERA), RC_CAMERA_PERMISSION)
                }
            }
        }

        previewItemView {
            id("file")
            title(R.string.preview_file)
            icon(R.drawable.ic_file)
            clickListener { _ ->
                try {
                    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                        type = "*/*"
                        addCategory(Intent.CATEGORY_OPENABLE)
                    }
                    startActivityForResult(Intent.createChooser(intent, "Select a JSON file"), RC_FILE)
                } catch (ex: ActivityNotFoundException) {
                    // Potentially direct the user to the Market with a Dialog
                    Toast.makeText(context, "Please install a File Manager.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        previewItemView {
            id("url")
            title(R.string.preview_url)
            icon(R.drawable.ic_network)
            clickListener { _ ->
                val urlOrJsonView = EditText(context)
                AlertDialog.Builder(context)
                        .setTitle(R.string.preview_url)
                        .setView(urlOrJsonView)
                        .setPositiveButton(R.string.preview_load) { _, _ ->
                            val args = CompositionArgs(url = urlOrJsonView.text.toString())
                            startActivity(PlayerActivity.intent(requireContext(), args))
                        }
                        .setNegativeButton(R.string.preview_cancel) { dialog, _ -> dialog.dismiss() }
                        .show()
            }
        }

        previewItemView {
            id("assets")
            title(R.string.preview_assets)
            icon(R.drawable.ic_storage)
            clickListener { _ ->
                val adapter = ArrayAdapter<String>(requireContext(), android.R.layout.select_dialog_item)
                requireContext().assets.list("")?.asSequence()
                        ?.filter { it.endsWith(".json") || it.endsWith(".zip") }
                        ?.forEach { adapter.add(it) }
                AlertDialog.Builder(context)
                        .setAdapter(adapter) { _, which ->
                            val args = CompositionArgs(asset = adapter.getItem(which))
                            startActivity(PlayerActivity.intent(requireContext(), args))
                        }
                        .show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            RC_FILE-> startActivity(PlayerActivity.intent(requireContext(), CompositionArgs(fileUri = data?.data)))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            RC_CAMERA_PERMISSION -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                    startActivity(QRScanActivity.intent(requireContext()))
                } else {
                    Snackbar.make(binding.root, R.string.qr_permission_denied, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }
}