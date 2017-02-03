package com.airbnb.lottie;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

class ShapePath {
  private static final String TAG = ShapePath.class.getSimpleName();

  private final String name;
  private final int index;
  private AnimatableShapeValue shapePath;

  ShapePath(JSONObject json, int frameRate, LottieComposition composition) {
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

    boolean closed = false;
    try {
      closed = json.getBoolean("closed");
    } catch (JSONException e) {
      // Do nothing. Bodymovin 4.4 moved "closed" to be "c" inside of the shape json itself.
    }

    JSONObject shape;
    try {
      shape = json.getJSONObject("ks");
      shapePath = new AnimatableShapeValue(shape, frameRate, composition, closed);
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
