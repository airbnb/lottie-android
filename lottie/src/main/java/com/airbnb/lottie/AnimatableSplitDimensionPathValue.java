package com.airbnb.lottie;

import android.graphics.PointF;

import org.json.JSONException;
import org.json.JSONObject;

class AnimatableSplitDimensionPathValue implements IAnimatablePathValue {
  private final PointF point = new PointF();

  private final AnimatableFloatValue animatableXDimension;
  private final AnimatableFloatValue animatableYDimension;

  AnimatableSplitDimensionPathValue(JSONObject pointValue, LottieComposition composition) {
    try {
      animatableXDimension = new AnimatableFloatValue(pointValue.getJSONObject("x"),
          composition.getFrameRate(), composition);
      animatableYDimension = new AnimatableFloatValue(pointValue.getJSONObject("y"),
          composition.getFrameRate(), composition);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Unable to parse split dimension path.");
    }
  }

  @Override public KeyframeAnimation<PointF> createAnimation() {
    return new SplitDimensionPathKeyframeAnimation(
        animatableXDimension.createAnimation(), animatableYDimension.createAnimation());
  }

  @Override public boolean hasAnimation() {
    return animatableXDimension.hasAnimation() || animatableYDimension.hasAnimation();
  }

  @Override public PointF getInitialPoint() {
    point.set(animatableXDimension.getInitialValue(), animatableYDimension.getInitialValue());
    return point;
  }
}
