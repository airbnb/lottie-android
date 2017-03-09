package com.airbnb.lottie;

import android.graphics.Canvas;
import android.graphics.Matrix;

class NullLayer extends AnimatableLayer {
  NullLayer(LottieDrawable lottieDrawable, Layer layerModel) {
    super(lottieDrawable, layerModel);
  }

  @Override void drawLayer(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
    // Do nothing.
  }
}
