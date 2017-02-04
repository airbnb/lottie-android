package com.airbnb.lottie;

import android.graphics.PointF;
import android.view.animation.Interpolator;

import java.util.Collections;

class SplitDimensionPathKeyframeAnimation extends KeyframeAnimation<PointF> {
  private final PointF point = new PointF();
  private final KeyframeAnimation<Float> xAnimation;
  private final KeyframeAnimation<Float> yAnimation;

  SplitDimensionPathKeyframeAnimation(KeyframeAnimation<Float> xAnimation,
      KeyframeAnimation<Float> yAnimation) {
    super(0, null, Collections.<Float>emptyList(),
        Collections.<Interpolator>emptyList());

    this.xAnimation = xAnimation;
    this.yAnimation = yAnimation;
  }

  @Override void setProgress(float progress) {
    xAnimation.setProgress(progress);
    yAnimation.setProgress(progress);
    point.set(xAnimation.getValue(), yAnimation.getValue());
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).onValueChanged(point);
    }
  }

  @Override PointF getValue() {
    point.set(xAnimation.getValue(), yAnimation.getValue());
    return point;
  }
}
