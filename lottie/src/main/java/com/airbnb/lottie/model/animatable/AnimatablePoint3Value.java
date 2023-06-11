package com.airbnb.lottie.model.animatable;

import com.airbnb.lottie.animation.Point3F;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.Point3KeyframeAnimation;
import com.airbnb.lottie.value.Keyframe;

import java.util.List;

public class AnimatablePoint3Value extends BaseAnimatableValue<Point3F, Point3F> {
  public AnimatablePoint3Value(List<Keyframe<Point3F>> keyframes) {
    super(keyframes);
  }

  @Override public BaseKeyframeAnimation<Point3F, Point3F> createAnimation() {
    return new Point3KeyframeAnimation(keyframes);
  }
}
