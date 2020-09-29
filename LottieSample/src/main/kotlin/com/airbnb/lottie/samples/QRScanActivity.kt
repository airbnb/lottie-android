package com.airbnb.lottie.samples

import android.content.Context
import android.content.Intent
import android.graphics.PointF
import android.os.Bundle
import android.os.Vibrator
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.samples.databinding.QrscanActivityBinding
import com.airbnb.lottie.samples.model.CompositionArgs
import com.airbnb.lottie.samples.utils.vibrateCompat
import com.airbnb.lottie.samples.utils.viewBinding
import com.dlazaro66.qrcodereaderview.QRCodeReaderView

class QRScanActivity : AppCompatActivity(), QRCodeReaderView.OnQRCodeReadListener {
    private val binding: QrscanActivityBinding by viewBinding()
    private val vibrator by lazy { getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }

    // Sometimes the qr code is read twice in rapid succession. This prevents it from being read
    // multiple times.
    private var hasReadQrCode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.qrView.setQRDecodingEnabled(true)
        binding.qrView.setAutofocusInterval(2000L)
        binding.qrView.setBackCamera()
        binding.qrView.setOnQRCodeReadListener(this)
        binding.qrView.setOnClickListener { binding.qrView.forceAutoFocus() }
    }

    override fun onResume() {
        super.onResume()
        binding.qrView.startCamera()
        hasReadQrCode = false
    }

    override fun onPause() {
        super.onPause()
        binding.qrView.stopCamera()
    }

    override fun onQRCodeRead(url: String, pointFS: Array<PointF>) {
        if (hasReadQrCode) return
        hasReadQrCode = true
        vibrator.vibrateCompat(100)
        finish()
        startActivity(PlayerActivity.intent(this, CompositionArgs(url = url)))
    }

    companion object {
        fun intent(context: Context) = Intent(context, QRScanActivity::class.java)
    }
}