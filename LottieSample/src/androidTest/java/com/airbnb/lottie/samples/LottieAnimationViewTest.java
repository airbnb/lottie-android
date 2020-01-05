package com.airbnb.lottie.samples;

import android.app.Activity;
import android.os.Build;
import android.view.LayoutInflater;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
public class LottieAnimationViewTest {

  private Activity activity;

  @Before
  public void setup(){
    activity = Robolectric.buildActivity(Activity.class).create().get();
  }

  @Test
  public void inflateShouldNotCrash() {
    LayoutInflater.from(activity).inflate(R.layout.lottie_activity_main, null);
  }
}
