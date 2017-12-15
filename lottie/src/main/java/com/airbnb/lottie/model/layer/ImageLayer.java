package com.airbnb.lottie.model.layer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.ValueCallbackKeyframeAnimation;
import com.airbnb.lottie.value.LottieValueCallback;

public class ImageLayer extends BaseLayer {

  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
  private final Rect src = new Rect();
  private final Rect dst = new Rect();
  private final float density;
  @Nullable private BaseKeyframeAnimation<ColorFilter, ColorFilter> colorFilterAnimation;

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
    if (colorFilterAnimation != null) {
      paint.setColorFilter(colorFilterAnimation.getValue());
    }
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

  @SuppressWarnings("SingleStatementInBlock")
  @Override
  public <T> void addValueCallback(T property, @Nullable LottieValueCallback<T> callback) {
    super.addValueCallback(property, callback);
     if (property == LottieProperty.COLOR_FILTER) {
       if (callback == null) {
         colorFilterAnimation = null;
       } else {
         colorFilterAnimation =
             new ValueCallbackKeyframeAnimation<>((LottieValueCallback<ColorFilter>) callback);
       }
    }
  }
}
