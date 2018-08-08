package com.airbnb.lottie;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.airbnb.happo.SnapshotProvider;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.value.LottieInterpolatedIntegerValue;
import com.airbnb.lottie.value.LottieRelativeFloatValueCallback;
import com.airbnb.lottie.value.LottieRelativePointValueCallback;
import com.airbnb.lottie.value.LottieValueCallback;
import com.airbnb.lottie.value.ScaleXY;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import retrofit2.http.HEAD;

public class LottieSnapshotProvider extends SnapshotProvider {

  private static final float[] PROGRESS = {0f, 0.25f, 0.5f, 0.75f, 1f};
  private static final int CORES = 1; //Runtime.getRuntime().availableProcessors();

  private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
  private final ThreadPoolExecutor executor = new ThreadPoolExecutor(CORES, CORES, 15, TimeUnit.MINUTES, queue);
  private final Context context;
  // Bitmap to return from an ImageAssetDelegate to make testing animations with images easier.
  private final Bitmap dummyBitmap;

  private int remainingTasks = 0;

  LottieSnapshotProvider(Context context) {
    this.context = context;
    dummyBitmap = BitmapFactory.decodeResource(context.getResources(), com.airbnb.lottie.samples.R.drawable.airbnb);
  }

  @Override
  public void beginSnapshotting() {
    Log.d(L.TAG, "beginSnapshotting");
    try {
      snapshotAssets(context.getAssets().list(""));
      String[] tests = context.getAssets().list("Tests");
      for (int i = 0; i < tests.length; i++) {
        tests[i] = "Tests/" + tests[i];
      }
      snapshotAssets(tests);

      String[] lottiefiles = context.getAssets().list("lottiefiles");
      for (int i = 0; i < lottiefiles.length; i++) {
        lottiefiles[i] = "lottiefiles/" + lottiefiles[i];
      }
      snapshotAssets(lottiefiles);
    } catch (IOException e) {
      onError(e);
    }
    testFrameBoundary();
    testFrameBoundary2();
    testScaleTypes();
    testDynamicProperties();
    testSwitchingToDrawableAndBack();
    testStartEndFrameWithStartEndProgress();
    testUrl();
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
      if (!animation.contains(".json") && !animation.contains(".zip")) {
        continue;
      }
      remainingTasks += 1;
      Log.d(L.TAG, "Enqueueing " + animation);
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
    Log.d(L.TAG, "Running " + name);
    LottieResult<LottieComposition> result = LottieCompositionFactory.fromAssetSync(context, name);
    if (result.getException() != null) throw new IllegalStateException(result.getException());
    LottieComposition composition = result.getValue();

    Rect bounds = composition.getBounds();
    int width = bounds.width();
    int height = bounds.height();
    DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
    if (width > 4 * displayMetrics.widthPixels || height > 4 * displayMetrics.heightPixels) {
      Log.d("Happo", name + " is too large. Skipping (" + width + "x" + height + ")");
      return;
    }
    drawComposition(composition, name);
  }

  private void drawComposition(LottieComposition composition, String name) {
    Log.d(L.TAG, "Drawing " + name);
    LottieAnimationView view = new LottieAnimationView(context);
    view.setImageAssetDelegate(new ImageAssetDelegate() {
      @Override public Bitmap fetchBitmap(LottieImageAsset asset) {
        return dummyBitmap;
      }
    });
    view.setComposition(composition);
    for (float progress : PROGRESS) {
      view.setProgress(progress);
      Log.d(L.TAG, "Recording " + name + " @ " + progress);
      recordSnapshot(view, 1080, "android", name, Integer.toString((int) (progress * 100)));
    }
  }

  @Override
  public void stopSnapshotting() {
    queue.clear();
  }

