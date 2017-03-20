package com.airbnb.lottie;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;

class SolidLayer extends BaseLayer {

  private final RectF rect = new RectF();
  private final Paint paint = new Paint();
  private final Layer layerModel;

  SolidLayer(LottieDrawable lottieDrawable, Layer layerModel) {
    super(lottieDrawable, layerModel);
    this.layerModel = layerModel;

    paint.setAlpha(0);
    paint.setStyle(Paint.Style.FILL);
    paint.setColor(layerModel.getSolidColor());
  }

  @Override public void drawLayer(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
    int backgroundAlpha = Color.alpha(layerModel.getSolidColor());
    if (backgroundAlpha == 0) {
      return;
    }

    int alpha = (int) ((backgroundAlpha / 255f * transform.getOpacity().getValue() / 100f) * 255);
    paint.setAlpha(alpha);
    if (alpha > 0) {
      updateRect(parentMatrix);
      canvas.drawRect(rect, paint);
    }
  }

  @Override public void getBounds(RectF outBounds, Matrix parentMatrix) {
    super.getBounds(outBounds, parentMatrix);
    updateRect(boundsMatrix);
    outBounds.set(rect);
  }

  private void updateRect(Matrix matrix) {
    rect.set(0, 0, layerModel.getSolidWidth(), layerModel.getSolidHeight());
    matrix.mapRect(rect);
  }

  @Override public void addColorFilter(@Nullable String layerName, @Nullable String contentName,
      @Nullable ColorFilter colorFilter) {
    paint.setColorFilter(colorFilter);
  }
}
