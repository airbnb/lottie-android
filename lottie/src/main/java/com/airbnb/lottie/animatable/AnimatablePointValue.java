package com.airbnb.lottie.animatable;

import android.graphics.PointF;
import android.support.annotation.RestrictTo;

import com.airbnb.lottie.animation.KeyframeAnimation;
import com.airbnb.lottie.animation.PointKeyframeAnimation;
import com.airbnb.lottie.animation.StaticKeyframeAnimation;
import com.airbnb.lottie.model.LottieComposition;
import com.airbnb.lottie.utils.JsonUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class AnimatablePointValue extends BaseAnimatableValue<PointF, PointF> {

  public AnimatablePointValue(JSONObject pointValues, int frameRate, LottieComposition composition) {
    super(pointValues, frameRate, composition, true);
  }

  @Override
  protected PointF valueFromObject(Object object, float scale) throws JSONException {
    if (object instanceof JSONArray) {
      return JsonUtils.pointFromJsonArray((JSONArray) object, scale);
    } else if (object instanceof JSONObject) {
      return JsonUtils.pointValueFromJsonObject((JSONObject) object, scale);
    }
    throw new IllegalArgumentException("Unable to parse point from " + object);
  }

  @Override
  public KeyframeAnimation<PointF> createAnimation() {
    if (!hasAnimation()) {
      return new StaticKeyframeAnimation<>(initialValue);
    }

    KeyframeAnimation<PointF> animation = new PointKeyframeAnimation(duration, composition, keyTimes, keyValues, interpolators);
    animation.setStartDelay(delay);
    return animation;
  }

  @Override
  public boolean hasAnimation() {
    return !keyValues.isEmpty();
  }
}
