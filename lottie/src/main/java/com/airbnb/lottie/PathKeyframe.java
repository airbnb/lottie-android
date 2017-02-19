package com.airbnb.lottie;

import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

class PathKeyframe extends Keyframe<PointF> {

  @Nullable private Path path;


  PathKeyframe(JSONObject json, LottieComposition composition,
      AnimatableValue<PointF, ?> animatableValue) {
    super(json, composition, composition.getScale(), animatableValue);
    PointF cp1 = null;
    PointF cp2 = null;
    JSONArray tiJson = json.optJSONArray("ti");
    JSONArray toJson = json.optJSONArray("to");
    if (tiJson != null && toJson != null) {
      cp1 = JsonUtils.pointFromJsonArray(toJson, composition.getScale());
      cp2 = JsonUtils.pointFromJsonArray(tiJson, composition.getScale());
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
