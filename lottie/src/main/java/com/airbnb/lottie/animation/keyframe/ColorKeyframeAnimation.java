package com.airbnb.lottie.animation.keyframe;

import com.airbnb.lottie.L;
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
    L.beginSection("ColorKeyframeAnimation#getIntValue");
    if (keyframe.startValue == null || keyframe.endValue == null) {
      throw new IllegalStateException("Missing values for keyframe.");
    }

    if (valueCallback != null) {
      //noinspection ConstantConditions
      Integer value = valueCallback.getValueInternal(keyframe.startFrame, keyframe.endFrame, keyframe.startValue,
          keyframe.endValue, keyframeProgress, getLinearCurrentKeyframeProgress(), getProgress());
      if (value != null) {
        L.endSection("ColorKeyframeAnimation#getIntValue");
        return value;
      }
    }

    int value = GammaEvaluator.evaluate(MiscUtils.clamp(keyframeProgress, 0f, 1f), keyframe.startValue, keyframe.endValue);
    L.endSection("ColorKeyframeAnimation#getIntValue");
    return value;
  }

  /**
   * Optimization to avoid autoboxing.
   */
  public int getIntValue() {
    L.beginSection("#ColorKeyframeAnimation#getIntValue");
    Keyframe<Integer> currentKeyframe = getCurrentKeyframe();
    L.endSection("#ColorKeyframeAnimation#getIntValue");
    return getIntValue(currentKeyframe, getInterpolatedCurrentKeyframeProgress());
  }
}
