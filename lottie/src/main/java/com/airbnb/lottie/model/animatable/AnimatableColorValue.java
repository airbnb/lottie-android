package com.airbnb.lottie.model.animatable;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.ColorKeyframeAnimation;
import com.airbnb.lottie.model.ColorFactory;

import org.json.JSONObject;

import java.util.List;

public class AnimatableColorValue extends BaseAnimatableValue<Integer, Integer> {
  private AnimatableColorValue(List<Keyframe<Integer>> keyframes) {
    super(keyframes);
  }

  @Override public BaseKeyframeAnimation<Integer, Integer> createAnimation() {
    return new ColorKeyframeAnimation(keyframes);
  }

  public static final class Factory {
    private Factory() {
    }

    public static AnimatableColorValue newInstance(JSONObject json, LottieComposition composition) {
      return new AnimatableColorValue(
          AnimatableValueParser.newInstance(json, 1f, composition, ColorFactory.INSTANCE));
    }
  }
}
