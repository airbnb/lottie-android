package com.airbnb.lottie;

import org.json.JSONException;
import org.json.JSONObject;

class AnimatableIntegerValue extends BaseAnimatableValue<Integer, Integer> {
  AnimatableIntegerValue(LottieComposition composition, Integer initialValue) {
    super(composition);
    this.initialValue = initialValue;
  }

  AnimatableIntegerValue(JSONObject json, LottieComposition composition,
      boolean isDp, boolean remap100To255) throws JSONException {
    super(json, composition, isDp);
    if (remap100To255) {
      initialValue = initialValue * 255 / 100;
      for (int i = 0; i < keyframes.size(); i++) {
        Keyframe<Integer> keyframe = keyframes.get(i);
        keyframe.startValue = keyframe.startValue * 255 / 100;
        keyframe.endValue = keyframe.endValue * 255 / 100;
      }
    }
  }

  @Override public Integer valueFromObject(Object object, float scale) throws JSONException {
    return Math.round(JsonUtils.valueFromObject(object) * scale);
  }

  @Override public KeyframeAnimation<Integer> createAnimation() {
    if (!hasAnimation()) {
      return new StaticKeyframeAnimation<>(initialValue);
    }

    return new NumberKeyframeAnimation<>(keyframes, Integer.class);
  }

  public Integer getInitialValue() {
    return initialValue;
  }
}
