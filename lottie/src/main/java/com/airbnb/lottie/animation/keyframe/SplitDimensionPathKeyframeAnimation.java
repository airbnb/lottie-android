package com.airbnb.lottie.animation.keyframe;

import android.graphics.PointF;

import com.airbnb.lottie.value.Keyframe;

import java.util.Collections;

public class SplitDimensionPathKeyframeAnimation extends BaseKeyframeAnimation<PointF, PointF> {
  private final PointF point = new PointF();
  private final BaseKeyframeAnimation<Float, Float> xAnimation;
  private final BaseKeyframeAnimation<Float, Float> yAnimation;

  public SplitDimensionPathKeyframeAnimation(
      BaseKeyframeAnimation<Float, Float> xAnimation,
      BaseKeyframeAnimation<Float, Float> yAnimation) {
    super(Collections.<Keyframe<PointF>>emptyList());

    this.xAnimation = xAnimation;
    this.yAnimation = yAnimation;
    // We need to call an initial setProgress so point gets set with the initial value.
    setProgress(getProgress());
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
