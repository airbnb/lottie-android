package com.airbnb.lottie.samples

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import com.airbnb.lottie.samples.model.CompositionArgs
import kotlinx.android.synthetic.main.fragment_preview.*

private const val RC_FILE = 1000
private const val RC_CAMERA_PERMISSION = 1001
class PreviewFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_preview, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        qr.setOnClickListener {
            if (requireContext().hasPermission(Manifest.permission.CAMERA)) {
                startActivity(QRScanActivity.intent(requireContext()))
            } else {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), RC_CAMERA_PERMISSION)
            }
        }

        file.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "*/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
                startActivityForResult(Intent.createChooser(intent, "Select a JSON file"), RC_FILE)
            } catch (ex: android.content.ActivityNotFoundException) {
                // Potentially direct the user to the Market with a Dialog
                Toast.makeText(context, "Please install a File Manager.", Toast.LENGTH_SHORT).show()
            }
        }

        url.setOnClickListener {
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

        assets.setOnClickListener {
            val adapter = ArrayAdapter<String>(requireContext(), android.R.layout.select_dialog_item).apply {
                requireContext().assets.list("").forEach {
                    if (it.endsWith(".json")) {
                        add(it)
                    }
                }
            }
            AlertDialog.Builder(context)
                    .setAdapter(adapter) { _, which ->
                        val args = CompositionArgs(asset = adapter.getItem(which))
                        startActivity(PlayerActivity.intent(requireContext(), args))
                    }
                    .show()
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
                    Snackbar.make(container, R.string.qr_permission_denied, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }
}