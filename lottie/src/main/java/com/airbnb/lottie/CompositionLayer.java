package com.airbnb.lottie;

import android.graphics.Canvas;
import android.support.annotation.FloatRange;
import android.support.v4.util.LongSparseArray;

import java.util.ArrayList;
import java.util.List;

class CompositionLayer extends AnimatableLayer {

  private final List<AnimatableLayer> layers = new ArrayList<>();

  CompositionLayer(LottieDrawable lottieDrawable, Layer layerModel, List<Layer> layerModels,
      LottieComposition composition) {
    super(lottieDrawable, layerModel);

    LongSparseArray<AnimatableLayer> layerMap =
        new LongSparseArray<>(composition.getLayers().size());

    AnimatableLayer mattedLayer = null;
    for (int i = layerModels.size() - 1; i >= 0; i--) {
      Layer lm = layerModels.get(i);
      AnimatableLayer layer = AnimatableLayer.forModel(lm, lottieDrawable, composition);
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
      AnimatableLayer layerView = layerMap.get(key);
      AnimatableLayer parentLayer = layerMap.get(layerView.getLayerModel().getParentId());
      if (parentLayer != null) {
        layerView.setParentLayer(parentLayer);
      }
    }
  }

  @Override void drawLayer(Canvas canvas, int parentAlpha) {
    for (int i = layers.size() - 1; i >= 0 ; i--) {
      layers.get(i).draw(canvas, parentAlpha);
    }
  }

  @Override public void setProgress(@FloatRange(from = 0f, to = 1f) float progress) {
    super.setProgress(progress);
    for (int i = layers.size() - 1; i >= 0; i--) {
      layers.get(i).setProgress(progress);
    }
  }

  boolean hasMasks() {
    for (int i = layers.size() - 1; i >= 0; i--) {
      AnimatableLayer layer = layers.get(i);
      if (layer instanceof ShapeLayer) {
        if (((ShapeLayer) layer).hasMasks()) {
          return true;
        }
      }
    }
    return false;
  }

  @Override boolean hasMatte() {
    if (super.hasMatte()) {
      return true;
    }

    for (int i = layers.size() - 1; i >= 0; i--) {
      if (layers.get(i).hasMatte()) {
        return true;
      }
    }
    return false;
  }
}
