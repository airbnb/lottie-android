package com.airbnb.lottie.model.animatable;

import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.FloatKeyframeAnimation;
import com.airbnb.lottie.utils.JsonUtils;
import com.airbnb.lottie.utils.Utils;

import java.io.IOException;
import java.util.List;

public class AnimatableFloatValue extends BaseAnimatableValue<Float, Float> {

  private AnimatableFloatValue() {
    this(0f);
  }

  private AnimatableFloatValue(Float value) {
    super(value);
  }

  private AnimatableFloatValue(List<Keyframe<Float>> keyframes) {
    super(keyframes);
  }

  @Override public BaseKeyframeAnimation<Float, Float> createAnimation() {
    return new FloatKeyframeAnimation(keyframes);
  }

  private static class ValueFactory implements AnimatableValue.Factory<Float> {
    static final ValueFactory INSTANCE = new ValueFactory();

    private ValueFactory() {
    }

    @Override public Float valueFromObject(JsonReader reader, float scale) throws IOException {
      return JsonUtils.valueFromObject(reader) * scale;
    }
  }

  public static final class Factory {
    private Factory() {
    }

    static AnimatableFloatValue newInstance() {
      return new AnimatableFloatValue();
    }

    public static AnimatableFloatValue newInstance(JsonReader reader, LottieComposition composition)
        throws IOException {
      return newInstance(reader, composition, true);
    }

    public static AnimatableFloatValue newInstance(
        JsonReader reader, LottieComposition composition, boolean isDp) throws IOException {
      float scale = isDp ? Utils.dpScale() : 1f;
      return new AnimatableFloatValue(
          AnimatableValueParser.newInstance(reader, scale, composition, ValueFactory.INSTANCE));
    }
  }
}
