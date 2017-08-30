package com.airbnb.lottie.model.animatable;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.ColorKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.StaticKeyframeAnimation;
import com.airbnb.lottie.model.ColorFactory;

import org.json.JSONObject;

import java.util.List;

public class AnimatableColorValue extends BaseAnimatableValue<Integer, Integer> {
  private AnimatableColorValue(List<Keyframe<Integer>> keyframes, Integer initialValue) {
    super(keyframes, initialValue);
  }

  @Override public BaseKeyframeAnimation<Integer, Integer> createAnimation() {
    if (!hasAnimation()) {
      return new StaticKeyframeAnimation<>(initialValue);
    }
    return new ColorKeyframeAnimation(keyframes);
  }

  @Override public String toString() {
    return "AnimatableColorValue{" + "initialValue=" + initialValue + '}';
  }

  public static final class Factory {
    private Factory() {
    }

    public static AnimatableColorValue newInstance(JSONObject json, LottieComposition composition) {
      AnimatableValueParser.Result<Integer> result = AnimatableValueParser
          .newInstance(json, 1f, composition, ColorFactory.INSTANCE)
          .parseJson();
      return new AnimatableColorValue(result.keyframes, result.initialValue);
    }
  }
}
