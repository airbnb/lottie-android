package com.airbnb.lottie;

import android.graphics.Canvas;
import android.graphics.Path;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

@Deprecated
class LayerView  {
  /**
   * The width and height of the precomp that this was added to.
   * This differs from the LayerModel precompWidth and height which will be set if this is
   * the precomp layer itself.
   */
  private int precompWidth;
  private int precompHeight;

  LayerView(Layer layerModel, LottieComposition composition, LottieDrawable lottieDrawable) {
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

  @Override public void drawLayer(@NonNull Canvas canvas) {
    if (!isVisible() || mainCanvasPaint.getAlpha() == 0) {
      return;
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
      super.drawLayer(canvas);
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
    super.drawLayer(canvas);
    canvas.restore();

    if (hasMasks()) {
      applyMasks(canvas);
    }

    if (hasMatte()) {
      canvas.saveLayer(rect, mattePaint, SAVE_FLAGS);
      canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), clearPaint);
      matteLayer.drawLayer(canvas);
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
