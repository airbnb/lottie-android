package com.airbnb.lottie.samples

import android.content.Context
import android.content.Intent
import android.graphics.PointF
import android.os.Bundle
import android.os.Vibrator
import android.support.v7.app.AppCompatActivity
import com.airbnb.lottie.samples.model.CompositionArgs
import com.dlazaro66.qrcodereaderview.QRCodeReaderView
import kotlinx.android.synthetic.main.activity_qrscan.*

class QRScanActivity : AppCompatActivity(), QRCodeReaderView.OnQRCodeReadListener {
    private val vibrator by lazy { getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }

    // Sometimes the qr code is read twice in rapid succession. This prevents it from being read
    // multiple times.
    private var hasReadQrCode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrscan)

        qrView.setQRDecodingEnabled(true)
        qrView.setAutofocusInterval(2000L)
        qrView.setBackCamera()
        qrView.setOnQRCodeReadListener(this)
        qrView.setOnClickListener { qrView.forceAutoFocus() }
    }

    override fun onResume() {
        super.onResume()
        qrView.startCamera()
        hasReadQrCode = false
    }

    override fun onPause() {
        super.onPause()
        qrView.stopCamera()
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
