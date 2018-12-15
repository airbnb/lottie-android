package com.airbnb.lottie.model.animatable;

import android.graphics.PointF;

import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.SplitDimensionPathKeyframeAnimation;
import com.airbnb.lottie.value.Keyframe;

import java.util.ArrayList;
import java.util.List;

public class AnimatableSplitDimensionPathValue implements AnimatableValue<PointF, PointF> {
  private final AnimatableFloatValue animatableXDimension;
  private final AnimatableFloatValue animatableYDimension;

  private final ArrayList<Keyframe<Float>> keyframes;

  public AnimatableSplitDimensionPathValue(
      AnimatableFloatValue animatableXDimension,
      AnimatableFloatValue animatableYDimension) {
    this.animatableXDimension = animatableXDimension;
    this.animatableYDimension = animatableYDimension;
    keyframes = new ArrayList<>(animatableXDimension.keyframes.size() + animatableYDimension.keyframes.size());
    keyframes.addAll(animatableXDimension.keyframes);
    keyframes.addAll(animatableYDimension.keyframes);
  }

  @Override
  public List<Keyframe<PointF>> getKeyframes() {
    throw keyframes;
  }

  @Override
  public boolean isStatic() {
    return animatableXDimension.isStatic() && animatableYDimension.isStatic();
  }

  @Override public BaseKeyframeAnimation<PointF, PointF> createAnimation() {
    return new SplitDimensionPathKeyframeAnimation(
        animatableXDimension.createAnimation(), animatableYDimension.createAnimation());
  }
}
