package com.airbnb.lottie.animation.content;

import static com.airbnb.lottie.utils.MiscUtils.clamp;

import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
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
import com.airbnb.lottie.animation.keyframe.DropShadowKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.ValueCallbackKeyframeAnimation;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.model.content.ShapeFill;
import com.airbnb.lottie.model.layer.BaseLayer;
import com.airbnb.lottie.utils.MiscUtils;
import com.airbnb.lottie.value.LottieValueCallback;

import java.util.ArrayList;
import java.util.List;

public class FillContent
    implements DrawingContent, BaseKeyframeAnimation.AnimationListener, KeyPathElementContent {
  private final Path path = new Path();
  private final Matrix pathWithParentMatrixMatrix = new Matrix();
  private int pathGeneration = -23094820;
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

  private static final int DIRTY_FLAG_COLOR = 1;
  private static final int DIRTY_FLAG_OPACITY = 1 << 1;
  private int colorDirtyFlags = DIRTY_FLAG_COLOR | DIRTY_FLAG_OPACITY;
  private int colorFromAnimations = 0;
  private int lastAlpha = Integer.MAX_VALUE;

  @Nullable private DropShadowKeyframeAnimation dropShadowAnimation;

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
    if (layer.getDropShadowEffect() != null) {
      dropShadowAnimation = new DropShadowKeyframeAnimation(this, layer, layer.getDropShadowEffect());
    }

    if (fill.getColor() == null || fill.getOpacity() == null) {
      colorAnimation = null;
      opacityAnimation = null;
      return;
    }

    path.setFillType(fill.getFillType());

    colorAnimation = fill.getColor().createAnimation();
    colorAnimation.addUpdateListener(() -> {
      colorDirtyFlags |= DIRTY_FLAG_COLOR;
      onValueChanged();
    });
    layer.addAnimation(colorAnimation);
    opacityAnimation = fill.getOpacity().createAnimation();
    opacityAnimation.addUpdateListener(() -> {
      colorDirtyFlags |= DIRTY_FLAG_OPACITY;
      onValueChanged();
    });
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

  @Override public void draw(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
    if (hidden) {
      return;
    }
    L.beginSection("FillContent#draw");

    boolean hasDirtyFlags = colorDirtyFlags != 0;
    if ((colorDirtyFlags & DIRTY_FLAG_COLOR) != 0) {
      int color = ((ColorKeyframeAnimation) this.colorAnimation).getIntValue();
      colorFromAnimations = (colorFromAnimations & 0xFF000000) | (color & 0xFFFFFF);
      colorDirtyFlags &= ~DIRTY_FLAG_COLOR;
    }
    if ((colorDirtyFlags & DIRTY_FLAG_OPACITY) != 0) {
      int alpha = (int) ((opacityAnimation.getValue() / 100f) * 255);
      colorFromAnimations = (clamp(alpha, 0, 255) << 24) | (colorFromAnimations & 0xFFFFFF);
      colorDirtyFlags &= ~DIRTY_FLAG_OPACITY;
    }
    int finalAlpha = Color.alpha(colorFromAnimations) * parentAlpha / 255;
    if (finalAlpha != lastAlpha || hasDirtyFlags) {
      paint.setColor(finalAlpha << 24 | (colorFromAnimations & 0xFFFFFF));
    }
    lastAlpha = finalAlpha;

    if (colorFilterAnimation != null) {
      paint.setColorFilter(colorFilterAnimation.getValue());
    }

    if (blurAnimation != null) {
      float blurRadius = blurAnimation.getValue();
      if (blurRadius == 0f) {
        paint.setMaskFilter(null);
      } else if (blurRadius != blurMaskFilterRadius) {
        BlurMaskFilter blur = layer.getBlurMaskFilter(blurRadius);
        paint.setMaskFilter(blur);
      }
      blurMaskFilterRadius = blurRadius;
    }
    if (dropShadowAnimation != null) {
      dropShadowAnimation.applyTo(paint);
    }

    updatePath(parentMatrix);
    canvas.drawPath(path, paint);
    L.endSection("FillContent#draw");
  }

  private void updatePath(Matrix parentMatrix) {
    int pathGeneration = pathGeneration();
    if (this.pathGeneration != pathGeneration || !parentMatrix.equals(pathWithParentMatrixMatrix)) {
      path.rewind();
      for (int i = 0; i < paths.size(); i++) {
        Path newPath = paths.get(i).getPath();
        path.addPath(newPath);
      }
      path.transform(parentMatrix);
      this.pathGeneration = pathGeneration;
      pathWithParentMatrixMatrix.set(parentMatrix);
    }
  }

  private int pathGeneration() {
    int result = 0;
    for (int i = paths.size() - 1; i >= 0; i--) {
      PathContent content = paths.get(i);
      result = 31 * result + content.getGeneration();
    }
    return result;
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
    }
  }
}
