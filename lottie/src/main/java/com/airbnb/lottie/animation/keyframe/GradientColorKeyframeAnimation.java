package com.airbnb.lottie.animation.keyframe;

import com.airbnb.lottie.model.content.GradientColor;
import com.airbnb.lottie.value.Keyframe;

import java.util.List;

public class GradientColorKeyframeAnimation extends KeyframeAnimation<GradientColor> {
  private final GradientColor gradientColor;

  public GradientColorKeyframeAnimation(List<Keyframe<GradientColor>> keyframes) {
    super(keyframes);
    // Not all keyframes that this GradientColor are used for will have the same length.
    // AnimatableGradientColorValue.ensureInterpolatableKeyframes may add extra positions
    // for some keyframes but not others to ensure that it is interpolatable.
    // Ensure that there is enough space for the largest keyframe.
    int size = 0;
    for (int i = 0; i < keyframes.size(); i++) {
      GradientColor startValue = keyframes.get(i).startValue;
      if (startValue != null) {
        size = Math.max(size, startValue.getSize());
      }
    }
    gradientColor = new GradientColor(new float[size], new int[size]);
  }

  @Override GradientColor getValue(Keyframe<GradientColor> keyframe, float keyframeProgress) {
    //noinspection DataFlowIssue
    gradientColor.lerp(keyframe.startValue, keyframe.endValue, keyframeProgress);
    return gradientColor;
  }
}
