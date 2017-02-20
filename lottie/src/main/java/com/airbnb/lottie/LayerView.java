package com.airbnb.lottie;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class LayerView extends AnimatableLayer {
  private static final int SAVE_FLAGS = Canvas.CLIP_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG;
  private MaskKeyframeAnimation mask;
  private LayerView matteLayer;

  private final PorterDuffXfermode DST_OUT = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
  private final PorterDuffXfermode DST_IN = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
  private final RectF rect = new RectF();
  private final List<LayerView> transformLayers = new ArrayList<>();
  private final Paint mainCanvasPaint = new Paint();
  private final Paint mattePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

  private final Layer layerModel;
  private final LottieComposition composition;
  private final CanvasPool canvasPool;

  @Nullable private LayerView parentLayer;
  /**
   * The width and height of the precomp that this was added to.
   * This differs from the LayerModel precompWidth and height which will be set if this is
   * the precomp layer itself.
   */
  private int precompWidth;
  private int precompHeight;

  LayerView(Layer layerModel, LottieComposition composition, Callback callback, CanvasPool canvasPool) {
    super(callback);
    this.layerModel = layerModel;
    this.composition = composition;
    this.canvasPool = canvasPool;
    setBounds(composition.getBounds());

    if (layerModel.getMatteType() == Layer.MatteType.Invert) {
      mattePaint.setXfermode(DST_OUT);
    } else {
      mattePaint.setXfermode(DST_IN);
    }

    setupForModel();
  }

  private void setupForModel() {
    setBackgroundColor(layerModel.getSolidColor());
    setBounds(0, 0, layerModel.getSolidWidth(), layerModel.getSolidHeight());

    setTransform(layerModel.getTransform().createAnimation());
    setupInOutAnimations();

    switch (layerModel.getLayerType()) {
      case Shape:
        setupShapeLayer();
        break;
      case PreComp:
        setupPreComp();
        break;
    }

    if (layerModel.getMasks() != null && !layerModel.getMasks().isEmpty()) {
      setMask(new MaskKeyframeAnimation(layerModel.getMasks()));
    }

    LongSparseArray<LayerView> layerMap = new LongSparseArray<>();

    for (AnimatableLayer layer : layers) {
      if (layer instanceof LayerView) {
        layerMap.put(((LayerView) layer).getId(), ((LayerView) layer));
      }
    }

    for (AnimatableLayer layer : layers) {
      if (!(layer instanceof LayerView)) {
        continue;
      }
      long parentId = ((LayerView) layer).getLayerModel().getParentId();
      if (parentId == -1) {
        continue;
      }
      LayerView parentLayer = layerMap.get(parentId);
      if (parentLayer == null) {
        continue;
      }
      ((LayerView) layer).setParentLayer(parentLayer);
    }
  }

  private void setupShapeLayer() {
    List<Object> reversedItems = new ArrayList<>(layerModel.getShapes());
    Collections.reverse(reversedItems);
    AnimatableTransform currentTransform = null;
    ShapeTrimPath currentTrim = null;
    ShapeFill currentFill = null;
    ShapeStroke currentStroke = null;

    for (int i = 0; i < reversedItems.size(); i++) {
      Object item = reversedItems.get(i);
      if (item instanceof ShapeGroup) {
        GroupLayerView groupLayer = new GroupLayerView((ShapeGroup) item, currentFill,
            currentStroke, currentTrim, currentTransform, getCallback());
        addLayer(groupLayer);
      } else if (item instanceof AnimatableTransform) {
        currentTransform = (AnimatableTransform) item;
      } else if (item instanceof ShapeFill) {
        currentFill = (ShapeFill) item;
      } else if (item instanceof ShapeTrimPath) {
        currentTrim = (ShapeTrimPath) item;
      } else if (item instanceof ShapeStroke) {
        currentStroke = (ShapeStroke) item;
      } else if (item instanceof ShapePath) {
        ShapePath shapePath = (ShapePath) item;
        ShapeLayerView shapeLayer =
            new ShapeLayerView(shapePath, currentFill, currentStroke, currentTrim,
                new AnimatableTransform(composition), getCallback());
        addLayer(shapeLayer);
      } else if (item instanceof RectangleShape) {
        RectangleShape shapeRect = (RectangleShape) item;
        RectLayer shapeLayer =
            new RectLayer(shapeRect, currentFill, currentStroke, currentTrim,
                new AnimatableTransform(composition), getCallback());
        addLayer(shapeLayer);
      } else if (item instanceof CircleShape) {
        CircleShape shapeCircle = (CircleShape) item;
        EllipseLayer shapeLayer =
            new EllipseLayer(shapeCircle, currentFill, currentStroke, currentTrim,
                new AnimatableTransform(composition), getCallback());
        addLayer(shapeLayer);
      } else if (item instanceof PolystarShape) {
        PolystarShape polystarShape = (PolystarShape) item;
        PolystarLayer shapeLayer = new PolystarLayer(polystarShape, currentFill, currentStroke,
            currentTrim, new AnimatableTransform(composition), getCallback());
        addLayer(shapeLayer);
      }
    }
  }

  private void setupPreComp() {
    List<Layer> precompLayers = composition.getPrecomps(layerModel.getPrecompId());
    if (precompLayers == null) {
      return;
    }
    for (int i = precompLayers.size() - 1; i >= 0; i--) {
      Layer layer = precompLayers.get(i);
      LayerView layerView =
          new LayerView(layer, composition, getCallback(), canvasPool);
      layerView.setPrecompSize(layerModel.getPreCompWidth(), layerModel.getPreCompHeight());
      addLayer(layerView);
    }
  }

  private void setupInOutAnimations() {
    if (!layerModel.getInOutKeyframes().isEmpty()) {
      FloatKeyframeAnimation inOutAnimation =
          new FloatKeyframeAnimation(layerModel.getInOutKeyframes());
      inOutAnimation.setIsDiscrete();
      inOutAnimation.addUpdateListener(new KeyframeAnimation.AnimationListener<Float>() {
        @Override public void onValueChanged(Float value) {
          setVisible(value == 1f, false);
        }
      });
      setVisible(inOutAnimation.getValue() == 1f, false);
      addAnimation(inOutAnimation);
    } else {
      setVisible(true, false);
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

    // Make a list of all parent layers.
    transformLayers.clear();
    LayerView parent = parentLayer;
    while (parent != null) {
      transformLayers.add(parent);
      parent = parent.getParentLayer();
    }

    if (!hasMasks() && !hasMatte()) {
      int mainCanvasCount = saveCanvas(canvas);
      if (precompWidth != 0 || precompHeight != 0) {
        canvas.clipRect(0, 0, precompWidth, precompHeight);
      }
      // Now apply the parent transformations from the top down.
      for (int i = transformLayers.size() - 1; i >= 0; i--) {
        LayerView layer = transformLayers.get(i);
        applyTransformForLayer(canvas, layer);
      }
      super.draw(canvas);
      canvas.restoreToCount(mainCanvasCount);
      return;
    }


    BitmapCanvas bitmapCanvas =
        canvasPool.acquire(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);

    // Now apply the parent transformations from the top down.
    bitmapCanvas.save();
    for (int i = transformLayers.size() - 1; i >= 0; i--) {
      LayerView layer = transformLayers.get(i);
      applyTransformForLayer(bitmapCanvas, layer);
    }
    super.draw(bitmapCanvas);

    rect.set(0, 0, canvas.getWidth(), canvas.getHeight());
    if (hasMasks()) {
      List<Mask> masks = mask.getMasks();
      List<BaseKeyframeAnimation<?, Path>> maskAnimations = mask.getMaskAnimations();
      for (int i = 0; i < masks.size(); i++) {
        applyMask(bitmapCanvas, masks.get(i), maskAnimations.get(i));
      }
    }
    bitmapCanvas.restore();

    if (hasMatte()) {
      bitmapCanvas.saveLayer(rect, mattePaint, SAVE_FLAGS);
      matteLayer.draw(bitmapCanvas);
      bitmapCanvas.restore();
    }

    if (precompWidth != 0 || precompHeight != 0) {
      canvas.clipRect(0, 0, precompWidth, precompHeight);
    }
    canvas.drawBitmap(bitmapCanvas.getBitmap(), 0, 0, null);
    canvasPool.release(bitmapCanvas);
  }

  private void applyMask(BitmapCanvas canvas, Mask mask,
      BaseKeyframeAnimation<?, Path> maskAnimation) {
    switch (mask.getMaskMode()) {
      case MaskModeSubtract:
        maskPaint.setXfermode(DST_OUT);
        break;
      case MaskModeAdd:
      default:
        maskPaint.setXfermode(DST_IN);
    }

    canvas.saveLayer(rect, maskPaint, SAVE_FLAGS);
    for (int i = transformLayers.size() - 1; i >= 0; i--) {
      LayerView layer = transformLayers.get(i);
      applyTransformForLayer(canvas, layer);
    }
    applyTransformForLayer(canvas, this);
    canvas.drawPath(maskAnimation.getValue(), mainCanvasPaint);
    canvas.restore();
  }

  private boolean hasMatte() {
    return matteLayer != null;
  }

  private boolean hasMasks() {
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
