package com.airbnb.lottie;

import org.json.JSONArray;

class ScaleXY {
  private final float scaleX;
  private final float scaleY;

  ScaleXY(float sx, float sy) {
    this.scaleX = sx;
    this.scaleY = sy;
  }

  ScaleXY() {
    this(1f, 1f);
  }

  float getScaleX() {
    return scaleX;
  }

  float getScaleY() {
    return scaleY;
  }

  @Override public String toString() {
    return getScaleX() + "x" + getScaleY();
  }

  static class Factory implements AnimatableValue.Factory<ScaleXY> {
    static final Factory INSTANCE = new Factory();

    private Factory() {
    }

    @Override public ScaleXY valueFromObject(Object object, float scale) {
      JSONArray array = (JSONArray) object;
      return new ScaleXY(
          (float) array.optDouble(0, 1) / 100f * scale,
          (float) array.optDouble(1, 1) / 100f * scale);
    }
  }
}
