package com.airbnb.lottie;

import android.graphics.PointF;

import org.json.JSONException;

class AnimatableSplitDimensionPathValue implements IAnimatablePathValue {
  private final PointF point = new PointF();

  private final AnimatableFloatValue animatableXDimension;
  private final AnimatableFloatValue animatableYDimension;

  AnimatableSplitDimensionPathValue(
      AnimatableFloatValue animatableXDimension,
      AnimatableFloatValue animatableYDimension) {
    this.animatableXDimension = animatableXDimension;
    this.animatableYDimension = animatableYDimension;
  }

  @Override public PointF valueFromObject(Object object, float scale) throws JSONException {
    return null;
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
