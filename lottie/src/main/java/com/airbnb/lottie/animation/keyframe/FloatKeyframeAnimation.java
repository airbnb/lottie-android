package com.airbnb.lottie.animation.keyframe;

import com.airbnb.lottie.utils.MiscUtils;
import com.airbnb.lottie.value.Keyframe;

import java.util.List;

public class FloatKeyframeAnimation extends KeyframeAnimation<Float> {

  public FloatKeyframeAnimation(List<Keyframe<Float>> keyframes) {
    super(keyframes);
  }

  @Override Float getValue(Keyframe<Float> keyframe, float keyframeProgress) {
    return getFloatValue(keyframe, keyframeProgress);
  }

  /**
   * Optimization to avoid autoboxing.
   */
  float getFloatValue(Keyframe<Float> keyframe, float keyframeProgress) {
    if (keyframe.startValue == null || keyframe.endValue == null) {
      throw new IllegalStateException("Missing values for keyframe.");
    }

    if (valueCallback != null) {
      //noinspection ConstantConditions
      Float value = valueCallback.getValueInternal(keyframe.startFrame, keyframe.endFrame,
          keyframe.startValue, keyframe.endValue,
          keyframeProgress, getLinearCurrentKeyframeProgress(), getProgress());
      if (value != null) {
        return value;
      }
    }

    return MiscUtils.lerp(keyframe.getStartValueFloat(), keyframe.getEndValueFloat(), keyframeProgress);
  }

  /**
   * Optimization to avoid autoboxing.
   */
  public float getFloatValue() {
    return getFloatValue(getCurrentKeyframe(), getInterpolatedCurrentKeyframeProgress());
  }
}
