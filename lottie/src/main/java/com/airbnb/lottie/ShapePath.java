package com.airbnb.lottie;

import android.support.annotation.Nullable;

import org.json.JSONObject;

class ShapePath {
  private final String name;
  private final int index;
  private final AnimatableShapeValue shapePath;

  private ShapePath(String name, int index, AnimatableShapeValue shapePath) {
    this.name = name;
    this.index = index;
    this.shapePath = shapePath;
  }

  static class Factory {
    private Factory() {
    }

    static ShapePath newInstance(JSONObject json, LottieComposition composition) {
      AnimatableShapeValue animatableShapeValue =
          AnimatableShapeValue.Factory.newInstance(json.optJSONObject("ks"), composition);
      return new ShapePath(json.optString("nm"), json.optInt("ind"), animatableShapeValue);
    }
  }

  public String getName() {
    return name;
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
