package com.airbnb.lottie.model.animatable;

import com.airbnb.lottie.value.Keyframe;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.ColorKeyframeAnimation;

import java.util.List;

public class AnimatableColorValue extends BaseAnimatableValue<Integer, Integer> {
  public AnimatableColorValue(List<Keyframe<Integer>> keyframes) {
    super(keyframes);
  }

  @Override public BaseKeyframeAnimation<Integer, Integer> createAnimation() {
    return new ColorKeyframeAnimation(keyframes);
  }
}
