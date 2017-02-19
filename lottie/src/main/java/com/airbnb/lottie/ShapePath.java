package com.airbnb.lottie;

import android.util.Log;

import org.json.JSONObject;

class ShapePath {
  private static final String TAG = ShapePath.class.getSimpleName();

  private final String name;
  private final int index;
  private AnimatableShapeValue shapePath;

  ShapePath(JSONObject json, LottieComposition composition) {
    index = json.optInt("ind");
    name = json.optString("nm");
    shapePath = new AnimatableShapeValue(json.optJSONObject("ks"), composition);
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
