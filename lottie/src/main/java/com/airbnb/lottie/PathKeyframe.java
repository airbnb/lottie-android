package com.airbnb.lottie;

import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

class PathKeyframe extends Keyframe<PointF> {

  @Nullable private Path path;


  PathKeyframe(JSONObject json, LottieComposition composition,
      AnimatableValue<PointF, ?> animatableValue) throws JSONException {
    super(json, composition, composition.getScale(), animatableValue);
    PointF cp1 = null;
    PointF cp2 = null;
    if (json.has("to") && json.has("ti")) {
      cp1 =
          JsonUtils.pointFromJsonArray(json.getJSONArray("to"), composition.getScale());
      cp2 =
          JsonUtils.pointFromJsonArray(json.getJSONArray("ti"), composition.getScale());
    }

    if (endValue != null && !startValue.equals(endValue)) {
      path = Utils.createPath(startValue, endValue, cp1, cp2);
    }
  }

  /**
   * This will be null if the startValue and endValue are the same.
   */
  @Nullable
  Path getPath() {
    return path;
  }
}
