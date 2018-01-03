package com.airbnb.lottie.model.animatable;

import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.IntegerKeyframeAnimation;
import com.airbnb.lottie.utils.JsonUtils;

import java.io.IOException;
import java.util.List;

public class AnimatableIntegerValue extends BaseAnimatableValue<Integer, Integer> {

  private AnimatableIntegerValue() {
    this(100);
  }

  AnimatableIntegerValue(Integer value) {
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
        JsonReader reader, LottieComposition composition) throws IOException {
      // TODO (json)
      // if (reader != null && reader.has("x")) {
      //   composition.addWarning("Lottie doesn't support expressions.");
      // }
      return new AnimatableIntegerValue(
          AnimatableValueParser.newInstance(reader, 1, composition, ValueFactory.INSTANCE)
      );
    }
  }

  private static class ValueFactory implements AnimatableValue.Factory<Integer> {
    private static final ValueFactory INSTANCE = new ValueFactory();

    private ValueFactory() {
    }

    @Override public Integer valueFromObject(JsonReader reader, float scale) throws IOException {
      return Math.round(JsonUtils.valueFromObject(reader) * scale);
    }
  }
}
