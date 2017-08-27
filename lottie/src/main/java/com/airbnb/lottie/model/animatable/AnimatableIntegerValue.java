package com.airbnb.lottie.model.animatable;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.animation.keyframe.IntegerKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.KeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.StaticKeyframeAnimation;
import com.airbnb.lottie.utils.JsonUtils;

import org.json.JSONObject;

import java.util.List;

public class AnimatableIntegerValue extends BaseAnimatableValue<Integer, Integer> {
  private AnimatableIntegerValue() {
    super(100);
  }

  AnimatableIntegerValue(List<Keyframe<Integer>> keyframes, Integer initialValue) {
    super(keyframes, initialValue);
  }

  @Override public KeyframeAnimation<Integer> createAnimation() {
    if (!hasAnimation()) {
      return new StaticKeyframeAnimation<>(initialValue);
    }

    return new IntegerKeyframeAnimation(keyframes);
  }

  @Override
  public Integer getInitialValue() {
    return initialValue;
  }

  public static final class Factory {
    private Factory() {
    }

    static AnimatableIntegerValue newInstance() {
      return new AnimatableIntegerValue();
    }

    public static AnimatableIntegerValue newInstance(
        JSONObject json, LottieComposition composition) {
      if (json != null && json.has("x")) {
        composition.addWarning("Lottie doesn't support expressions.");
      }
      AnimatableValueParser.Result<Integer> result = AnimatableValueParser
          .newInstance(json, 1, composition, ValueFactory.INSTANCE)
          .parseJson();
      Integer initialValue = result.initialValue;
      return new AnimatableIntegerValue(result.keyframes, initialValue);
    }
  }

  private static class ValueFactory implements AnimatableValue.Factory<Integer> {
    private static final ValueFactory INSTANCE = new ValueFactory();

    private ValueFactory() {
    }

    @Override public Integer valueFromObject(Object object, float scale) {
      return Math.round(JsonUtils.valueFromObject(object) * scale);
    }
  }
}
