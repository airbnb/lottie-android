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
    int startColor = keyframe.startValue;
    int endColor = keyframe.endValue;

    if (valueCallback != null) {
      //noinspection ConstantConditions
      Integer value = valueCallback.getValueInternal(keyframe.startFrame, keyframe.endFrame, startColor,
          endColor, keyframeProgress, getLinearCurrentKeyframeProgress(), getProgress());
      if (value != null) {
        return value;
      }
    }

    return GammaEvaluator.evaluate(MiscUtils.clamp(keyframeProgress, 0f, 1f), startColor, endColor);
  }

  /**
   * Optimization to avoid autoboxing.
   */
  public int getIntValue() {
    return getIntValue(getCurrentKeyframe(), getInterpolatedCurrentKeyframeProgress());
  }
}
