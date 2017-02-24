package com.airbnb.lottie;

import com.airbnb.lottie.samples.MainActivity;
import com.facebook.testing.screenshot.Screenshot;
import com.facebook.testing.screenshot.ViewHelpers;

import java.util.Locale;
import java.util.concurrent.Semaphore;

class TestRobot {
  private static final float[] DEFAULT_ANIMATED_PROGRESS = {0f, 0.05f, 0.10f, 0.2f, 0.3f, 0.4f,
      0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 0.95f, 1f};

  static void testStatic(MainActivity activity, String fileName) {
    testAnimation(activity, fileName, null, new float[] {0});
  }

  static void testAnimation(MainActivity activity, String fileName) {
    testAnimation(activity, fileName, null, DEFAULT_ANIMATED_PROGRESS);
  }

  static void testAnimation(MainActivity activity, String fileName, String imageAssetsFolder) {
    testAnimation(activity, fileName, imageAssetsFolder, DEFAULT_ANIMATED_PROGRESS);
  }

  static void testAnimation(MainActivity activity, String fileName, String imageAssetsFolder,
      float[] progress) {
    final LottieAnimationView view = new LottieAnimationView(activity);
    view.setImageAssetsFolder(imageAssetsFolder);
    view.setComposition(LottieComposition.Factory.fromFileSync(activity, fileName));
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
    final Semaphore semaphore = new Semaphore(1);
    activity.runOnUiThread(new Runnable() {
      @Override public void run() {
        view.recycleBitmaps();
        semaphore.release();
      }
    });
    try {
      semaphore.acquire();
    } catch (InterruptedException e) {
      // Do nothing.
    }
  }
}
