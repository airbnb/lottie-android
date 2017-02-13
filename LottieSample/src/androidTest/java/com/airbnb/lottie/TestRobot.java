package com.airbnb.lottie;

import com.airbnb.lottie.samples.MainActivity;
import com.facebook.testing.screenshot.Screenshot;
import com.facebook.testing.screenshot.ViewHelpers;

import java.util.Locale;

class TestRobot {
  private static final float[] DEFAULT_ANIMATED_PROGRESS = {0f, 0.05f, 0.10f, 0.2f, 0.5f, 1f};

  static void testStatic(MainActivity activity, String fileName) {
    testAnimation(activity, fileName, new float[] {0});
  }

  static void testAnimation(MainActivity activity, String fileName) {
    testAnimation(activity, fileName, DEFAULT_ANIMATED_PROGRESS);
  }

  static void testAnimation(MainActivity activity, String fileName, float[] progress) {
    LottieAnimationView view = new LottieAnimationView(activity);
    view.setComposition(LottieComposition.fromFileSync(activity, fileName));
    ViewHelpers.setupView(view)
        .layout();

    String nameWithoutExtension = fileName
        .substring(0, fileName.indexOf('.'))
        .replace("/", "_");
    for (float p : progress) {
      view.setProgress(p);
      Screenshot.snap(view)
          .setGroup(fileName)
          .setName(String.format(Locale.US, "%s %d", nameWithoutExtension, (int) (p * 100)))
          .record();
    }
    view.recycleBitmaps();
  }
}
