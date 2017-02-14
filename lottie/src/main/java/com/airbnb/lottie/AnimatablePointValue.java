package com.airbnb.lottie;

import android.graphics.PointF;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class AnimatablePointValue extends BaseAnimatableValue<PointF, PointF> {
  AnimatablePointValue(JSONObject pointValues, LottieComposition composition) throws JSONException {
    super(pointValues, composition, true);
  }

  @Override public PointF valueFromObject(Object object, float scale) throws JSONException {
    if (object instanceof JSONArray) {
      return JsonUtils.pointFromJsonArray((JSONArray) object, scale);
    } else if (object instanceof JSONObject) {
      return JsonUtils.pointFromJsonObject((JSONObject) object, scale);
    }
    throw new IllegalArgumentException("Unable to parse point from " + object);
  }

  @Override public KeyframeAnimation<PointF> createAnimation() {
    if (!hasAnimation()) {
      return new StaticKeyframeAnimation<>(initialValue);
    }

    return new PointKeyframeAnimation(keyframes);
  }
}
