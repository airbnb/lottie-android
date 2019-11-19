package com.airbnb.lottie;

import android.app.Activity;
import android.view.LayoutInflater;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.Robolectric;

public class LottieAnimationViewTest extends BaseTest {

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
