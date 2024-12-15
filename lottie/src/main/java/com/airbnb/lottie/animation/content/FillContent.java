package com.airbnb.lottie.animation.content;

import static com.airbnb.lottie.utils.MiscUtils.clamp;

import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import androidx.annotation.Nullable;

import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.animation.LPaint;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.ColorKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.ValueCallbackKeyframeAnimation;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.model.content.ShapeFill;
import com.airbnb.lottie.model.layer.BaseLayer;
import com.airbnb.lottie.utils.DropShadow;
import com.airbnb.lottie.utils.MiscUtils;
import com.airbnb.lottie.value.LottieValueCallback;

import java.util.ArrayList;
import java.util.List;

public class FillContent
    implements DrawingContent, BaseKeyframeAnimation.AnimationListener, KeyPathElementContent {

  private final Path path = new Path();
  private final Paint paint = new LPaint(Paint.ANTI_ALIAS_FLAG);
  private final BaseLayer layer;
  private final String name;
  private final boolean hidden;
  private final List<PathContent> paths = new ArrayList<>();
  private final BaseKeyframeAnimation<Integer, Integer> colorAnimation;
  private final BaseKeyframeAnimation<Integer, Integer> opacityAnimation;
  @Nullable private BaseKeyframeAnimation<ColorFilter, ColorFilter> colorFilterAnimation;
  private final LottieDrawable lottieDrawable;
  @Nullable private BaseKeyframeAnimation<Float, Float> blurAnimation;
  float blurMaskFilterRadius;

  public FillContent(final LottieDrawable lottieDrawable, BaseLayer layer, ShapeFill fill) {
    this.layer = layer;
    name = fill.getName();
    hidden = fill.isHidden();
    this.lottieDrawable = lottieDrawable;
    if (layer.getBlurEffect() != null) {
      blurAnimation = layer.getBlurEffect().getBlurriness().createAnimation();
      blurAnimation.addUpdateListener(this);
      layer.addAnimation(blurAnimation);
    }

    if (fill.getColor() == null || fill.getOpacity() == null) {
      colorAnimation = null;
      opacityAnimation = null;
      return;
    }

    path.setFillType(fill.getFillType());

    colorAnimation = fill.getColor().createAnimation();
    colorAnimation.addUpdateListener(this);
    layer.addAnimation(colorAnimation);
    opacityAnimation = fill.getOpacity().createAnimation();
    opacityAnimation.addUpdateListener(this);
    layer.addAnimation(opacityAnimation);
  }

  @Override public void onValueChanged() {
    lottieDrawable.invalidateSelf();
  }

  @Override public void setContents(List<Content> contentsBefore, List<Content> contentsAfter) {
    for (int i = 0; i < contentsAfter.size(); i++) {
      Content content = contentsAfter.get(i);
      if (content instanceof PathContent) {
        paths.add((PathContent) content);
      }
    }
  }

  @Override public String getName() {
    return name;
  }

  @Override public void draw(Canvas canvas, Matrix parentMatrix, int parentAlpha, @Nullable DropShadow shadowToApply, float blurToApply) {
    if (hidden) {
      return;
    }
    if (L.isTraceEnabled()) {
      L.beginSection("FillContent#draw");
    }
    int color = ((ColorKeyframeAnimation) this.colorAnimation).getIntValue();
    float fillAlpha = opacityAnimation.getValue() / 100f;
    int alpha = (int) (parentAlpha * fillAlpha);
    alpha = clamp(alpha, 0, 255);
    paint.setColor((alpha << 24) | (color & 0xFFFFFF));

    if (colorFilterAnimation != null) {
      paint.setColorFilter(colorFilterAnimation.getValue());
    }


    if (blurToApply == 0f) {
      paint.setMaskFilter(null);
    } else if (blurToApply != blurMaskFilterRadius) {
      BlurMaskFilter blur = layer.getBlurMaskFilter(blurToApply);
      paint.setMaskFilter(blur);
    }
    blurMaskFilterRadius = blurToApply;

    if (shadowToApply != null) {
      shadowToApply.applyWithAlpha((int)(fillAlpha * 255), paint);
    } else {
      paint.clearShadowLayer();
    }

    path.reset();
    for (int i = 0; i < paths.size(); i++) {
      path.addPath(paths.get(i).getPath(), parentMatrix);
    }

    canvas.drawPath(path, paint);

    if (L.isTraceEnabled()) {
      L.endSection("FillContent#draw");
    }
  }

  @Override public void getBounds(RectF outBounds, Matrix parentMatrix, boolean applyParents) {
    path.reset();
    for (int i = 0; i < paths.size(); i++) {
      this.path.addPath(paths.get(i).getPath(), parentMatrix);
    }
    path.computeBounds(outBounds, false);
    // Add padding to account for rounding errors.
    outBounds.set(
        outBounds.left - 1,
        outBounds.top - 1,
        outBounds.right + 1,
        outBounds.bottom + 1
    );
  }

  @Override public void resolveKeyPath(
      KeyPath keyPath, int depth, List<KeyPath> accumulator, KeyPath currentPartialKeyPath) {
    MiscUtils.resolveKeyPath(keyPath, depth, accumulator, currentPartialKeyPath, this);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> void addValueCallback(T property, @Nullable LottieValueCallback<T> callback) {
    if (property == LottieProperty.COLOR) {
      colorAnimation.setValueCallback((LottieValueCallback<Integer>) callback);
    } else if (property == LottieProperty.OPACITY) {
      opacityAnimation.setValueCallback((LottieValueCallback<Integer>) callback);
    } else if (property == LottieProperty.COLOR_FILTER) {
      if (colorFilterAnimation != null) {
        layer.removeAnimation(colorFilterAnimation);
      }

      if (callback == null) {
        colorFilterAnimation = null;
      } else {
        colorFilterAnimation =
            new ValueCallbackKeyframeAnimation<>((LottieValueCallback<ColorFilter>) callback);
        colorFilterAnimation.addUpdateListener(this);
        layer.addAnimation(colorFilterAnimation);
      }
    } else if (property == LottieProperty.BLUR_RADIUS) {
      if (blurAnimation != null) {
        blurAnimation.setValueCallback((LottieValueCallback<Float>) callback);
      } else {
        blurAnimation =
            new ValueCallbackKeyframeAnimation<>((LottieValueCallback<Float>) callback);
        blurAnimation.addUpdateListener(this);
        layer.addAnimation(blurAnimation);
      }
    }
  }
}
