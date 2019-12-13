package com.airbnb.lottie.animation.keyframe;

import com.airbnb.lottie.value.Keyframe;
import com.airbnb.lottie.utils.MiscUtils;

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
    if (keyframe.startValue == null || keyframe.endValue == null) {
      throw new IllegalStateException("Missing values for keyframe.");
    }

    if (valueCallback != null) {
      //noinspection ConstantConditions
      Integer value = valueCallback.getValueInternal(keyframe.startFrame, keyframe.endFrame,
              keyframe.startValue, keyframe.endValue,
              keyframeProgress, getLinearCurrentKeyframeProgress(), getProgress());
      if (value != null) {
        return value;
      }
    }

    int value = MiscUtils.lerp(keyframe.getStartValueInt(), keyframe.getEndValueInt(), keyframeProgress);
    cachedValue = value;
    return value;
  }

  /**
   * Optimization to avoid autoboxing.
   */
  private int cachedValue = 0;
  public int getIntValue() {
    float interpolatedProgress = getInterpolatedCurrentKeyframeProgress();
    if (valueCallback == null && keyframesWrapper.isCachedValueEnabled(interpolatedProgress)) {
      return cachedValue;
    }
    return getIntValue(getCurrentKeyframe(), interpolatedProgress);
  }
}
