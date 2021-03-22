package com.airbnb.lottie.model.animatable;

import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.value.Keyframe;

import java.util.List;

public interface AnimatableValue<K, A> {
  List<Keyframe<K>> getKeyframes();

  boolean isStatic();

  BaseKeyframeAnimation<K, A> createAnimation();
}
