package com.airbnb.lottie;

import android.os.Bundle;
import android.support.test.runner.AndroidJUnitRunner;

import com.facebook.testing.screenshot.ScreenshotRunner;

public class TestRunner extends AndroidJUnitRunner {
  @Override public void onCreate(Bundle arguments) {
    ScreenshotRunner.onCreate(this, arguments);
    super.onCreate(arguments);
  }

  @Override public void finish(int resultCode, Bundle results) {
    ScreenshotRunner.onDestroy();
    super.finish(resultCode, results);
  }
}
