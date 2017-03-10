package com.airbnb.lottie;

import android.graphics.PointF;

import org.json.JSONObject;

import java.util.List;

class AnimatablePointValue extends BaseAnimatableValue<PointF, PointF> {
  private AnimatablePointValue(List<Keyframe<PointF>> keyframes, PointF initialValue) {
    super(keyframes, initialValue);
  }

  @Override public KeyframeAnimation<PointF> createAnimation() {
    if (!hasAnimation()) {
      return new StaticKeyframeAnimation<>(initialValue);
    } else {
      return new PointKeyframeAnimation(keyframes);
    }
  }

  static final class Factory {
    private Factory() {
    }

    static AnimatablePointValue newInstance(JSONObject json, LottieComposition composition) {
      AnimatableValueParser.Result<PointF> result = AnimatableValueParser
          .newInstance(json, composition.getDpScale(), composition, PointFFactory.INSTANCE)
          .parseJson();
      return new AnimatablePointValue(result.keyframes, result.initialValue);
    }
  }
}
