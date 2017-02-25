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
    testAnimation(activity, fileName, null, new float[]{0});
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
    loadCompositionOnView(view, imageAssetsFolder, fileName);
    recordScreenshots(view, fileName, progress);
    cleanUpView(view, activity);
  }

  static void testChangingCompositions(MainActivity activity, String firstFile, String secondFile) {
    final LottieAnimationView view = new LottieAnimationView(activity);
    loadCompositionOnView(view, null, firstFile);
    recordScreenshots(view, "test_changing_compositions_" + firstFile, DEFAULT_ANIMATED_PROGRESS);

    loadCompositionOnView(view, null, secondFile);
    recordScreenshots(view, "test_changing_compositions_" + secondFile, DEFAULT_ANIMATED_PROGRESS);

    cleanUpView(view, activity);
  }

  static void testSettingSameComposition(MainActivity activity, String fileName) {
    final LottieAnimationView view = new LottieAnimationView(activity);
    loadCompositionOnView(view, null, fileName);
    recordScreenshots(view, "same_composition_first_run_" + fileName, DEFAULT_ANIMATED_PROGRESS);

    loadCompositionOnView(view, null, fileName);
    recordScreenshots(view, "same_composition_second_run_" + fileName, DEFAULT_ANIMATED_PROGRESS);

    cleanUpView(view, activity);
  }

  private static void loadCompositionOnView(LottieAnimationView view, String imageAssetsFolder,
      String fileName) {

    LottieComposition composition =
        LottieComposition.Factory.fromFileSync(view.getContext(), fileName);

    view.setImageAssetsFolder(imageAssetsFolder);
    view.setComposition(composition);

    ViewHelpers
        .setupView(view)
        .layout();
  }

  private static void recordScreenshots(LottieAnimationView view, String fileName,
      float[] progress) {
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
  }

  private static void cleanUpView(final LottieAnimationView view, MainActivity activity) {
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
