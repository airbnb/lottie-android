package com.airbnb.lottie.samples

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.airbnb.lottie.samples.model.CompositionArgs
import kotlinx.android.synthetic.main.fragment_preview.*

private val RC_FILE = 1000
class PreviewFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_preview, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        qr.apply {
            addIcon(R.drawable.ic_device)
            setOnClickListener {
                startActivity(QRScanActivity.intent(context))
            }
        }

        file.apply {
            addIcon(R.drawable.ic_device)
            setOnClickListener {
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
        }

        url.apply {
            addIcon(R.drawable.ic_device)
            setOnClickListener {
                startActivity(PlayerActivity.intent(context, CompositionArgs(assetName = "HamburgerArrow.json")))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            RC_FILE-> startActivity(PlayerActivity.intent(requireContext(), CompositionArgs(fileUri = data?.data)))
        }
    }
}