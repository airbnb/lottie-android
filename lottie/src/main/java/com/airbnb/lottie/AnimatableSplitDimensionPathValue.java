package com.airbnb.lottie;

import android.graphics.PointF;

class AnimatableSplitDimensionPathValue implements AnimatableValue<PointF> {
  private final AnimatableFloatValue animatableXDimension;
  private final AnimatableFloatValue animatableYDimension;

  AnimatableSplitDimensionPathValue(
      AnimatableFloatValue animatableXDimension,
      AnimatableFloatValue animatableYDimension) {
    this.animatableXDimension = animatableXDimension;
    this.animatableYDimension = animatableYDimension;
  }

  @Override public KeyframeAnimation<PointF> createAnimation() {
    return new SplitDimensionPathKeyframeAnimation(
        animatableXDimension.createAnimation(), animatableYDimension.createAnimation());
  }

  @Override public boolean hasAnimation() {
    return animatableXDimension.hasAnimation() || animatableYDimension.hasAnimation();
  }

}
