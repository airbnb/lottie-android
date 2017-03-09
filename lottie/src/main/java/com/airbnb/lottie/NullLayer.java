package com.airbnb.lottie;

import android.graphics.Canvas;

class NullLayer extends AnimatableLayer {
  NullLayer(LottieDrawable lottieDrawable, Layer layerModel) {
    super(lottieDrawable, layerModel);
  }

  @Override void drawLayer(Canvas canvas, int alpha) {
    // Do nothing.
  }
}