  private void decrementAndCompleteIfDone() {
    remainingTasks--;
    Log.d(L.TAG, "There are " + remainingTasks + " tasks left.");
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

  private void testFrameBoundary2() {
    LottieAnimationView animationView = new LottieAnimationView(context);
    LottieComposition composition =
        LottieComposition.Factory.fromFileSync(context, "Tests/RGB.json");
    animationView.setComposition(composition);
    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    animationView.setFrame(0);
    recordSnapshot(animationView, 1080, "android", "Frame Boundary", "Frame 0 Red", params);
    animationView.setFrame(1);
    recordSnapshot(animationView, 1080, "android", "Frame Boundary", "Frame 1 Green", params);
    animationView.setFrame(2);
    recordSnapshot(animationView, 1080, "android", "Frame Boundary", "Frame 2 Blue", params);
  }

  private void testDynamicProperties() {
    testDynamicProperty(
        "Fill color (Green)",
        new KeyPath("Shape Layer 1", "Rectangle", "Fill 1"),
        LottieProperty.COLOR,
        new LottieValueCallback<>(Color.GREEN));

    testDynamicProperty(
        "Fill color (Yellow)",
        new KeyPath("Shape Layer 1", "Rectangle", "Fill 1"),
        LottieProperty.COLOR,
        new LottieValueCallback<>(Color.YELLOW));

    testDynamicProperty(
        "Fill opacity",
        new KeyPath("Shape Layer 1", "Rectangle", "Fill 1"),
        LottieProperty.OPACITY,
        new LottieValueCallback<>(50));

    testDynamicProperty(
        "Stroke color",
        new KeyPath("Shape Layer 1", "Rectangle", "Stroke 1"),
        LottieProperty.STROKE_COLOR,
        new LottieValueCallback<>(Color.GREEN));

    testDynamicProperty(
        "Stroke width",
        new KeyPath("Shape Layer 1", "Rectangle", "Stroke 1"),
        LottieProperty.STROKE_WIDTH,
        new LottieRelativeFloatValueCallback(50f));

    testDynamicProperty(
        "Stroke opacity",
        new KeyPath("Shape Layer 1", "Rectangle", "Stroke 1"),
        LottieProperty.OPACITY,
        new LottieValueCallback<>(50));

    testDynamicProperty(
        "Transform anchor point",
        new KeyPath("Shape Layer 1", "Rectangle"),
        LottieProperty.TRANSFORM_ANCHOR_POINT,
        new LottieRelativePointValueCallback(new PointF(20f, 20f)));

    testDynamicProperty(
        "Transform position",
        new KeyPath("Shape Layer 1", "Rectangle"),
        LottieProperty.TRANSFORM_POSITION,
        new LottieRelativePointValueCallback(new PointF(20f, 20f)));

    testDynamicProperty(
        "Transform position (relative)",
        new KeyPath("Shape Layer 1", "Rectangle"),
        LottieProperty.TRANSFORM_POSITION,
        new LottieRelativePointValueCallback(new PointF(20f, 20f)));

    testDynamicProperty(
        "Transform opacity",
        new KeyPath("Shape Layer 1", "Rectangle"),
        LottieProperty.TRANSFORM_OPACITY,
        new LottieValueCallback<>(50));

    testDynamicProperty(
        "Transform rotation",
        new KeyPath("Shape Layer 1", "Rectangle"),
        LottieProperty.TRANSFORM_ROTATION,
        new LottieValueCallback<>(45f));

    testDynamicProperty(
        "Transform scale",
        new KeyPath("Shape Layer 1", "Rectangle"),
        LottieProperty.TRANSFORM_SCALE,
        new LottieValueCallback<>(new ScaleXY(0.5f, 0.5f)));

    testDynamicProperty(
        "Ellipse position",
        new KeyPath("Shape Layer 1", "Ellipse", "Ellipse Path 1"),
        LottieProperty.POSITION,
        new LottieRelativePointValueCallback(new PointF(20f, 20f)));

    testDynamicProperty(
        "Ellipse size",
        new KeyPath("Shape Layer 1", "Ellipse", "Ellipse Path 1"),
        LottieProperty.ELLIPSE_SIZE,
        new LottieRelativePointValueCallback(new PointF(40f, 60f)));

    testDynamicProperty(
        "Star points",
        new KeyPath("Shape Layer 1", "Star", "Polystar Path 1"),
        LottieProperty.POLYSTAR_POINTS,
        new LottieValueCallback<>(8f));

    testDynamicProperty(
        "Star rotation",
        new KeyPath("Shape Layer 1", "Star", "Polystar Path 1"),
        LottieProperty.POLYSTAR_ROTATION,
        new LottieValueCallback<>(10f));

    testDynamicProperty(
        "Star position",
        new KeyPath("Shape Layer 1", "Star", "Polystar Path 1"),
        LottieProperty.POSITION,
        new LottieRelativePointValueCallback(new PointF(20f, 20f)));

    testDynamicProperty(
        "Star inner radius",
        new KeyPath("Shape Layer 1", "Star", "Polystar Path 1"),
        LottieProperty.POLYSTAR_INNER_RADIUS,
        new LottieValueCallback<>(10f));

    testDynamicProperty(
        "Star inner roundedness",
        new KeyPath("Shape Layer 1", "Star", "Polystar Path 1"),
        LottieProperty.POLYSTAR_INNER_ROUNDEDNESS,
        new LottieValueCallback<>(100f));

    testDynamicProperty(
        "Star outer radius",
        new KeyPath("Shape Layer 1", "Star", "Polystar Path 1"),
        LottieProperty.POLYSTAR_OUTER_RADIUS,
        new LottieValueCallback<>(60f));

    testDynamicProperty(
        "Star outer roundedness",
        new KeyPath("Shape Layer 1", "Star", "Polystar Path 1"),
        LottieProperty.POLYSTAR_OUTER_ROUNDEDNESS,
        new LottieValueCallback<>(100f));

    testDynamicProperty(
        "Polygon points",
        new KeyPath("Shape Layer 1", "Polygon", "Polystar Path 1"),
        LottieProperty.POLYSTAR_POINTS,
        new LottieValueCallback<>(8f));

    testDynamicProperty(
        "Polygon rotation",
        new KeyPath("Shape Layer 1", "Polygon", "Polystar Path 1"),
        LottieProperty.POLYSTAR_ROTATION,
        new LottieValueCallback<>(10f));

    testDynamicProperty(
        "Polygon position",
        new KeyPath("Shape Layer 1", "Polygon", "Polystar Path 1"),
        LottieProperty.POSITION,
        new LottieRelativePointValueCallback(new PointF(20f, 20f)));

    testDynamicProperty(
        "Polygon radius",
        new KeyPath("Shape Layer 1", "Polygon", "Polystar Path 1"),
        LottieProperty.POLYSTAR_OUTER_RADIUS,
        new LottieRelativeFloatValueCallback(60f));

    testDynamicProperty(
        "Polygon roundedness",
        new KeyPath("Shape Layer 1", "Polygon", "Polystar Path 1"),
        LottieProperty.POLYSTAR_OUTER_ROUNDEDNESS,
        new LottieValueCallback<>(100f));

    testDynamicProperty(
        "Repeater transform position",
        new KeyPath("Shape Layer 1", "Repeater Shape", "Repeater 1"),
        LottieProperty.TRANSFORM_POSITION,
        new LottieRelativePointValueCallback(new PointF(100f, 100f)));

    testDynamicProperty(
        "Repeater transform start opacity",
        new KeyPath("Shape Layer 1", "Repeater Shape", "Repeater 1"),
        LottieProperty.TRANSFORM_START_OPACITY,
        new LottieValueCallback<>(25f));

    testDynamicProperty(
        "Repeater transform end opacity",
        new KeyPath("Shape Layer 1", "Repeater Shape", "Repeater 1"),
        LottieProperty.TRANSFORM_END_OPACITY,
        new LottieValueCallback<>(25f));

    testDynamicProperty(
        "Repeater transform rotation",
        new KeyPath("Shape Layer 1", "Repeater Shape", "Repeater 1"),
        LottieProperty.TRANSFORM_ROTATION,
        new LottieValueCallback<>(45f));

    testDynamicProperty(
        "Repeater transform scale",
        new KeyPath("Shape Layer 1", "Repeater Shape", "Repeater 1"),
        LottieProperty.TRANSFORM_SCALE,
        new LottieValueCallback<>(new ScaleXY(2f, 2f)));

    testDynamicProperty(
        "Time remapping",
        new KeyPath("Circle 1"),
        LottieProperty.TIME_REMAP,
        new LottieValueCallback<>(1f));

    testDynamicProperty(
        "Color Filter",
        new KeyPath("**"),
        LottieProperty.COLOR_FILTER,
        new LottieValueCallback<ColorFilter>(new SimpleColorFilter(Color.GREEN)));

    LottieValueCallback<ColorFilter> blueColorFilter = new LottieValueCallback<ColorFilter>(new SimpleColorFilter(Color.GREEN));
    LottieAnimationView animationView = new LottieAnimationView(context);
    LottieComposition composition = LottieComposition.Factory.fromFileSync(context, "Tests/Shapes.json");
    animationView.setComposition(composition);
    animationView.setProgress(0f);
    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    animationView.addValueCallback(new KeyPath("**"), LottieProperty.COLOR_FILTER, blueColorFilter);
    recordSnapshot(animationView, 1080, "android", "Dynamic Properties", "Color Filter before blue", params);
    blueColorFilter.setValue(new SimpleColorFilter(Color.BLUE));
    recordSnapshot(animationView, 1080, "android", "Dynamic Properties", "Color Filter after blue", params);

    testDynamicProperty(
        "Null Color Filter",
        new KeyPath("**"),
        LottieProperty.COLOR_FILTER,
        new LottieValueCallback<ColorFilter>(null));

    testDynamicProperty(
        "Opacity interpolation (0)",
        new KeyPath("Shape Layer 1", "Rectangle"),
        LottieProperty.TRANSFORM_OPACITY,
        new LottieInterpolatedIntegerValue(10, 100),
        0f);

    testDynamicProperty(
        "Opacity interpolation (0.5)",
        new KeyPath("Shape Layer 1", "Rectangle"),
        LottieProperty.TRANSFORM_OPACITY,
        new LottieInterpolatedIntegerValue(10, 100),
        0.5f);

    testDynamicProperty(
        "Opacity interpolation (1)",
        new KeyPath("Shape Layer 1", "Rectangle"),
        LottieProperty.TRANSFORM_OPACITY,
        new LottieInterpolatedIntegerValue(10, 100),
        1f);
  }

  private <T> void testDynamicProperty(
      String name, KeyPath keyPath, T property, LottieValueCallback<T> callback) {
    testDynamicProperty(name, keyPath, property, callback, 0f);
  }

  private <T> void testDynamicProperty(
      String name, KeyPath keyPath, T property, LottieValueCallback<T> callback, float progress) {
    LottieAnimationView animationView = new LottieAnimationView(context);
    LottieComposition composition =
        LottieComposition.Factory.fromFileSync(context, "Tests/Shapes.json");
    animationView.setComposition(composition);
    animationView.setProgress(progress);
    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    animationView.addValueCallback(keyPath, property, callback);
    recordSnapshot(animationView, 1080, "android", "Dynamic Properties", name, params);
  }

  private void testSwitchingToDrawableAndBack() {
    LottieComposition composition = LottieComposition.Factory.fromFileSync(context, "Tests/Shapes.json");
    LottieAnimationView view = new LottieAnimationView(context);
    view.setComposition(composition);
    view.setImageDrawable(new ColorDrawable(Color.RED));
    view.setComposition(composition);
    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    recordSnapshot(view, 1080, "android", "Reset Animation", "Drawable and back", params);
  }

  private void testStartEndFrameWithStartEndProgress() {
    LottieComposition composition = LottieComposition.Factory.fromFileSync(context, "Tests/StartEndFrame.json");
    LottieAnimationView view = new LottieAnimationView(context);
    view.setComposition(composition);
    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    view.setMinProgress(0f);
    view.setProgress(0f);
    recordSnapshot(view, 1080, "android", "MinMaxFrame", "minProgress 0", params);
    view.setMinProgress(0.25f);
    view.setProgress(0f);
    recordSnapshot(view, 1080, "android", "MinMaxFrame", "minProgress 0.25", params);
    view.setMinProgress(0.75f);
    view.setProgress(0f);
    recordSnapshot(view, 1080, "android", "MinMaxFrame", "minProgress 0.75", params);
    view.setMinProgress(1f);
    view.setProgress(0f);
    recordSnapshot(view, 1080, "android", "MinMaxFrame", "minProgress 1", params);

    view.setMaxProgress(0f);
    view.setProgress(1f);
    recordSnapshot(view, 1080, "android", "MinMaxFrame", "maxProgress 0", params);
    view.setMaxProgress(0.25f);
    view.setProgress(1f);
    recordSnapshot(view, 1080, "android", "MinMaxFrame", "maxProgress 0.25", params);
    view.setMaxProgress(0.75f);
    view.setProgress(1f);
    recordSnapshot(view, 1080, "android", "MinMaxFrame", "maxProgress 0.75", params);
    view.setMaxProgress(1f);
    view.setProgress(1f);
    recordSnapshot(view, 1080, "android", "MinMaxFrame", "maxProgress 1", params);

    composition = LottieComposition.Factory.fromFileSync(context, "Tests/EndFrame.json");
    view = new LottieAnimationView(context);
    view.setComposition(composition);
    view.setFrame(29);
    recordSnapshot(view, 1080, "android", "EndFrame", "End Frame (red)", params);
    view.setFrame(30);
    recordSnapshot(view, 1080, "android", "EndFrame", "End Frame (blue)", params);

  }

  private void testUrl() {
    LottieComposition composition = LottieCompositionFactory.fromUrlSync(context, "https://www.lottiefiles.com/download/427").getValue();
    drawComposition(composition, "GiftBox from LottieFiles URL (427)");
  }

  private int dpToPx(int dp) {
    Resources resources = context.getResources();
    return (int) TypedValue.applyDimension(1, (float) dp, resources.getDisplayMetrics());
  }
}
