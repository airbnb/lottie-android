package com.airbnb.lottie;

import java.util.List;

import static com.airbnb.lottie.MiscUtils.lerp;

class IntegerKeyframeAnimation extends KeyframeAnimation<Integer> {

  IntegerKeyframeAnimation(List<Keyframe<Integer>> keyframes) {
    super(keyframes);
  }

  @Override Integer getValue(Keyframe<Integer> keyframe, float keyframeProgress) {
    if (keyframe.startValue == null || keyframe.endValue == null) {
      throw new IllegalStateException("Missing values for keyframe.");
    }
    return lerp(keyframe.startValue, keyframe.endValue, keyframeProgress);
  }
}
