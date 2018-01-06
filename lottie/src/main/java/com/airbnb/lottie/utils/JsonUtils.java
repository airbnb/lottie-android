package com.airbnb.lottie.utils;

import android.graphics.PointF;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonUtils {
  private JsonUtils() {
  }

  public static PointF pointFromJsonObject(JSONObject values) {
    return new PointF(
        valueFromObject(values.opt("x")), valueFromObject(values.opt("y")));
  }

  public static PointF pointFromJsonArray(JSONArray values) {
    if (values.length() < 2) {
      throw new IllegalArgumentException("Unable to parse point for " + values);
    }
    return new PointF((float) values.optDouble(0, 1), (float) values.optDouble(1, 1));
  }

  public static float valueFromObject(Object object) {
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
