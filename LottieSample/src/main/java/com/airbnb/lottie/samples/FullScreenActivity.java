package com.airbnb.lottie.samples;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * To have a full screen animation, make an animation that is wider than the screen and set the
 * scaleType to centerCrop.
 */
public class FullScreenActivity extends AppCompatActivity {

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.fragment_full_screen);
  }
}
