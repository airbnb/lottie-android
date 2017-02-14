package com.airbnb.lottie;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

class ShapePath {
  private static final String TAG = ShapePath.class.getSimpleName();

  private final String name;
  private final int index;
  private AnimatableShapeValue shapePath;

  ShapePath(JSONObject json, LottieComposition composition) {
    try {
      index = json.getInt("ind");
    } catch (JSONException e) {
      throw new IllegalArgumentException("ShapePath has no index.", e);
    }

    try {
      name = json.getString("nm");
    } catch (JSONException e) {
      throw new IllegalArgumentException("Layer has no name.", e);
    }

    JSONObject shape;
    try {
      shape = json.getJSONObject("ks");
      shapePath = new AnimatableShapeValue(shape, composition);
    } catch (JSONException e) {
      // Ignore
    }

    if (L.DBG) {
      Log.d(TAG, "Parsed new shape path " + toString());
    }
  }

  AnimatableShapeValue getShapePath() {
    return shapePath;
  }

  @Override public String toString() {
    return "ShapePath{" + "name=" + name +
        ", index=" + index +
        ", hasAnimation=" + shapePath.hasAnimation() +
        '}';
  }
}
