package com.airbnb.lottie;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.support.annotation.FloatRange;
import android.support.v4.util.LongSparseArray;

import java.util.ArrayList;
import java.util.List;

class CompositionLayer extends BaseLayer {
  private final List<BaseLayer> layers = new ArrayList<>();

  CompositionLayer(LottieDrawable lottieDrawable, Layer layerModel, List<Layer> layerModels,
      LottieComposition composition) {
    super(lottieDrawable, layerModel);

    LongSparseArray<BaseLayer> layerMap =
        new LongSparseArray<>(composition.getLayers().size());

    BaseLayer mattedLayer = null;
    for (int i = layerModels.size() - 1; i >= 0; i--) {
      Layer lm = layerModels.get(i);
      BaseLayer layer = BaseLayer.forModel(lm, lottieDrawable, composition);
      layerMap.put(layer.getLayerModel().getId(), layer);
      if (mattedLayer != null) {
        mattedLayer.setMatteLayer(layer);
        mattedLayer = null;
      } else {
        layers.add(0, layer);
        switch (lm.getMatteType()) {
          case Add:
          case Invert:
            mattedLayer = layer;
            break;
        }
      }
    }

    for (int i = 0; i < layerMap.size(); i++) {
      long key = layerMap.keyAt(i);
      BaseLayer layerView = layerMap.get(key);
      BaseLayer parentLayer = layerMap.get(layerView.getLayerModel().getParentId());
      if (parentLayer != null) {
        layerView.setParentLayer(parentLayer);
      }
    }
  }

  @Override void drawLayer(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
    for (int i = layers.size() - 1; i >= 0 ; i--) {
      layers.get(i).draw(canvas, parentMatrix, parentAlpha);
    }
  }

  @Override public void setProgress(@FloatRange(from = 0f, to = 1f) float progress) {
    super.setProgress(progress);
    progress -= layerModel.getStartProgress();
    for (int i = layers.size() - 1; i >= 0; i--) {
      layers.get(i).setProgress(progress);
    }
  }

  boolean hasMasks() {
    for (int i = layers.size() - 1; i >= 0; i--) {
      BaseLayer layer = layers.get(i);
      if (layer instanceof ShapeLayer) {
        if (layer.hasMasksOnThisLayer()) {
          return true;
        }
      }
    }
    return false;
  }

  boolean hasMatte() {
    if (hasMatteOnThisLayer()) {
      return true;
    }

    for (int i = layers.size() - 1; i >= 0; i--) {
      if (layers.get(i).hasMatteOnThisLayer()) {
        return true;
      }
    }
    return false;
  }
}
