package com.airbnb.lottie.value;

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
}
