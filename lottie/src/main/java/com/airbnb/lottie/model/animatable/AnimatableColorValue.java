package com.airbnb.lottie.model.animatable;

import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.ColorKeyframeAnimation;
import com.airbnb.lottie.model.ColorFactory;

import java.io.IOException;
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

    public static AnimatableColorValue newInstance(
        JsonReader reader, LottieComposition composition) throws IOException {
      return new AnimatableColorValue(
          AnimatableValueParser.newInstance(reader, 1f, composition, ColorFactory.INSTANCE));
    }
  }
}
