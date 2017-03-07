package com.airbnb.lottie;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;

import java.util.ArrayList;
import java.util.List;

class LayerView extends AnimatableLayer {
  private static final int SAVE_FLAGS = Canvas.CLIP_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG |
      Canvas.MATRIX_SAVE_FLAG;
  private MaskKeyframeAnimation mask;
  private LayerView matteLayer;

  private final Paint solidBackgroundPaint = new Paint();
  @ColorInt private int backgroundColor;
  private final RectF rect = new RectF();
  private final List<LayerView> transformLayers = new ArrayList<>();
  private final Paint mainCanvasPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint mattePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint clearPaint = new Paint();
  private final Paint imagePaint =
      new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

  private final Layer layerModel;
  private final LottieComposition composition;

  @Nullable private LayerView parentLayer;
  /**
   * The width and height of the precomp that this was added to.
   * This differs from the LayerModel precompWidth and height which will be set if this is
   * the precomp layer itself.
   */
  private int precompWidth;
  private int precompHeight;

  LayerView(Layer layerModel, LottieComposition composition, LottieDrawable lottieDrawable) {
    super(lottieDrawable);
    this.layerModel = layerModel;
    this.composition = composition;

    solidBackgroundPaint.setAlpha(0);
    solidBackgroundPaint.setStyle(Paint.Style.FILL);

    if (layerModel.getMatteType() == Layer.MatteType.Invert) {
      mattePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
    } else {
      mattePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
    }
    maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
    clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

    setupForModel();
  }

  private void setupForModel() {
    setBackgroundColor(layerModel.getSolidColor());

    setTransform(layerModel.getTransform().createAnimation());
    setupInOutAnimations();

    switch (layerModel.getLayerType()) {
      case Shape:
        setupShapeLayer();
        break;
      case PreComp:
        setupPreCompLayer();
        break;
    }

    if (layerModel.getMasks() != null && !layerModel.getMasks().isEmpty()) {
      setMask(new MaskKeyframeAnimation(layerModel.getMasks()));
    }

    LongSparseArray<LayerView> layerMap = new LongSparseArray<>();

    for (AnimatableLayer layer : layers) {
      if (layer instanceof LayerView) {
        layerMap.put(((LayerView) layer).getId(), ((LayerView) layer));
        LayerView matteLayer = ((LayerView) layer).matteLayer;
        if (matteLayer != null) {
          layerMap.put(matteLayer.getId(), matteLayer);
        }
      }
    }

    for (AnimatableLayer layer : layers) {
      if (!(layer instanceof LayerView)) {
        continue;
      }
      long parentId = ((LayerView) layer).getLayerModel().getParentId();
      LayerView parentLayer = layerMap.get(parentId);
      if (parentLayer != null) {
        ((LayerView) layer).setParentLayer(parentLayer);
      }

      LayerView matteLayer = ((LayerView) layer).matteLayer;
      if (matteLayer != null) {
        parentId = matteLayer.getLayerModel().getParentId();
        parentLayer = layerMap.get(parentId);
        if (parentLayer != null) {
          matteLayer.setParentLayer(parentLayer);
        }
      }
    }
  }

  void setBackgroundColor(@ColorInt int color) {
    this.backgroundColor = color;
    solidBackgroundPaint.setColor(color);
    invalidateSelf();
  }

  private void setupShapeLayer() {
    ShapeGroup shapeGroup = new ShapeGroup(layerModel.getName(), layerModel.getShapes());
    addLayer(new ContentGroup(shapeGroup, null, null, null, null, lottieDrawable));
  }

