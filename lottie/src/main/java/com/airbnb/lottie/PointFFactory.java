package com.airbnb.lottie;

import android.graphics.PointF;

import org.json.JSONArray;
import org.json.JSONObject;

class PointFFactory implements AnimatableValue.Factory<PointF> {
  static final PointFFactory INSTANCE = new PointFFactory();

  private PointFFactory() {
  }

  @Override public PointF valueFromObject(Object object, float scale) {
    if (object instanceof JSONArray) {
      return JsonUtils.pointFromJsonArray((JSONArray) object, scale);
    } else if (object instanceof JSONObject) {
      return JsonUtils.pointFromJsonObject((JSONObject) object, scale);
    } else {
      throw new IllegalArgumentException("Unable to parse point from " + object);
    }
  }
}
