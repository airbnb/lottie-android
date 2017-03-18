package com.airbnb.lottie;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

class ImageLayer extends BaseLayer {

  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

  ImageLayer(LottieDrawable lottieDrawable, Layer layerModel) {
    super(lottieDrawable, layerModel);
  }

  @Override public void drawLayer(@NonNull Canvas canvas, Matrix parentMatrix, int parentAlpha) {
    Bitmap bitmap = getBitmap();
    if (bitmap == null) {
      return;
    }
    paint.setAlpha(parentAlpha);
    canvas.save();
    canvas.concat(parentMatrix);
    canvas.drawBitmap(bitmap, 0, 0 , paint);
    canvas.restore();
  }

  @Override public void getBounds(Rect outBounds) {
    Bitmap bitmap = getBitmap();
    if (bitmap == null) {
      outBounds.set(0, 0, 0, 0);
    } else {
      outBounds.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
    }

  }

  @Nullable
  private Bitmap getBitmap() {
    String refId = layerModel.getRefId();
    return lottieDrawable.getImageAsset(refId);
  }
}
