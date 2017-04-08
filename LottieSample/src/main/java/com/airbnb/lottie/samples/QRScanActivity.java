package com.airbnb.lottie.samples;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class QRScanActivity extends AppCompatActivity
    implements QRCodeReaderView.OnQRCodeReadListener {

  @BindView(R.id.qrdecoderview) QRCodeReaderView qrCodeReaderView;
  private Vibrator vibrator;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_qrscan);
    ButterKnife.bind(this);

    vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

    /*
     * Initialize the QR Scanner component
     */
    qrCodeReaderView.setQRDecodingEnabled(true);
    qrCodeReaderView.setAutofocusInterval(2000L);
    qrCodeReaderView.setBackCamera();
    qrCodeReaderView.setOnQRCodeReadListener(this);
    qrCodeReaderView.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        qrCodeReaderView.forceAutoFocus();
      }
    });

  }

  @Override
  protected void onPause() {
    super.onPause();
    qrCodeReaderView.stopCamera();
  }

  @Override public void onQRCodeRead(String s, PointF[] pointFS) {
    vibrator.vibrate(100);

    Intent resultIntent = new Intent();
    resultIntent.putExtra(AnimationFragment.EXTRA_URL, s);
    setResult(Activity.RESULT_OK, resultIntent);
    finish();
  }

  @Override
  protected void onResume() {
    super.onResume();
    qrCodeReaderView.startCamera();
  }



}
