package com.airbnb.lottie;

import android.graphics.Color;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class AnimatableColorValue extends BaseAnimatableValue<Integer, Integer> {
  AnimatableColorValue(JSONObject json, LottieComposition composition) throws JSONException {
    super(json, composition, false);
  }

  @Override public Integer valueFromObject(Object object, float scale) throws JSONException {
    JSONArray colorArray = (JSONArray) object;
    if (colorArray.length() == 4) {
      boolean shouldUse255 = true;
      for (int i = 0; i < colorArray.length(); i++) {
        double colorChannel = colorArray.getDouble(i);
        if (colorChannel > 1f) {
          shouldUse255 = false;
        }
      }

      float multiplier = shouldUse255 ? 255f : 1f;
      return Color.argb(
          (int) (colorArray.getDouble(3) * multiplier),
          (int) (colorArray.getDouble(0) * multiplier),
          (int) (colorArray.getDouble(1) * multiplier),
          (int) (colorArray.getDouble(2) * multiplier));
    }
    return Color.BLACK;
  }


  @Override public KeyframeAnimation<Integer> createAnimation() {
    if (!hasAnimation()) {
      return new StaticKeyframeAnimation<>(initialValue);
    }
    return new ColorKeyframeAnimation(keyframes);
  }

  @Override public String toString() {
    return "AnimatableColorValue{" + "initialValue=" + initialValue + '}';
  }
}
