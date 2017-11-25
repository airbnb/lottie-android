package com.airbnb.lottie.model.animatable;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.ScaleKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.StaticKeyframeAnimation;
import com.airbnb.lottie.value.ScaleXY;

import org.json.JSONObject;

import java.util.List;

public class AnimatableScaleValue extends BaseAnimatableValue<ScaleXY, ScaleXY> {
  private AnimatableScaleValue() {
    super(new ScaleXY());
  }

  AnimatableScaleValue(List<Keyframe<ScaleXY>> keyframes, ScaleXY initialValue) {
    super(keyframes, initialValue);
  }

  @Override public BaseKeyframeAnimation<ScaleXY, ScaleXY> createAnimation() {
    if (!hasAnimation()) {
      return new StaticKeyframeAnimation<>(initialValue);
    } else {
      return new ScaleKeyframeAnimation(keyframes);
    }
  }

  static final class Factory {
    private Factory() {
    }

    static AnimatableScaleValue newInstance(JSONObject json, LottieComposition
        composition) {
      AnimatableValueParser.Result<ScaleXY> result = AnimatableValueParser
          .newInstance(json, 1, composition, ScaleXY.Factory.INSTANCE)
          .parseJson();
      return new AnimatableScaleValue(result.keyframes, result.initialValue);
    }

    static AnimatableScaleValue newInstance() {
      return new AnimatableScaleValue();
    }
  }
}
