package com.airbnb.lottie.animation;

import android.support.annotation.RestrictTo;
import android.view.animation.Interpolator;

import com.airbnb.lottie.model.LottieComposition;

import java.util.List;

import static com.airbnb.lottie.utils.MiscUtils.lerp;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class NumberKeyframeAnimation<T extends Number> extends KeyframeAnimation<T> {

  private final List<T> values;
  private final Class<T> klass;

  public NumberKeyframeAnimation(long duration, LottieComposition composition, List<Float> keyTimes, Class<T> klass, List<T> values, List<Interpolator> interpolators) {
    super(duration, composition, keyTimes, interpolators);
    this.klass = klass;
    if (keyTimes.size() != values.size()) {
      throw new IllegalArgumentException("Key times and values must be the same length " + keyTimes + " vs " + values);
    }
    this.values = values;
  }

  @Override
  public T getValue() {
    if (progress <= 0f) {
      return values.get(0);
    } else if (progress >= 1f) {
      return values.get(values.size() - 1);
    }

    int keyframeIndex = getKeyframeIndex();

    float startKeytime = keyTimes.get(keyframeIndex);
    float endKeytime = keyTimes.get(keyframeIndex + 1);

    float percentageIntoFrame = 0;
    if (!isDiscrete) {
      percentageIntoFrame = (progress - startKeytime) / (endKeytime - startKeytime);
      if (interpolators != null) {
        percentageIntoFrame = interpolators.get(keyframeIndex).getInterpolation(percentageIntoFrame);
      }
    }

    Number startValue = values.get(keyframeIndex);
    Number endValue = values.get(keyframeIndex + 1);

    if (klass.isAssignableFrom(Integer.class)) {
      return klass.cast(lerp(startValue.intValue(), endValue.intValue(), percentageIntoFrame));
    } else {
      return klass.cast(lerp(startValue.floatValue(), endValue.floatValue(), percentageIntoFrame));
    }
  }
}
