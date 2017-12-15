package com.airbnb.lottie.model.animatable;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.IntegerKeyframeAnimation;
import com.airbnb.lottie.utils.JsonUtils;

import org.json.JSONObject;

import java.util.List;

public class AnimatableIntegerValue extends BaseAnimatableValue<Integer, Integer> {

  private AnimatableIntegerValue() {
    this(100);
  }

  private AnimatableIntegerValue(Integer value) {
    super(value);
  }

  AnimatableIntegerValue(List<Keyframe<Integer>> keyframes) {
    super(keyframes);
  }

  @Override public BaseKeyframeAnimation<Integer, Integer> createAnimation() {
    return new IntegerKeyframeAnimation(keyframes);
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
      return new AnimatableIntegerValue(
          AnimatableValueParser.newInstance(json, 1, composition, ValueFactory.INSTANCE)
      );
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
