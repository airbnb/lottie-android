package com.airbnb.lottie.animation.keyframe;

import com.airbnb.lottie.utils.MiscUtils;
import com.airbnb.lottie.value.Keyframe;

import java.util.List;

public class IntegerKeyframeAnimation extends KeyframeAnimation<Integer> {

  public IntegerKeyframeAnimation(List<Keyframe<Integer>> keyframes) {
    super(keyframes);
  }

  @Override
  Integer getValue(Keyframe<Integer> keyframe, float keyframeProgress) {
    return getIntValue(keyframe, keyframeProgress);
  }

  /**
   * Optimization to avoid autoboxing.
   */
  int getIntValue(Keyframe<Integer> keyframe, float keyframeProgress) {
    if (keyframe.startValue == null) {
      throw new IllegalStateException("Missing values for keyframe.");
    }

    int endValue = keyframe.endValue == null ? keyframe.getStartValueInt() : keyframe.getEndValueInt();

    if (valueCallback != null) {
      //noinspection ConstantConditions
      Integer value = valueCallback.getValueInternal(keyframe.startFrame, keyframe.endFrame,
          keyframe.startValue, endValue,
          keyframeProgress, getLinearCurrentKeyframeProgress(), getProgress());
      if (value != null) {
        return value;
      }
    }

    return MiscUtils.lerp(keyframe.getStartValueInt(), endValue, keyframeProgress);
  }

  /**
   * Optimization to avoid autoboxing.
   */
  public int getIntValue() {
    return getIntValue(getCurrentKeyframe(), getInterpolatedCurrentKeyframeProgress());
  }
}
