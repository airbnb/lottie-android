package com.airbnb.lottie.animation.keyframe;

import com.airbnb.lottie.utils.GammaEvaluator;
import com.airbnb.lottie.utils.MiscUtils;
import com.airbnb.lottie.value.Keyframe;

import java.util.List;

public class ColorKeyframeAnimation extends KeyframeAnimation<Integer> {

  public ColorKeyframeAnimation(List<Keyframe<Integer>> keyframes) {
    super(keyframes);
  }

  @Override
  Integer getValue(Keyframe<Integer> keyframe, float keyframeProgress) {
    return getIntValue(keyframe, keyframeProgress);
  }

  /**
   * Optimization to avoid autoboxing.
   */
  public int getIntValue(Keyframe<Integer> keyframe, float keyframeProgress) {
    if (keyframe.startValue == null || keyframe.endValue == null) {
      throw new IllegalStateException("Missing values for keyframe.");
    }

    // keyframe.endFrame should not be null under normal operation.
    // It is not clear why this would be null and when it does, it seems to be extremely rare.
    // https://github.com/airbnb/lottie-android/issues/2361
    if (valueCallback != null && keyframe.endFrame != null) {
      //noinspection ConstantConditions
      Integer value = valueCallback.getValueInternal(keyframe.startFrame, keyframe.endFrame, keyframe.startValue,
          keyframe.endValue, keyframeProgress, getLinearCurrentKeyframeProgress(), getProgress());
      if (value != null) {
        return value;
      }
    }

    return GammaEvaluator.evaluate(MiscUtils.clamp(keyframeProgress, 0f, 1f), keyframe.startValue, keyframe.endValue);
  }

  /**
   * Optimization to avoid autoboxing.
   */
  public int getIntValue() {
    return getIntValue(getCurrentKeyframe(), getInterpolatedCurrentKeyframeProgress());
  }
}
