package com.airbnb.lottie;

import java.util.List;

import static com.airbnb.lottie.MiscUtils.lerp;

class NumberKeyframeAnimation<T extends Number> extends KeyframeAnimation<T> {
  private final Class<T> klass;

  NumberKeyframeAnimation(List<Keyframe<T>> keyframes, Class<T> klass) {
    super(keyframes);
    this.klass = klass;
  }

  @Override T getValue(Keyframe<T> keyframe, float keyframeProgress) {
    Number startValue = keyframe.startValue;
    Number endValue = keyframe.endValue;

    if (klass.isAssignableFrom(Integer.class)) {
      return klass.cast(lerp(startValue.intValue(), endValue.intValue(), keyframeProgress));
    } else {
      return klass.cast(lerp(startValue.floatValue(), endValue.floatValue(), keyframeProgress));
    }
  }
}
