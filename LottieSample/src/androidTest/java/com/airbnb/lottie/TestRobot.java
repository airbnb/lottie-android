package com.airbnb.lottie;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.airbnb.lottie.samples.MainActivity;
import com.airbnb.lottie.samples.R;
import com.airbnb.lottie.samples.TestColorFilterActivity;
import com.facebook.testing.screenshot.Screenshot;
import com.facebook.testing.screenshot.ViewHelpers;

import java.util.concurrent.Semaphore;

class TestRobot {

  static void testLinearAnimation(MainActivity activity, String fileName) {
    testLinearAnimation(activity, fileName, null, null);
  }

  static void testLinearAnimation(MainActivity activity, String fileName,
      @Nullable String imageAssetsFolder, @Nullable TextDelegate textDelegate) {
    LottieComposition composition =
        LottieComposition.Factory.fromFileSync(activity, fileName);
    AnimationLinearLayout view = new AnimationLinearLayout(activity);

    view.setImageAssetsFolder(imageAssetsFolder);
    view.setComposition(composition);
    view.setTextDelegate(textDelegate);


    ViewHelpers
        .setupView(view)
        .layout();

    String nameWithoutExtension = fileName
        .substring(0, fileName.lastIndexOf('.'))
        .replace(" ", "_")
        .replace("/", "_");
    Screenshot.snap(view)
        .setGroup(fileName)
        .setName(nameWithoutExtension)
        .record();
  }

  static void testChangingCompositions(MainActivity activity, String firstFile, String secondFile) {
    final AnimationLinearLayout view = new AnimationLinearLayout(activity);
    loadCompositionOnView(view, null, firstFile);
    recordScreenshots(view, "test_changing_compositions_" + firstFile);

    loadCompositionOnView(view, null, secondFile);
    recordScreenshots(view, "test_changing_compositions_" + secondFile);

    cleanUpView(view, activity);
  }

  static void testSettingSameComposition(MainActivity activity, String fileName) {
    final AnimationLinearLayout view = new AnimationLinearLayout(activity);
    loadCompositionOnView(view, null, fileName);
    recordScreenshots(view, "same_composition_first_run_" + fileName);

    loadCompositionOnView(view, null, fileName);
    recordScreenshots(view, "same_composition_second_run_" + fileName);

    cleanUpView(view, activity);
  }

  private static void loadCompositionOnView(AnimationLinearLayout view, String imageAssetsFolder,
      String fileName) {

    LottieComposition composition =
        LottieComposition.Factory.fromFileSync(view.getContext(), fileName);

    view.setImageAssetsFolder(imageAssetsFolder);
    view.setComposition(composition);

    ViewHelpers
        .setupView(view)
        .layout();
  }

  private static void recordScreenshots(AnimationLinearLayout view, String fileName) {
    String nameWithoutExtension = fileName
        .substring(0, fileName.lastIndexOf('.'))
        .replace(" ", "_")
        .replace("/", "_");
    Screenshot.snap(view)
        .setGroup(fileName)
        .setName(nameWithoutExtension)
        .record();
  }

  private static void cleanUpView(final AnimationLinearLayout view, MainActivity activity) {
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

  static void testAddYellowColorFilterInXml(final TestColorFilterActivity activity) {
    View view = activity.findViewById(R.id.yellow_color_filter);
    Screenshot.snap(view)
        .setGroup("test_color_filter")
        .setName("yellow_color_filter")
        .record();
  }

  static void testAddNullColorFilterInXml(final TestColorFilterActivity activity) {
    View view = activity.findViewById(R.id.null_color_filter);
    Screenshot.snap(view)
        .setGroup("test_color_filter")
        .setName("null_color_filter")
        .record();
  }

  static void testAnimationView(Context context, LottieComposition composition,
      String name, FrameLayout.LayoutParams params, @Nullable ImageView.ScaleType scaleType) {
    FrameLayout container = new FrameLayout(context);
    LottieAnimationView animationView = new LottieAnimationView(context);
    animationView.setComposition(composition);
    animationView.setProgress(1f);
    if (scaleType != null) {
      animationView.setScaleType(scaleType);
    }
    container.addView(animationView, params);

    ViewHelpers.setupView(container)
        .setExactWidthDp(500)
        .setExactHeightDp(500)
        .layout();

    Screenshot.snap(container)
        .setGroup("Container")
        .setName(name)
        .record();
  }
}
