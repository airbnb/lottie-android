package com.airbnb.lottie.model.animatable;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.FloatKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.StaticKeyframeAnimation;
import com.airbnb.lottie.utils.JsonUtils;

import org.json.JSONObject;

import java.util.List;

public class AnimatableFloatValue extends BaseAnimatableValue<Float, Float> {
  private AnimatableFloatValue() {
    super(0f);
  }

  private AnimatableFloatValue(List<Keyframe<Float>> keyframes, Float initialValue) {
    super(keyframes, initialValue);
  }

  @Override public BaseKeyframeAnimation<Float, Float> createAnimation() {
    if (!hasAnimation()) {
      return new StaticKeyframeAnimation<>(initialValue);
    }

    return new FloatKeyframeAnimation(keyframes);
  }

  @Override
  public Float getInitialValue() {
    return initialValue;
  }

  private static class ValueFactory implements AnimatableValue.Factory<Float> {
    static final ValueFactory INSTANCE = new ValueFactory();

    private ValueFactory() {
    }

    @Override public Float valueFromObject(Object object, float scale) {
      return JsonUtils.valueFromObject(object) * scale;
    }
  }

  public static final class Factory {
    private Factory() {
    }

    static AnimatableFloatValue newInstance() {
      return new AnimatableFloatValue();
    }

    public static AnimatableFloatValue newInstance(JSONObject json, LottieComposition composition) {
      return newInstance(json, composition, true);
    }

    public static AnimatableFloatValue newInstance(JSONObject json, LottieComposition composition,
        boolean isDp) {
      float scale = isDp ? composition.getDpScale() : 1f;
      if (json != null && json.has("x")) {
        composition.addWarning("Lottie doesn't support expressions.");
      }
      AnimatableValueParser.Result<Float> result = AnimatableValueParser
          .newInstance(json, scale, composition, ValueFactory.INSTANCE)
          .parseJson();
      return new AnimatableFloatValue(result.keyframes, result.initialValue);
    }
  }
}
