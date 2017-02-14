package com.airbnb.lottie;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class LayerView extends AnimatableLayer {
  private MaskLayer mask;
  private LayerView matteLayer;

  private final List<LayerView> transformLayers = new ArrayList<>();
  private final Paint mainCanvasPaint = new Paint();
  @Nullable private final Bitmap contentBitmap;
  @Nullable private final Bitmap maskBitmap;
  @Nullable private final Bitmap matteBitmap;
  @Nullable private Canvas contentCanvas;
  @Nullable private Canvas maskCanvas;
  @Nullable private Canvas matteCanvas;
  private final Paint maskShapePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint mattePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

  private final Layer layerModel;
  private final LottieComposition composition;

  @Nullable private LayerView parentLayer;

  LayerView(Layer layerModel, LottieComposition composition, Callback callback,
      @Nullable Bitmap mainBitmap, @Nullable Bitmap maskBitmap, @Nullable Bitmap matteBitmap) {
    super(callback);
    this.layerModel = layerModel;
    this.composition = composition;
    this.maskBitmap = maskBitmap;
    this.matteBitmap = matteBitmap;
    this.contentBitmap = mainBitmap;
    mattePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
    setBounds(composition.getBounds());
    if (contentBitmap != null) {
      contentCanvas = new Canvas(contentBitmap);
      if (maskBitmap != null) {
        maskPaint.setShader(
            new BitmapShader(contentBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
      }
    }

    setupForModel();
  }

  private void setupForModel() {
    setBackgroundColor(layerModel.getSolidColor());
    setBounds(0, 0, layerModel.getSolidWidth(), layerModel.getSolidHeight());

    setPosition(layerModel.getPosition().createAnimation());
    setAnchorPoint(layerModel.getAnchor().createAnimation());
    setTransform(layerModel.getScale().createAnimation());
    setRotation(layerModel.getRotation().createAnimation());
    setAlpha(layerModel.getOpacity().createAnimation());

    // TODO: determine if this is necessary
    // setVisible(layerModel.getInOutKeyframes() != null, false);

    List<Object> reversedItems = new ArrayList<>(layerModel.getShapes());
    Collections.reverse(reversedItems);
    Transform currentTransform = null;
    ShapeTrimPath currentTrimPath = null;
    ShapeFill currentFill = null;
    ShapeStroke currentStroke = null;

    for (int i = 0; i < reversedItems.size(); i++) {
      Object item = reversedItems.get(i);
      if (item instanceof ShapeGroup) {
        GroupLayerView groupLayer = new GroupLayerView((ShapeGroup) item, currentFill,
            currentStroke, currentTrimPath, currentTransform, getCallback());
        addLayer(groupLayer);
      } else if (item instanceof ShapeTransform) {
        currentTransform = (ShapeTransform) item;
      } else if (item instanceof ShapeFill) {
        currentFill = (ShapeFill) item;
      } else if (item instanceof ShapeTrimPath) {
        currentTrimPath = (ShapeTrimPath) item;
      } else if (item instanceof ShapeStroke) {
        currentStroke = (ShapeStroke) item;
      } else if (item instanceof ShapePath) {
        ShapePath shapePath = (ShapePath) item;
        ShapeLayerView shapeLayer =
            new ShapeLayerView(shapePath, currentFill, currentStroke, currentTrimPath,
                new ShapeTransform(composition), getCallback());
        addLayer(shapeLayer);
      } else if (item instanceof RectangleShape) {
        RectangleShape shapeRect = (RectangleShape) item;
        RectLayer shapeLayer =
            new RectLayer(shapeRect, currentFill, currentStroke, currentTrimPath,
                new ShapeTransform(composition), getCallback());
        addLayer(shapeLayer);
      } else if (item instanceof CircleShape) {
        CircleShape shapeCircle = (CircleShape) item;
        EllipseLayer shapeLayer =
            new EllipseLayer(shapeCircle, currentFill, currentStroke, currentTrimPath,
                new ShapeTransform(composition), getCallback());
        addLayer(shapeLayer);
      }
    }

    if (maskBitmap != null && layerModel.getMasks() != null && !layerModel.getMasks().isEmpty()) {
      setMask(new MaskLayer(layerModel.getMasks(), getCallback()));
      maskCanvas = new Canvas(maskBitmap);
    }
    buildAnimations();
  }

  private void buildAnimations() {
    if (!layerModel.getInOutKeyframes().isEmpty()) {
      NumberKeyframeAnimation<Float> inOutAnimation = new NumberKeyframeAnimation<>(
          layerModel.getInOutKeyframes(), Float.class);
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

  private void setMask(MaskLayer mask) {
    this.mask = mask;
    // TODO: make this a field like other animation listeners and remove existing ones.
    for (BaseKeyframeAnimation<?, Path> animation : mask.getMasks()) {
      addAnimation(animation);
      animation.addUpdateListener(new KeyframeAnimation.AnimationListener<Path>() {
        @Override public void onValueChanged(Path value) {
          invalidateSelf();
        }
      });
    }
  }

  void setMatteLayer(LayerView matteLayer) {
    if (matteBitmap == null) {
      throw new IllegalArgumentException("Cannot set a matte if no matte bitmap was given!");
    }
    this.matteLayer = matteLayer;
    matteCanvas = new Canvas(matteBitmap);
  }

  @Override public void draw(@NonNull Canvas mainCanvas) {
    if (contentBitmap != null) {
      if (contentBitmap.isRecycled()) {
        return;
      }
      contentBitmap.eraseColor(Color.TRANSPARENT);
    }
    if (maskBitmap != null) {
      maskBitmap.eraseColor(Color.TRANSPARENT);
    }
    if (matteBitmap != null) {
      matteBitmap.eraseColor(Color.TRANSPARENT);
    }
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
    Collections.reverse(transformLayers);

    if (contentCanvas == null || contentBitmap == null) {
      int mainCanvasCount = saveCanvas(mainCanvas);
      // Now apply the parent transformations from the top down.
      for (LayerView layer : transformLayers) {
        applyTransformForLayer(mainCanvas, layer);
      }
      super.draw(mainCanvas);
      mainCanvas.restoreToCount(mainCanvasCount);
      return;
    }

    int contentCanvasCount = saveCanvas(contentCanvas);
    int maskCanvasCount = saveCanvas(maskCanvas);
    // Now apply the parent transformations from the top down.
    for (LayerView layer : transformLayers) {
      applyTransformForLayer(contentCanvas, layer);
      applyTransformForLayer(maskCanvas, layer);
    }
    // We only have to apply the transformation to the mask because it's normally handed in 
    // AnimatableLayer#draw but masks don't go through that.
    applyTransformForLayer(maskCanvas, this);

    super.draw(contentCanvas);

    Bitmap mainBitmap;
    if (hasMasks()) {
      for (int i = 0; i < mask.getMasks().size(); i++) {
        Path path = mask.getMasks().get(i).getValue();
        //noinspection ConstantConditions
        maskCanvas.drawPath(path, maskShapePaint);
      }
      if (!hasMattes()) {
        mainCanvas.drawBitmap(maskBitmap, 0, 0, maskPaint);
      }
      mainBitmap = maskBitmap;
    } else {
      if (!hasMattes()) {
        mainCanvas.drawBitmap(contentBitmap, 0, 0, mainCanvasPaint);
      }
      mainBitmap = contentBitmap;
    }

    restoreCanvas(contentCanvas, contentCanvasCount);
    restoreCanvas(maskCanvas, maskCanvasCount);

    if (hasMattes()) {
      //noinspection ConstantConditions
      matteLayer.draw(matteCanvas);
      matteCanvas.drawBitmap(mainBitmap, 0, 0, mattePaint);
      mainCanvas.drawBitmap(matteBitmap, 0, 0, mainCanvasPaint);
    }
  }

  private boolean hasMattes() {
    return matteCanvas != null && matteBitmap != null && matteLayer != null;
  }

  private boolean hasMasks() {
    return maskBitmap != null && maskCanvas != null && mask != null && !mask.getMasks().isEmpty();
  }

  @Override public void setProgress(@FloatRange(from = 0f, to = 1f) float progress) {
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
