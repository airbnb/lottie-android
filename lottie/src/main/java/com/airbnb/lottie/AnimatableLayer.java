package com.airbnb.lottie;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

abstract class AnimatableLayer {
  private static final int SAVE_FLAGS = Canvas.CLIP_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG |
      Canvas.MATRIX_SAVE_FLAG;

  static AnimatableLayer forModel(
    Layer layerModel, LottieDrawable drawable, LottieComposition composition) {
    switch (layerModel.getLayerType()) {
      case Shape:
        return new ShapeLayer(drawable, layerModel, composition);
      case PreComp:
        return new CompositionLayer(drawable, layerModel,
            composition.getPrecomps(layerModel.getRefId()), composition);
      case Solid:
        return new SolidLayer(drawable, layerModel);
      case Image:
        return new ImageLayer(drawable, layerModel);
      case Null:
        return new NullLayer(drawable, layerModel);
      case Text:
      case Unknown:
      default:
        // Do nothing
        Log.w(L.TAG, "Unknown layer type " + layerModel.getLayerType());
        return new NullLayer(drawable, layerModel);
    }
  }

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

  private final Paint contentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint mattePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint clearPaint = new Paint();
  private final RectF rect = new RectF();
  private final List<AnimatableLayer> transformLayers = new ArrayList<>();
  final LottieDrawable lottieDrawable;
  final Layer layerModel;
  @Nullable private AnimatableLayer matteLayer;
  @Nullable private AnimatableLayer parentLayer;

  private final List<BaseKeyframeAnimation<?, ?>> animations = new ArrayList<>();
  TransformKeyframeAnimation transform;
  private boolean visible = true;

  AnimatableLayer(LottieDrawable lottieDrawable, Layer layerModel) {
    this.lottieDrawable = lottieDrawable;
    this.layerModel = layerModel;
    clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    if (layerModel.getMatteType() == Layer.MatteType.Invert) {
      mattePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
    } else {
      mattePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
    }

    setTransform(layerModel.getTransform().createAnimation());
    setupInOutAnimations();
  }

  Layer getLayerModel() {
    return layerModel;
  }

  void setMatteLayer(@Nullable AnimatableLayer matteLayer) {
    this.matteLayer = matteLayer;
  }

  boolean hasMatte() {
    return matteLayer != null;
  }

  void setParentLayer(@Nullable AnimatableLayer parentLayer) {
    this.parentLayer = parentLayer;
  }

  private void setupInOutAnimations() {
    if (!layerModel.getInOutKeyframes().isEmpty()) {
      FloatKeyframeAnimation inOutAnimation =
          new FloatKeyframeAnimation(layerModel.getInOutKeyframes());
      inOutAnimation.setIsDiscrete();
      inOutAnimation.addUpdateListener(new KeyframeAnimation.AnimationListener<Float>() {
        @Override public void onValueChanged(Float value) {
          setVisible(value == 1f);
        }
      });
      setVisible(inOutAnimation.getValue() == 1f);
      addAnimation(inOutAnimation);
    } else {
      setVisible(true);
    }
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

  void draw(Canvas canvas) {
    if (!hasMatte()) {
      canvas.save();
      applyTransformsForParentLayersAndSelf(canvas);
      drawLayer(canvas);
      canvas.restore();
      return;
    }

    rect.set(canvas.getClipBounds());
    canvas.saveLayer(rect, contentPaint, Canvas.ALL_SAVE_FLAG);
    // Clear the off screen buffer. This is necessary for some phones.
    canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), clearPaint);
    applyTransformsForParentLayersAndSelf(canvas);
    drawLayer(canvas);

    canvas.saveLayer(rect, mattePaint, SAVE_FLAGS);
    canvas.drawRect(rect, clearPaint);
    assert matteLayer != null;
    matteLayer.drawLayer(canvas);
    canvas.restore();

    canvas.restore();
  }

  abstract void drawLayer(Canvas canvas);

  void applyTransformsForParentLayersAndSelf(Canvas canvas) {
    // Make a list of all parent layers.
    transformLayers.clear();
    AnimatableLayer parent = parentLayer;
    while (parent != null) {
      transformLayers.add(parent);
      parent = parent.parentLayer;
    }
    // Now apply the parent transformations from the top down.
    for (int i = transformLayers.size() - 1; i >= 0; i--) {
      AnimatableLayer layer = transformLayers.get(i);
      applyTransformForLayer(canvas, layer);
    }
    applyTransformForLayer(canvas, this);
  }

  void applyTransformForLayer(@Nullable Canvas canvas, AnimatableLayer layer) {
    if (canvas == null || transform == null) {
      return;
    }

    float scale = lottieDrawable.getScale();
    TransformKeyframeAnimation transform = layer.transform;

    PointF position = transform.getPosition().getValue();
    if (position.x != 0 || position.y != 0) {
      canvas.translate(position.x * scale, position.y * scale);
    }

    float rotation = transform.getRotation().getValue();
    if (rotation != 0f) {
      canvas.rotate(rotation);
    }

    ScaleXY scaleTransform = transform.getScale().getValue();
    if (scaleTransform.getScaleX() != 1f || scaleTransform.getScaleY() != 1f) {
      canvas.scale(scaleTransform.getScaleX(), scaleTransform.getScaleY());
    }

    PointF anchorPoint = transform.getAnchorPoint().getValue();
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

  boolean isVisible() {
    return visible;
  }

  private void setVisible(boolean visible) {
    this.visible = visible;
  }

  public void setProgress(@FloatRange(from = 0f, to = 1f) float progress) {
    for (int i = 0; i < animations.size(); i++) {
      animations.get(i).setProgress(progress);
    }
  }
}
