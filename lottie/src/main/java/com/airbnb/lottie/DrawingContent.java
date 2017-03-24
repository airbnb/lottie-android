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
   * Add a color filter to specific content on a specific layer.
   * @param layerName name of the layer where the supplied content name lives, null if color
   *                  filter is to be applied to all layers
   * @param contentName name of the specific content that the color filter is to be applied, null
   *                   is color filter is to be applied to all content that matches the layer param
   * @param colorFilter the color filter, null to clear the color filter
   */
  void addColorFilter(@Nullable String layerName, @Nullable String contentName,
      @Nullable ColorFilter colorFilter);
}
