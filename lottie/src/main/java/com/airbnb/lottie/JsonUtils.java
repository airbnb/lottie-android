package com.airbnb.lottie;

import android.graphics.PointF;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class JsonUtils {
  private JsonUtils() {
  }

  static PointF pointFromJsonObject(JSONObject values, float scale) {
    try {
      return new PointF(
          valueFromObject(values.get("x")) * scale,
          valueFromObject(values.get("y")) * scale
      );
    } catch (JSONException e) {
      return new PointF();
    }
  }

  static PointF pointFromJsonArray(JSONArray values, float scale) {
    if (values.length() < 2) {
      throw new IllegalArgumentException("Unable to parse point for " + values);
    }
    try {
      return new PointF((float) values.getDouble(0) * scale, (float) values.getDouble(1) * scale);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Unable to parse point for " + values, e);
    }
  }

  static float valueFromObject(Object object) {
    if (object instanceof Float) {
      return (float) object;
    } else if (object instanceof Integer) {
      return (Integer) object;
    } else if (object instanceof Double) {
      return (float) (double) object;
    } else if (object instanceof JSONArray) {
      try {
        return (float) ((JSONArray) object).getDouble(0);
      } catch (JSONException e) {
        return 0;
      }
    }
    return 0;
  }
}
