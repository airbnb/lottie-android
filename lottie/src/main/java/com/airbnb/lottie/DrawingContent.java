package com.airbnb.lottie;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.support.annotation.Nullable;

interface DrawingContent extends Content {
  void draw(Canvas canvas, Matrix parentMatrix, int alpha);
  void getBounds(RectF outBounds, Matrix parentMatrix);

  /**
   * To color a specific layer in the composition
   * @param name name of layer to apply the color filter to
   * @param colorFilter the color filter to apply
   */
  void setColorFilter(@Nullable String name, @Nullable ColorFilter colorFilter);
}
