package com.airbnb.lottie;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.airbnb.happo.SnapshotProvider;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class LottieSnapshotProvider extends SnapshotProvider {

  private static final float[] PROGRESS = {0f, 0.25f, 0.5f, 0.75f, 1f};
  private static final int CORES = 1; //Runtime.getRuntime().availableProcessors();

  private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
  private final ThreadPoolExecutor executor = new ThreadPoolExecutor(CORES, CORES, 15, TimeUnit.MINUTES, queue);
  private final Context context;

  private int remainingTasks = 0;

  LottieSnapshotProvider(Context context) {
    this.context = context;
  }

  @Override
  public void beginSnapshotting() {
    try {
      snapshotAssets(context.getAssets().list(""));
      String[] tests = context.getAssets().list("Tests");
      for (int i = 0; i < tests.length; i++) {
        tests[i] = "Tests/" + tests[i];
      }
      snapshotAssets(tests);
    } catch (IOException e) {
      onError(e);
    }
    testFrameBoundary();
    testScaleTypes();
  }

  private void snapshotAssets(String[] animations) {
    File dir = new File(Environment.getExternalStorageDirectory() + "/Snapshots");
    //noinspection ResultOfMethodCallIgnored
    dir.mkdirs();
    for (File file : dir.listFiles()) {
      //noinspection ResultOfMethodCallIgnored
      file.delete();
    }
    for (final String animation : animations) {
      if (!animation.contains(".json")) {
        continue;
      }
      remainingTasks += 1;
      executor.execute(new Runnable() {
        @Override
        public void run() {
          runAnimation(animation);
          decrementAndCompleteIfDone();
        }
      });
    }
  }

  private void runAnimation(final String name) {
    LottieComposition composition = LottieComposition.Factory.fromFileSync(context, name);
    if (composition.getBounds().width() > Resources.getSystem().getDisplayMetrics().widthPixels ||
        composition.getBounds().height() > Resources.getSystem().getDisplayMetrics().heightPixels) {
      return;
    }
    drawComposition(composition, name);
  }

  private void drawComposition(LottieComposition composition, String name) {
    LottieAnimationView view = new LottieAnimationView(context);
    view.setComposition(composition);
    for (float progress : PROGRESS) {
      view.setProgress(progress);
      recordSnapshot(view, 1080, "android", name, Integer.toString((int) (progress * 100)));
    }
  }

  @Override
  public void stopSnapshotting() {
    queue.clear();
  }

  private void decrementAndCompleteIfDone() {
    remainingTasks--;
    Log.d("Happo", "There are " + remainingTasks + " remaining tasks.");
    if (remainingTasks < 0) {
      throw new IllegalStateException("Remaining tasks cannot be negative.");
    }
    if (remainingTasks == 0) {
      onComplete();
    }
  }

  private void testScaleTypes() {
    LottieComposition composition = LottieComposition.Factory.fromFileSync(
        context, "LottieLogo1.json");

    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    testScaleTypeView(context, composition, "Wrap Content", params, null, null);

    params = new FrameLayout.LayoutParams(dpToPx(300), dpToPx(300));
    testScaleTypeView(context, composition, "300x300 @4x", params, null, 4f);

    params = new FrameLayout.LayoutParams(dpToPx(300), dpToPx(300));
    testScaleTypeView(context, composition, "300x300 centerCrop", params,
        ImageView.ScaleType.CENTER_CROP, null);

    params = new FrameLayout.LayoutParams(dpToPx(300), dpToPx(300));
    testScaleTypeView(context, composition, "300x300 centerInside", params,
        ImageView.ScaleType.CENTER_INSIDE, null);

    params = new FrameLayout.LayoutParams(dpToPx(300), dpToPx(300));
    testScaleTypeView(context, composition, "300x300 centerInside @2x", params,
        ImageView.ScaleType.CENTER_INSIDE, 2f);

    params = new FrameLayout.LayoutParams(dpToPx(300), dpToPx(300));
    testScaleTypeView(context, composition, "300x300 centerCrop @2x", params,
        ImageView.ScaleType.CENTER_CROP, 2f);

    params = new FrameLayout.LayoutParams(dpToPx(300), dpToPx(300));
    testScaleTypeView(context, composition, "300x300 @2x", params,
        null, 2f);

    params = new FrameLayout.LayoutParams(dpToPx(600), dpToPx(300));
    testScaleTypeView(context, composition, "600x300 centerInside", params,
        ImageView.ScaleType.CENTER_INSIDE, null);

    params = new FrameLayout.LayoutParams(dpToPx(300), dpToPx(600));
    testScaleTypeView(context, composition, "300x600 centerInside", params,
        ImageView.ScaleType.CENTER_INSIDE, null);

    params = new FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    testScaleTypeView(context, composition, "Match Parent", params, null, null);
  }

  private void testScaleTypeView(Context context, LottieComposition composition,
      String name, FrameLayout.LayoutParams params, @Nullable ImageView.ScaleType scaleType,
      @Nullable Float scale) {
    FrameLayout container = new FrameLayout(context);
    LottieAnimationView animationView = new LottieAnimationView(context);
    animationView.setComposition(composition);
    animationView.setProgress(1f);
    if (scaleType != null) {
      animationView.setScaleType(scaleType);
    }
    if (scale != null) {
      animationView.setScale(scale);
    }
    container.addView(animationView, params);

    recordSnapshot(container, 1080, "android", "Scale Types", name, new ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
  }

  private void testFrameBoundary() {
    LottieAnimationView animationView = new LottieAnimationView(context);
    LottieComposition composition =
        LottieComposition.Factory.fromFileSync(context, "Tests/Frame.json");
    animationView.setComposition(composition);
    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    animationView.setFrame(16);
    recordSnapshot(animationView, 1080, "android", "Frame Boundary", "Frame 16 Red", params);
    animationView.setFrame(17);
    recordSnapshot(animationView, 1080, "android", "Frame Boundary", "Frame 17 Blue", params);
    animationView.setFrame(50);
    recordSnapshot(animationView, 1080, "android", "Frame Boundary", "Frame 50 Blue", params);
    animationView.setFrame(51);
    recordSnapshot(animationView, 1080, "android", "Frame Boundary", "Frame 51 Green", params);
  }

  private int dpToPx(int dp) {
    Resources resources = context.getResources();
    return (int) TypedValue.applyDimension(1, (float) dp, resources.getDisplayMetrics());
  }
}
