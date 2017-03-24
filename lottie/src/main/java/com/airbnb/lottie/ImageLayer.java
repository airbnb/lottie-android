package com.airbnb.lottie;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

class ImageLayer extends BaseLayer {

  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
  private final Rect src = new Rect();
  private final Rect dst = new Rect();
  private final float density;

  ImageLayer(LottieDrawable lottieDrawable, Layer layerModel, float density) {
    super(lottieDrawable, layerModel);
    this.density = density;
  }

  @Override public void drawLayer(@NonNull Canvas canvas, Matrix parentMatrix, int parentAlpha) {
    Bitmap bitmap = getBitmap();
    if (bitmap == null) {
      return;
    }
    paint.setAlpha(parentAlpha);
    canvas.save();
    canvas.concat(parentMatrix);
    src.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
    dst.set(0, 0, (int) (bitmap.getWidth() * density), (int) (bitmap.getHeight() * density));
    canvas.drawBitmap(bitmap, src, dst , paint);
    canvas.restore();
  }

  @Override public void getBounds(RectF outBounds, Matrix parentMatrix) {
    super.getBounds(outBounds, parentMatrix);
    Bitmap bitmap = getBitmap();
    if (bitmap != null) {
      outBounds.set(
          outBounds.left,
          outBounds.top,
          Math.min(outBounds.right, bitmap.getWidth()),
          Math.min(outBounds.bottom, bitmap.getHeight())
      );
      boundsMatrix.mapRect(outBounds);
    }

  }

  @Nullable
  private Bitmap getBitmap() {
    String refId = layerModel.getRefId();
    return lottieDrawable.getImageAsset(refId);
  }

  @Override public void addColorFilter(@Nullable String layerName, @Nullable String contentName,
      @Nullable ColorFilter colorFilter) {
    paint.setColorFilter(colorFilter);
  }
}
