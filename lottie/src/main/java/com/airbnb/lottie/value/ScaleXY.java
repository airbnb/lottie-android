package com.airbnb.lottie.value;

import com.airbnb.lottie.model.animatable.AnimatableValue;

import org.json.JSONArray;

public class ScaleXY {
  private final float scaleX;
  private final float scaleY;

  public ScaleXY(float sx, float sy) {
    this.scaleX = sx;
    this.scaleY = sy;
  }

  public ScaleXY() {
    this(1f, 1f);
  }

  public float getScaleX() {
    return scaleX;
  }

  public float getScaleY() {
    return scaleY;
  }

  @Override public String toString() {
    return getScaleX() + "x" + getScaleY();
  }

  public static class Factory implements AnimatableValue.Factory<ScaleXY> {
    public static final Factory INSTANCE = new Factory();

    private Factory() {
    }

    @Override public ScaleXY valueFromObject(Object object) {
      JSONArray array = (JSONArray) object;
      return new ScaleXY(
          (float) array.optDouble(0, 1) / 100f,
          (float) array.optDouble(1, 1) / 100f);
    }
  }
}
