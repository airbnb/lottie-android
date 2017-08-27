package com.airbnb.lottie.animation.keyframe;

import android.graphics.PointF;

import com.airbnb.lottie.animation.Keyframe;

import java.util.Collections;

public class SplitDimensionPathKeyframeAnimation extends KeyframeAnimation<PointF> {
  private final PointF point = new PointF();
  private final KeyframeAnimation<Float> xAnimation;
  private final KeyframeAnimation<Float> yAnimation;

  public SplitDimensionPathKeyframeAnimation(
      KeyframeAnimation<Float> xAnimation, KeyframeAnimation<Float> yAnimation) {
    super(Collections.<Keyframe<PointF>>emptyList());

    this.xAnimation = xAnimation;
    this.yAnimation = yAnimation;
  }

  @Override public void setProgress(float progress) {
    xAnimation.setProgress(progress);
    yAnimation.setProgress(progress);
    point.set(xAnimation.getValue(), yAnimation.getValue());
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).onValueChanged();
    }
  }

  @Override public PointF getValue() {
    return getValue(null, 0);
  }

  @Override PointF getValue(Keyframe<PointF> keyframe, float keyframeProgress) {
    return point;
  }
}
