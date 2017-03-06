package com.airbnb.lottie;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AnimationLinearLayout extends LinearLayout {
  private static final float[] DEFAULT_ANIMATED_PROGRESS = {0f, 0.05f, 0.10f, 0.2f, 0.3f, 0.4f,
      0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 0.95f, 1f};
  private static final int DESIRED_WIDTH = 500;

  public AnimationLinearLayout(Context context) {
    super(context);
    init();
  }

  private void init() {
    setOrientation(VERTICAL);
    setBackgroundColor(Color.WHITE);
    for (int i = 0; i < DEFAULT_ANIMATED_PROGRESS.length; i++) {
      addViewsFor(DEFAULT_ANIMATED_PROGRESS[i]);
    }
  }

  private void addViewsFor(float progress) {
    TextView textView = new TextView(getContext());
    textView.setText("" + progress);
    textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
    textView.setPadding(0, 24, 0, 24);
    LinearLayout.LayoutParams lp = generateDefaultLayoutParams();
    lp.gravity = Gravity.CENTER_HORIZONTAL;
    addView(textView);

    LottieAnimationView animationView = new LottieAnimationView(getContext());
    animationView.setProgress(progress);
    addView(animationView);
  }

  void setImageAssetsFolder(String folder) {
    for (int i = getChildCount() - 1; i >= 0; i--) {
      View child = getChildAt(i);
      if (!(child instanceof LottieAnimationView)) {
        continue;
      }
      ((LottieAnimationView) child).setImageAssetsFolder(folder);
    }
  }

  void setComposition(LottieComposition composition) {
    for (int i = getChildCount() - 1; i >= 0; i--) {
      View child = getChildAt(i);
      if (!(child instanceof LottieAnimationView)) {
        continue;
      }
      ((LottieAnimationView) child).setComposition(composition);
      float scale = DESIRED_WIDTH / (float) composition.getBounds().width();
      ((LottieAnimationView) child).setScale(scale);
    }
  }

  void recycleBitmaps() {
    for (int i = getChildCount() - 1; i >= 0; i--) {
      View child = getChildAt(i);
      if (!(child instanceof LottieAnimationView)) {
        continue;
      }
      ((LottieAnimationView) child).recycleBitmaps();
    }
  }
}
