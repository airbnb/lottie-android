package com.airbnb.lottie;

class AnimatableSplitDimensionPathValue implements IAnimatablePathValue {
  private final CPointF point = new CPointF();
  private final AnimatableFloatValue animatableXDimension;
  private final AnimatableFloatValue animatableYDimension;

  AnimatableSplitDimensionPathValue(
      AnimatableFloatValue animatableXDimension,
      AnimatableFloatValue animatableYDimension) {
    this.animatableXDimension = animatableXDimension;
    this.animatableYDimension = animatableYDimension;
  }

  @Override public CPointF valueFromObject(Object object, float scale) {
    return null;
  }

  @Override public KeyframeAnimation<CPointF> createAnimation() {
    return new SplitDimensionPathKeyframeAnimation(
        animatableXDimension.createAnimation(), animatableYDimension.createAnimation());
  }

  @Override public boolean hasAnimation() {
    return animatableXDimension.hasAnimation() || animatableYDimension.hasAnimation();
  }

  @Override public CPointF getInitialPoint() {
    point.set(animatableXDimension.getInitialValue(), animatableYDimension.getInitialValue());
    return point;
  }
}