  private void setupPreCompLayer() {
    List<Layer> precompLayers = composition.getPrecomps(layerModel.getRefId());
    if (precompLayers == null) {
      return;
    }
    LayerView mattedLayer = null;
    for (int i = precompLayers.size() - 1; i >= 0; i--) {
      Layer layer = precompLayers.get(i);
      LayerView layerView = new LayerView(layer, composition, lottieDrawable);
      layerView.setPrecompSize(layerModel.getPreCompWidth(), layerModel.getPreCompHeight());
      if (mattedLayer != null) {
        mattedLayer.setMatteLayer(layerView);
        mattedLayer = null;
      } else {
        addLayer(layerView);
        if (layer.getMatteType() == Layer.MatteType.Add) {
          mattedLayer = layerView;
        } else if (layer.getMatteType() == Layer.MatteType.Invert) {
          mattedLayer = layerView;
        }
      }
    }
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

  Layer getLayerModel() {
    return layerModel;
  }

  void setParentLayer(@Nullable LayerView parentLayer) {
    this.parentLayer = parentLayer;
  }

  @Nullable
  private LayerView getParentLayer() {
    return parentLayer;
  }

  private void setPrecompSize(int width, int height) {
    precompWidth = width;
    precompHeight = height;
  }

  private void setMask(MaskKeyframeAnimation mask) {
    this.mask = mask;
    for (BaseKeyframeAnimation<?, Path> animation : mask.getMaskAnimations()) {
      addAnimation(animation);
      animation.addUpdateListener(pathChangedListener);
    }
  }

  void setMatteLayer(LayerView matteLayer) {
    this.matteLayer = matteLayer;
  }

  @Override public void draw(@NonNull Canvas canvas) {
    if (!isVisible() || mainCanvasPaint.getAlpha() == 0) {
      return;
    }

    int backgroundAlpha = Color.alpha(backgroundColor);
    if (backgroundAlpha != 0) {
      int alpha = backgroundAlpha;
      if (this.transform != null) {
        alpha = alpha * this.transform.getOpacity().getValue() / 255;
      }
      solidBackgroundPaint.setAlpha(alpha);
      if (alpha > 0) {
        canvas.drawRect(
            0,
            0,
            layerModel.getSolidWidth(),
            layerModel.getSolidHeight(),
            solidBackgroundPaint);
      }
    }

    // Make a list of all parent layers.
    transformLayers.clear();
    LayerView parent = parentLayer;
    while (parent != null) {
      transformLayers.add(parent);
      parent = parent.getParentLayer();
    }

    float scale = lottieDrawable.getScale();
    if (precompWidth != 0 || precompHeight != 0) {
      canvas.clipRect(0, 0, precompWidth * scale, precompHeight * scale);
    } else {
      canvas.clipRect(0, 0,
          lottieDrawable.getIntrinsicWidth(),
          lottieDrawable.getIntrinsicHeight());
    }

    if (!hasMasks() && !hasMatte()) {
      int mainCanvasCount = saveCanvas(canvas);
      // Now apply the parent transformations from the top down.
      for (int i = transformLayers.size() - 1; i >= 0; i--) {
        LayerView layer = transformLayers.get(i);
        applyTransformForLayer(canvas, layer);
      }
      drawImageIfNeeded(canvas);
      super.draw(canvas);
      canvas.restoreToCount(mainCanvasCount);
      return;
    }

    // Now apply the parent transformations from the top down.
    rect.set(canvas.getClipBounds());
    canvas.saveLayer(rect, mainCanvasPaint, Canvas.ALL_SAVE_FLAG);
    canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), clearPaint);

    canvas.save();
    drawImageIfNeeded(canvas);
    for (int i = transformLayers.size() - 1; i >= 0; i--) {
      LayerView layer = transformLayers.get(i);
      applyTransformForLayer(canvas, layer);
    }
    super.draw(canvas);
    canvas.restore();

    if (hasMasks()) {
      applyMasks(canvas);
    }

    if (hasMatte()) {
      canvas.saveLayer(rect, mattePaint, SAVE_FLAGS);
      canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), clearPaint);
      matteLayer.draw(canvas);
      canvas.restore();
    }
    canvas.restore();
  }

  private void applyMasks(Canvas canvas) {
    canvas.saveLayer(rect, maskPaint, SAVE_FLAGS);
    canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), clearPaint);

    for (int i = transformLayers.size() - 1; i >= 0; i--) {
      LayerView layer = transformLayers.get(i);
      applyTransformForLayer(canvas, layer);
    }
    applyTransformForLayer(canvas, this);

    float scale = lottieDrawable.getScale();
    canvas.scale(scale, scale);

    int size = mask.getMasks().size();
    for (int i = 0; i < size; i++) {
      Mask mask = this.mask.getMasks().get(i);
      BaseKeyframeAnimation<?, Path> maskAnimation = this.mask.getMaskAnimations().get(i);
      Path maskPath = maskAnimation.getValue();
      switch (mask.getMaskMode()) {
        case MaskModeSubtract:
          maskPath.setFillType(Path.FillType.INVERSE_WINDING);
          break;
        case MaskModeAdd:
        default:
          maskPath.setFillType(Path.FillType.WINDING);
      }
      canvas.drawPath(maskPath, mainCanvasPaint);
    }
    canvas.restore();
  }

  private void drawImageIfNeeded(Canvas canvas) {
    if (!composition.hasImages()) {
      return;
    }
    String refId = layerModel.getRefId();
    Bitmap bitmap = lottieDrawable.getImageAsset(refId);
    if (bitmap == null) {
      return;
    }

    canvas.save();
    applyTransformForLayer(canvas, this);
    canvas.scale(lottieDrawable.getScale(), lottieDrawable.getScale());
    imagePaint.setAlpha(getAlpha());
    canvas.drawBitmap(bitmap, 0, 0 ,imagePaint);
    canvas.restore();
  }

  boolean hasMatte() {
    return matteLayer != null;
  }

  boolean hasMasks() {
    return mask != null && !mask.getMaskAnimations().isEmpty();
  }

  @Override public void setProgress(@FloatRange(from = 0f, to = 1f) float progress) {
    // TODO: use this.
    // progress -= layerModel.getStartProgress();
    progress *= layerModel.getTimeStretch();


    super.setProgress(progress);
    if (matteLayer != null) {
      matteLayer.setProgress(progress);
    }
  }

  long getId() {
    return layerModel.getId();
  }

  @Override public String toString() {
    return layerModel.toString();
  }
}
