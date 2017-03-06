package com.airbnb.lottie;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

class AnimatableLayer extends Drawable {
  private final KeyframeAnimation.AnimationListener<Integer> integerChangedListener =
      new KeyframeAnimation.AnimationListener<Integer>() {
        @Override
        public void onValueChanged(Integer value) {
          invalidateSelf();
        }
      };
  private final KeyframeAnimation.AnimationListener<Float> floatChangedListener =
      new KeyframeAnimation.AnimationListener<Float>() {
        @Override
        public void onValueChanged(Float value) {
          invalidateSelf();
        }
      };
  private final KeyframeAnimation.AnimationListener<ScaleXY> scaleChangedListener =
      new KeyframeAnimation.AnimationListener<ScaleXY>() {
        @Override
        public void onValueChanged(ScaleXY value) {
          invalidateSelf();
        }
      };
  private final KeyframeAnimation.AnimationListener<PointF> pointChangedListener =
      new KeyframeAnimation.AnimationListener<PointF>() {
        @Override
        public void onValueChanged(PointF value) {
          invalidateSelf();
        }
      };

  final KeyframeAnimation.AnimationListener<Path> pathChangedListener =
      new KeyframeAnimation.AnimationListener<Path>() {
        @Override
        public void onValueChanged(Path value) {
          invalidateSelf();
        }
      };

  final List<AnimatableLayer> layers = new ArrayList<>();
  @Nullable private AnimatableLayer parentLayer;

  private final Paint solidBackgroundPaint = new Paint();
  @ColorInt private int backgroundColor;
  private final List<BaseKeyframeAnimation<?, ?>> animations = new ArrayList<>();
  @FloatRange(from = 0f, to = 1f) private float progress = 0f;
  private TransformKeyframeAnimation transform;

  AnimatableLayer(Drawable.Callback callback) {
    setCallback(callback);

    solidBackgroundPaint.setAlpha(0);
    solidBackgroundPaint.setStyle(Paint.Style.FILL);
  }

  void setBackgroundColor(@ColorInt int color) {
    this.backgroundColor = color;
    solidBackgroundPaint.setColor(color);
    invalidateSelf();
  }

  void addAnimation(BaseKeyframeAnimation<?, ?> newAnimation) {
    animations.add(newAnimation);
  }

  void removeAnimation(BaseKeyframeAnimation<?, ?> animation) {
    animations.remove(animation);
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    int saveCount = canvas.save();
    applyTransformForLayer(canvas, this);

    int backgroundAlpha = Color.alpha(backgroundColor);
    if (backgroundAlpha != 0) {
      int alpha = backgroundAlpha;
      if (this.transform != null) {
        alpha = alpha * this.transform.getOpacity().getValue() / 255;
      }
      solidBackgroundPaint.setAlpha(alpha);
      if (alpha > 0) {
        canvas.drawRect(getBounds(), solidBackgroundPaint);
      }
    }
    for (int i = 0; i < layers.size(); i++) {
      layers.get(i).draw(canvas);
    }
    canvas.restoreToCount(saveCount);
  }

  int saveCanvas(@Nullable Canvas canvas) {
    if (canvas == null) {
      return 0;
    }
    return canvas.save();
  }

  void applyTransformForLayer(@Nullable Canvas canvas, AnimatableLayer layer) {
    if (canvas == null || transform == null) {
      return;
    }

    PointF position = layer.transform.getPosition().getValue();
    if (position.x != 0 || position.y != 0) {
      canvas.translate(position.x, position.y);
    }

    float rotation = layer.transform.getRotation().getValue();
    if (rotation != 0f) {
      canvas.rotate(rotation);
    }

    ScaleXY scale = layer.transform.getScale().getValue();
    if (scale.getScaleX() != 1f || scale.getScaleY() != 1f) {
      canvas.scale(scale.getScaleX(), scale.getScaleY());
    }

    PointF anchorPoint = layer.transform.getAnchorPoint().getValue();
    if (anchorPoint.x != 0 || anchorPoint.y != 0) {
      canvas.translate(-anchorPoint.x, -anchorPoint.y);
    }
  }

  @Override
  public void setAlpha(int alpha) {
    throw new IllegalArgumentException("This shouldn't be used.");
  }


  @Override
  public int getAlpha() {
    return getAlphaInternal();
  }

  /**
   * getAlpha was added in 19. This internal getAlpha allows us to call it
   * without having to avoid suppressing the NewApi lint rule.
   */
  int getAlphaInternal() {
    float alpha = this.transform == null ? 1f : (this.transform.getOpacity().getValue() / 255f);
    float parentAlpha = parentLayer == null ? 1f : (parentLayer.getAlpha() / 255f);
    return (int) (alpha * parentAlpha * 255);
  }

  @Override
  public void setColorFilter(ColorFilter colorFilter) {

  }

  void setTransform(TransformKeyframeAnimation transform) {
    this.transform = transform;
    BaseKeyframeAnimation<?, PointF> anchorPoint = transform.getAnchorPoint();
    BaseKeyframeAnimation<?, PointF> position = transform.getPosition();
    BaseKeyframeAnimation<?, ScaleXY> scale = transform.getScale();
    BaseKeyframeAnimation<?, Float> rotation = transform.getRotation();
    BaseKeyframeAnimation<?, Integer> opacity = transform.getOpacity();

    anchorPoint.addUpdateListener(pointChangedListener);
    position.addUpdateListener(pointChangedListener);
    scale.addUpdateListener(scaleChangedListener);
    rotation.addUpdateListener(floatChangedListener);
    opacity.addUpdateListener(integerChangedListener);

    addAnimation(anchorPoint);
    addAnimation(position);
    addAnimation(scale);
    addAnimation(rotation);
    addAnimation(opacity);
    invalidateSelf();
  }

  @Override
  public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }

  void addLayer(AnimatableLayer layer) {
    layer.parentLayer = this;
    layers.add(layer);
    layer.setProgress(progress);
    invalidateSelf();
  }

  void clearLayers() {
    layers.clear();
    invalidateSelf();
  }

  public void setProgress(@FloatRange(from = 0f, to = 1f) float progress) {
    this.progress = progress;
    for (int i = 0; i < animations.size(); i++) {
      animations.get(i).setProgress(progress);
    }

    for (int i = 0; i < layers.size(); i++) {
      layers.get(i).setProgress(progress);
    }
  }

  float getProgress() {
    return progress;
  }

  LottieDrawable getLottieDrawable() {
    if (!(getCallback() instanceof LottieDrawable)) {
      return null;
    }
    return ((LottieDrawable) getCallback());
  }
}
