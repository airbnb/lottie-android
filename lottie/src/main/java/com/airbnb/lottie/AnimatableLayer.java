package com.airbnb.lottie;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

class AnimatableLayer {
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

  private final Rect bounds = new Rect();
  final List<AnimatableLayer> layers = new ArrayList<>();
  final LottieDrawable lottieDrawable;
  @Nullable private AnimatableLayer parentLayer;

  private final List<BaseKeyframeAnimation<?, ?>> animations = new ArrayList<>();
  @FloatRange(from = 0f, to = 1f) private float progress = 0f;
  TransformKeyframeAnimation transform;
  private boolean visible = true;

  AnimatableLayer(LottieDrawable lottieDrawable) {
    this.lottieDrawable = lottieDrawable;
  }

  void invalidateSelf() {
    lottieDrawable.invalidateSelf();
  }

  void addAnimation(BaseKeyframeAnimation<?, ?> newAnimation) {
    animations.add(newAnimation);
  }

  void removeAnimation(BaseKeyframeAnimation<?, ?> animation) {
    animations.remove(animation);
  }

  public void draw(@NonNull Canvas canvas) {
    int saveCount = canvas.save();
    applyTransformForLayer(canvas, this);

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

    float scale = lottieDrawable.getScale();

    PointF position = layer.transform.getPosition().getValue();
    if (position.x != 0 || position.y != 0) {
      canvas.translate(position.x * scale, position.y * scale);
    }

    float rotation = layer.transform.getRotation().getValue();
    if (rotation != 0f) {
      canvas.rotate(rotation);
    }

    ScaleXY scaleTransform = layer.transform.getScale().getValue();
    if (scaleTransform.getScaleX() != 1f || scaleTransform.getScaleY() != 1f) {
      canvas.scale(scaleTransform.getScaleX(), scaleTransform.getScaleY());
    }

    PointF anchorPoint = layer.transform.getAnchorPoint().getValue();
    if (anchorPoint.x != 0 || anchorPoint.y != 0) {
      canvas.translate(-anchorPoint.x * scale, -anchorPoint.y * scale);
    }
  }


  public int getAlpha() {
    float alpha = this.transform == null ? 1f : (this.transform.getOpacity().getValue() / 255f);
    float parentAlpha = parentLayer == null ? 1f : (parentLayer.getAlpha() / 255f);
    return (int) (alpha * parentAlpha * 255);
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

  boolean isVisible() {
    return visible;
  }

  void setVisible(boolean visible) {
    this.visible = visible;
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
}
