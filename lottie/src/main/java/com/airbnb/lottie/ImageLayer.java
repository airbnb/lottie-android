package com.airbnb.lottie;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;

class ImageLayer extends AnimatableLayer {

  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

  ImageLayer(LottieDrawable lottieDrawable, Layer layerModel) {
    super(lottieDrawable, layerModel);
  }

  @Override public void drawLayer(@NonNull Canvas canvas, int parentAlpha) {
    String refId = layerModel.getRefId();
    Bitmap bitmap = lottieDrawable.getImageAsset(refId);
    if (bitmap == null) {
      return;
    }

    paint.setAlpha(parentAlpha);
    canvas.drawBitmap(bitmap, 0, 0 , paint);
  }
}
