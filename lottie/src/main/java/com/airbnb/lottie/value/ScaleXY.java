package com.airbnb.lottie.value;

public class ScaleXY {
  private float scaleX;
  private float scaleY;

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

  public void set(float scaleX, float scaleY) {
    this.scaleX = scaleX;
    this.scaleY = scaleY;
  }

  public boolean equals(float scaleX, float scaleY) {
    return this.scaleX == scaleX && this.scaleY == scaleY;
  }

  @Override public String toString() {
    return getScaleX() + "x" + getScaleY();
  }
}
