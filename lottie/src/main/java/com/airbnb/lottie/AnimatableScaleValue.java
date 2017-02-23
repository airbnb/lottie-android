package com.airbnb.lottie;

import org.json.JSONObject;

import java.util.List;

class AnimatableScaleValue extends BaseAnimatableValue<ScaleXY, ScaleXY> {
  private AnimatableScaleValue(LottieComposition composition) {
    super(composition);
    initialValue = new ScaleXY();
  }

  private AnimatableScaleValue(List<Keyframe<ScaleXY>> keyframes, LottieComposition composition,
      ScaleXY initialValue) {
    super(keyframes, composition, initialValue);
  }

  @Override public KeyframeAnimation<ScaleXY> createAnimation() {
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
        composition, boolean isDp) {
      float scale = isDp ? composition.getScale() : 1f;
      AnimatableValueParser.Result<ScaleXY> result = AnimatableValueParser
          .newInstance(json, scale, composition, ScaleXY.Factory.INSTANCE)
          .parseJson();
      return new AnimatableScaleValue(result.keyframes, composition, result.initialValue);
    }

    static AnimatableScaleValue newInstance(LottieComposition composition) {
      return new AnimatableScaleValue(composition);
    }
  }
}
