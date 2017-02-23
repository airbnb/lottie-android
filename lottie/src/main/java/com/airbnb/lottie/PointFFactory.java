package com.airbnb.lottie;

import org.json.JSONArray;
import org.json.JSONObject;

class PointFFactory {
  static CPointF newInstance(Object object, float scale) {
    if (object instanceof JSONArray) {
      return JsonUtils.pointFromJsonArray((JSONArray) object, scale);
    } else if (object instanceof JSONObject) {
      return JsonUtils.pointFromJsonObject((JSONObject) object, scale);
    } else {
      throw new IllegalArgumentException("Unable to parse point from " + object);
    }
  }
}
