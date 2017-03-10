package com.airbnb.lottie;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;

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
      rect.set(0, 0, layerModel.getSolidWidth(), layerModel.getSolidHeight());
      parentMatrix.mapRect(rect);
      canvas.drawRect((int) rect.left, (int) rect.top, (int) rect.right, (int) rect.bottom, paint);
    }
  }
}
