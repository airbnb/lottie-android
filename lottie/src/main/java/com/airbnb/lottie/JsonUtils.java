package com.airbnb.lottie;

import android.graphics.PointF;

import org.json.JSONArray;
import org.json.JSONObject;

class JsonUtils {
  private JsonUtils() {
  }

  static PointF pointFromJsonObject(JSONObject values, float scale) {
    return new PointF(
        valueFromObject(values.opt("x")) * scale,
        valueFromObject(values.opt("y")) * scale);
  }

  static PointF pointFromJsonArray(JSONArray values, float scale) {
    if (values.length() < 2) {
      throw new IllegalArgumentException("Unable to parse point for " + values);
    }
    return new PointF(
        (float) values.optDouble(0, 1) * scale,
        (float) values.optDouble(1, 1) * scale);
  }

  static float valueFromObject(Object object) {
    if (object instanceof Float) {
      return (float) object;
    } else if (object instanceof Integer) {
      return (Integer) object;
    } else if (object instanceof Double) {
      return (float) (double) object;
    } else if (object instanceof JSONArray) {
      return (float) ((JSONArray) object).optDouble(0);
    } else {
      return 0;
    }
  }
}
