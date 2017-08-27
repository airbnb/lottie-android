package com.airbnb.lottie.animation.keyframe;

import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.utils.MiscUtils;

import java.util.List;

public class FloatKeyframeAnimation extends KeyframeAnimation<Float> {

  public FloatKeyframeAnimation(List<Keyframe<Float>> keyframes) {
    super(keyframes);
  }

  @Override Float getValue(Keyframe<Float> keyframe, float keyframeProgress) {
    if (keyframe.startValue == null || keyframe.endValue == null) {
      throw new IllegalStateException("Missing values for keyframe.");
    }
    return MiscUtils.lerp(keyframe.startValue, keyframe.endValue, keyframeProgress);
  }
}
