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
import com.airbnb.lottie.animation.keyframe.BlurKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.DropShadowKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.ValueCallbackKeyframeAnimation;
import com.airbnb.lottie.utils.DropShadow;
import com.airbnb.lottie.utils.OffscreenLayer;
import com.airbnb.lottie.utils.Utils;
import com.airbnb.lottie.value.LottieValueCallback;

public class ImageLayer extends BaseLayer {

  private final Paint paint = new LPaint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
  private final Rect src = new Rect();
  private final Rect dst = new Rect();
  private final RectF layerBounds = new RectF();
  @Nullable private final LottieImageAsset lottieImageAsset;
  @Nullable private BaseKeyframeAnimation<ColorFilter, ColorFilter> colorFilterAnimation;
  @Nullable private BaseKeyframeAnimation<Bitmap, Bitmap> imageAnimation;
  @Nullable private DropShadowKeyframeAnimation dropShadowAnimation;
  @Nullable private BlurKeyframeAnimation blurAnimation;
  @Nullable private OffscreenLayer offscreenLayer;
  @Nullable private OffscreenLayer.ComposeOp offscreenOp;

  ImageLayer(LottieDrawable lottieDrawable, Layer layerModel) {
    super(lottieDrawable, layerModel);
    lottieImageAsset = lottieDrawable.getLottieImageAssetForId(layerModel.getRefId());

    if (getDropShadowEffect() != null) {
      dropShadowAnimation = new DropShadowKeyframeAnimation(this, this, getDropShadowEffect());
    }

    if (getBlurEffect() != null) {
      blurAnimation = new BlurKeyframeAnimation(this, this, getBlurEffect());
    }
  }

  @Override public void drawLayer(@NonNull Canvas canvas, Matrix parentMatrix, int parentAlpha, @Nullable DropShadow parentShadowToApply, float parentBlurToApply) {
    Bitmap bitmap = getBitmap();
    if (bitmap == null || bitmap.isRecycled() || lottieImageAsset == null) {
      return;
    }
    float density = Utils.dpScale();

    paint.setAlpha(parentAlpha);
    if (colorFilterAnimation != null) {
      paint.setColorFilter(colorFilterAnimation.getValue());
    }

    @Nullable DropShadow shadowToApply = dropShadowAnimation != null
        ? dropShadowAnimation.evaluate(parentMatrix, parentAlpha)
        : parentShadowToApply;
    float blurToApply = parentBlurToApply;
    if (blurAnimation != null) {
      blurToApply += blurAnimation.evaluate(parentMatrix);
    }

    src.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
    if (lottieDrawable.getMaintainOriginalImageBounds()) {
      dst.set(0, 0, (int) (lottieImageAsset.getWidth() * density), (int) (lottieImageAsset.getHeight() * density));
    } else {
      dst.set(0, 0, (int) (bitmap.getWidth() * density), (int) (bitmap.getHeight() * density));
    }

    // Render off-screen if we have a drop shadow, because:
    // - Android does not apply drop-shadows to bitmaps properly in HW accelerated contexts (blur is ignored)
    // - On some newer phones (empirically verified), no shadow is rendered at all even in software contexts.
    boolean renderOffScreen = shadowToApply != null || blurToApply > 0.0f;
    Canvas targetCanvas = canvas;
    if (renderOffScreen) {
      if (offscreenLayer == null) offscreenLayer = new OffscreenLayer();
      if (offscreenOp == null) offscreenOp = new OffscreenLayer.ComposeOp();
      offscreenOp.reset();
      // We don't use offscreenOp for compositing here, so we still need to account for its alpha
      // when drawing the shadow.
      if (shadowToApply != null) {
        shadowToApply.applyWithAlpha(parentAlpha, offscreenOp);
      }
      offscreenOp.blur = blurToApply;

      // We don't use getBounds() as it expects the parent-to-world matrix, and what we have in parentMatrix is in
      // fact us-to-world (parent-to-world * us-to-parent)
      layerBounds.set(dst.left, dst.top, dst.right, dst.bottom);
      parentMatrix.mapRect(layerBounds);
      targetCanvas = offscreenLayer.start(canvas, layerBounds, offscreenOp);
    }

    targetCanvas.save();
    targetCanvas.concat(parentMatrix);
    targetCanvas.drawBitmap(bitmap, src, dst, paint);
    targetCanvas.restore();

    if (renderOffScreen) {
      offscreenLayer.finish();
    }
  }

  @Override public void getBounds(RectF outBounds, Matrix parentMatrix, boolean applyParents) {
    super.getBounds(outBounds, parentMatrix, applyParents);
    if (lottieImageAsset != null) {
      float scale = Utils.dpScale();
      if (lottieDrawable.getMaintainOriginalImageBounds()) {
        outBounds.set(0, 0, lottieImageAsset.getWidth() * scale, lottieImageAsset.getHeight() * scale);
      } else {
        outBounds.set(0, 0, getBitmap().getWidth() * scale, getBitmap().getHeight() * scale);
      }
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
    } else if (property == LottieProperty.DROP_SHADOW_COLOR && dropShadowAnimation != null) {
        dropShadowAnimation.setColorCallback((LottieValueCallback<Integer>) callback);
    } else if (property == LottieProperty.DROP_SHADOW_OPACITY && dropShadowAnimation != null) {
      dropShadowAnimation.setOpacityCallback((LottieValueCallback<Float>) callback);
    } else if (property == LottieProperty.DROP_SHADOW_DIRECTION && dropShadowAnimation != null) {
      dropShadowAnimation.setDirectionCallback((LottieValueCallback<Float>) callback);
    } else if (property == LottieProperty.DROP_SHADOW_DISTANCE && dropShadowAnimation != null) {
      dropShadowAnimation.setDistanceCallback((LottieValueCallback<Float>) callback);
    } else if (property == LottieProperty.DROP_SHADOW_RADIUS && dropShadowAnimation != null) {
      dropShadowAnimation.setRadiusCallback((LottieValueCallback<Float>) callback);
    } else if (property == LottieProperty.BLUR_RADIUS && blurAnimation != null) {
      blurAnimation.setBlurrinessCallback((LottieValueCallback<Float>) callback);
    }
  }
}
