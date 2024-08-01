package com.airbnb.lottie.model.layer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.LottieImageAsset;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.animation.LPaint;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.DropShadowKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.ValueCallbackKeyframeAnimation;
import com.airbnb.lottie.utils.Utils;
import com.airbnb.lottie.value.LottieValueCallback;

public class ImageLayer extends BaseLayer {

  private final Paint paint = new LPaint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
  private final Rect src = new Rect();
  private final Rect dst = new Rect();
  @Nullable private final LottieImageAsset lottieImageAsset;
  @Nullable private BaseKeyframeAnimation<ColorFilter, ColorFilter> colorFilterAnimation;
  @Nullable private BaseKeyframeAnimation<Bitmap, Bitmap> imageAnimation;
  @Nullable private DropShadowKeyframeAnimation dropShadowAnimation;

  ImageLayer(LottieDrawable lottieDrawable, Layer layerModel) {
    super(lottieDrawable, layerModel);
    lottieImageAsset = lottieDrawable.getLottieImageAssetForId(layerModel.getRefId());

    if (getDropShadowEffect() != null) {
      dropShadowAnimation = new DropShadowKeyframeAnimation(this, this, getDropShadowEffect());
    }
  }

  @Override public void drawLayer(@NonNull Canvas canvas, Matrix parentMatrix, int parentAlpha) {
    Bitmap bitmap = getBitmap();
    if (bitmap == null || bitmap.isRecycled() || lottieImageAsset == null) {
      return;
    }
    float density = Utils.dpScale();

    paint.setAlpha(parentAlpha);
    if (colorFilterAnimation != null) {
      paint.setColorFilter(colorFilterAnimation.getValue());
    }
    canvas.save();
    canvas.concat(parentMatrix);
    src.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
    if (lottieDrawable.getMaintainOriginalImageBounds()) {
      dst.set(0, 0, (int) (lottieImageAsset.getWidth() * density), (int) (lottieImageAsset.getHeight() * density));
    } else {
      dst.set(0, 0, (int) (bitmap.getWidth() * density), (int) (bitmap.getHeight() * density));
    }

    if (dropShadowAnimation != null) {
      dropShadowAnimation.applyTo(paint, parentMatrix, parentAlpha);
    }

    canvas.drawBitmap(bitmap, src, dst, paint);
    canvas.restore();
  }

  @Override public void getBounds(RectF outBounds, Matrix parentMatrix, boolean applyParents) {
    super.getBounds(outBounds, parentMatrix, applyParents);
    if (lottieImageAsset != null) {
      float scale = Utils.dpScale();
      outBounds.set(0, 0, lottieImageAsset.getWidth() * scale, lottieImageAsset.getHeight() * scale);
      boundsMatrix.mapRect(outBounds);
    }
  }

  @Nullable
  private Bitmap getBitmap() {
    if (imageAnimation != null) {
      Bitmap callbackBitmap = imageAnimation.getValue();
      if (callbackBitmap != null) {
        return callbackBitmap;
      }
    }
    String refId = layerModel.getRefId();
    Bitmap bitmapFromDrawable = lottieDrawable.getBitmapForId(refId);
    if (bitmapFromDrawable != null) {
      return bitmapFromDrawable;
    }
    LottieImageAsset asset = this.lottieImageAsset;
    if (asset != null) {
      return asset.getBitmap();
    }
    return null;
  }

  @SuppressWarnings("SingleStatementInBlock")
  @Override
  public <T> void addValueCallback(T property, @Nullable LottieValueCallback<T> callback) {
    super.addValueCallback(property, callback);
    if (property == LottieProperty.COLOR_FILTER) {
      if (callback == null) {
        colorFilterAnimation = null;
      } else {
        //noinspection unchecked
        colorFilterAnimation =
            new ValueCallbackKeyframeAnimation<>((LottieValueCallback<ColorFilter>) callback);
      }
    } else if (property == LottieProperty.IMAGE) {
      if (callback == null) {
        imageAnimation = null;
      } else {
        //noinspection unchecked
        imageAnimation =
            new ValueCallbackKeyframeAnimation<>((LottieValueCallback<Bitmap>) callback);
      }
    }
  }
}
