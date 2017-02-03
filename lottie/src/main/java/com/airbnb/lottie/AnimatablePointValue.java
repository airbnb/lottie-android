package com.airbnb.lottie;

import android.graphics.PointF;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class AnimatablePointValue extends BaseAnimatableValue<PointF, PointF> {
  AnimatablePointValue(JSONObject pointValues, int frameRate, LottieComposition composition) {
    super(pointValues, frameRate, composition, true);
  }

  @Override protected PointF valueFromObject(Object object, float scale) throws JSONException {
    if (object instanceof JSONArray) {
      return JsonUtils.pointFromJsonArray((JSONArray) object, scale);
    } else if (object instanceof JSONObject) {
      return JsonUtils.pointValueFromJsonObject((JSONObject) object, scale);
    }
    throw new IllegalArgumentException("Unable to parse point from " + object);
  }

  @Override public KeyframeAnimation<PointF> createAnimation() {
    if (!hasAnimation()) {
      return new StaticKeyframeAnimation<>(initialValue);
    }

    KeyframeAnimation<PointF> animation =
        new PointKeyframeAnimation(duration, composition, keyTimes, keyValues, interpolators);
    animation.setStartDelay(delay);
    return animation;
  }

  @Override public boolean hasAnimation() {
    return !keyValues.isEmpty();
  }
}
