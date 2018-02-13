package com.airbnb.lottie.model.animatable;

import com.airbnb.lottie.value.Keyframe;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.FloatKeyframeAnimation;

import java.util.List;

public class AnimatableFloatValue extends BaseAnimatableValue<Float, Float> {

  AnimatableFloatValue() {
    super(0f);
  }

  public AnimatableFloatValue(List<Keyframe<Float>> keyframes) {
    super(keyframes);
  }

  @Override public BaseKeyframeAnimation<Float, Float> createAnimation() {
    return new FloatKeyframeAnimation(keyframes);
  }
}
