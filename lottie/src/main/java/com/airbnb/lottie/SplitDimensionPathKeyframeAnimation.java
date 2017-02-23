package com.airbnb.lottie;

import java.util.Collections;

class SplitDimensionPathKeyframeAnimation extends KeyframeAnimation<CPointF> {
  private final CPointF point = new CPointF();
  private final KeyframeAnimation<Float> xAnimation;
  private final KeyframeAnimation<Float> yAnimation;

  SplitDimensionPathKeyframeAnimation(
      KeyframeAnimation<Float> xAnimation, KeyframeAnimation<Float> yAnimation) {
    super(Collections.<Keyframe<CPointF>>emptyList());

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

  @Override public CPointF getValue() {
    return getValue(null, 0);
  }

  @Override CPointF getValue(Keyframe<CPointF> keyframe, float keyframeProgress) {
    return point;
  }
}
