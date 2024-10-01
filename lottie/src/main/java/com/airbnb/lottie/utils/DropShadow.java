package com.airbnb.lottie.utils;

import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import androidx.annotation.Nullable;

/**
 * Settings for a drop shadow to apply.
 */
public class DropShadow {
  private float radius = 0.0f;
  private float dx = 0.0f;
  private float dy = 0.0f;
  private int color = 0x0;

  public DropShadow() {
  }

  public DropShadow(float radius, float dx, float dy, int color) {
    this.radius = radius;
    this.dx = dx;
    this.dy = dy;
    this.color = color;
  }

  public DropShadow(DropShadow other) {
    this.radius = other.radius;
    this.dx = other.dx;
    this.dy = other.dy;
    this.color = other.color;
  }

  public float getRadius() {
    return radius;
  }

  public void setRadius(float radius) {
    this.radius = radius;
  }

  public float getDx() {
    return dx;
  }

  public void setDx(float dx) {
    this.dx = dx;
  }

  public float getDy() {
    return dy;
  }

  public void setDy(float dy) {
    this.dy = dy;
  }

  public int getColor() {
    return color;
  }

  public void setColor(int color) {
    this.color = color;
  }

  public boolean sameAs(DropShadow other) {
    return radius == other.radius && dx == other.dx && dy == other.dy && color == other.color;
  }

  public void transformBy(Matrix matrix) {
    float[] vecs = {dx, dy};
    matrix.mapVectors(vecs);

    dx = vecs[0];
    dy = vecs[1];
    radius = matrix.mapRadius(radius);
  }

  public void multiplyOpacity(int newAlpha) {
    int opacity = Math.round(Color.alpha(color) * MiscUtils.clamp(newAlpha, 0, 255) / 255f);
    color = Color.argb(opacity, Color.red(color), Color.green(color), Color.blue(color));
  }

  /**
   * Applies a shadow to the provided Paint object.
   */
  public void applyTo(Paint paint) {
    if (Color.alpha(color) > 0) {
      // Paint.setShadowLayer() removes the shadow if radius is 0, so we use a small nonzero value in that case
      paint.setShadowLayer(Math.max(radius, Float.MIN_VALUE), dx, dy, color);
    } else {
      paint.clearShadowLayer();
    }
  }

  /**
   * Applies a shadow to the provided Paint object, mixing its alpha by the provided value.
   */
  public void applyWithAlpha(int alpha, Paint paint) {
    int finalAlpha = Utils.mixOpacities(Color.alpha(color), MiscUtils.clamp(alpha, 0, 255));
    if (finalAlpha > 0) {
      int newColor = Color.argb(finalAlpha, Color.red(color), Color.green(color), Color.blue(color));
      // Paint.setShadowLayer() removes the shadow if radius is 0, so we use a small nonzero value in that case
      paint.setShadowLayer(Math.max(radius, Float.MIN_VALUE), dx, dy, newColor);
    } else {
      paint.clearShadowLayer();
    }
  }

  /**
   * Applies a shadow to the provided ComposeOp, mixing its alpha by the provided value.
   */
  public void applyWithAlpha(int alpha, OffscreenLayer.ComposeOp op) {
    op.shadow = new DropShadow(this);
    op.shadow.multiplyOpacity(alpha);
  }

  /**
   * Applies a shadow to the provided ComposeOp, to be used with OffscreenLayer.
   */
  public void applyTo(OffscreenLayer.ComposeOp op) {
    if (Color.alpha(color) > 0) {
      op.shadow = this;
    } else {
      op.shadow = null;
    }
  }
}
