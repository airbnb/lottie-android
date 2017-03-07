package com.airbnb.lottie;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;

class SolidLayer extends AnimatableLayer {

  private final Paint paint = new Paint();
  private final Layer layerModel;

  SolidLayer(LottieDrawable lottieDrawable, Layer layerModel) {
    super(lottieDrawable, layerModel);
    this.layerModel = layerModel;

    paint.setAlpha(0);
    paint.setStyle(Paint.Style.FILL);
    paint.setColor(layerModel.getSolidColor());
  }

  @Override public void drawLayer(@NonNull Canvas canvas) {
    int backgroundAlpha = Color.alpha(layerModel.getSolidColor());
    if (backgroundAlpha == 0) {
      return;
    }

    canvas.save();
    applyTransformForLayer(canvas, this);

    int alpha = backgroundAlpha * transform.getOpacity().getValue() / 255;
    paint.setAlpha(alpha);
    if (alpha > 0) {
      canvas.drawRect(0, 0, layerModel.getSolidWidth(), layerModel.getSolidHeight(), paint);
    }
    canvas.restore();
  }
}
