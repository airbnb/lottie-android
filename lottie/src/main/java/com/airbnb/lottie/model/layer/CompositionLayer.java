package com.airbnb.lottie.model.layer;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;

import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.value.KeyPath;
import com.airbnb.lottie.value.LottieValue;

import java.util.ArrayList;
import java.util.List;

public class CompositionLayer extends BaseLayer {
  @Nullable private final BaseKeyframeAnimation<Float, Float> timeRemapping;
  private final List<BaseLayer> layers = new ArrayList<>();
  private final RectF rect = new RectF();
  private final RectF newClipRect = new RectF();

  @Nullable private Boolean hasMatte;
  @Nullable private Boolean hasMasks;

  public CompositionLayer(LottieDrawable lottieDrawable, Layer layerModel, List<Layer> layerModels,
      LottieComposition composition) {
    super(lottieDrawable, layerModel);

    AnimatableFloatValue timeRemapping = layerModel.getTimeRemapping();
    if (timeRemapping != null) {
      this.timeRemapping = timeRemapping.createAnimation();
      addAnimation(this.timeRemapping);
      this.timeRemapping.addUpdateListener(this);
    } else {
      this.timeRemapping = null;
    }

    LongSparseArray<BaseLayer> layerMap =
        new LongSparseArray<>(composition.getLayers().size());

    BaseLayer mattedLayer = null;
    for (int i = layerModels.size() - 1; i >= 0; i--) {
      Layer lm = layerModels.get(i);
      BaseLayer layer = BaseLayer.forModel(lm, lottieDrawable, composition);
      if (layer == null) {
        continue;
      }
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
    L.beginSection("CompositionLayer#draw");
    canvas.save();
    newClipRect.set(0, 0, layerModel.getPreCompWidth(), layerModel.getPreCompHeight());
    parentMatrix.mapRect(newClipRect);

    for (int i = layers.size() - 1; i >= 0 ; i--) {
      boolean nonEmptyClip = true;
      if (!newClipRect.isEmpty()) {
        nonEmptyClip = canvas.clipRect(newClipRect);
      }
      if (nonEmptyClip) {
        BaseLayer layer = layers.get(i);
        layer.draw(canvas, parentMatrix, parentAlpha);
      }
    }
    canvas.restore();
    L.endSection("CompositionLayer#draw");
  }

  @Override public void getBounds(RectF outBounds, Matrix parentMatrix) {
    super.getBounds(outBounds, parentMatrix);
    rect.set(0, 0, 0, 0);
    for (int i = layers.size() - 1; i >= 0; i--) {
      BaseLayer content = layers.get(i);
      content.getBounds(rect, boundsMatrix);
      if (outBounds.isEmpty()) {
        outBounds.set(rect);
      } else {
        outBounds.set(
            Math.min(outBounds.left, rect.left),
            Math.min(outBounds.top, rect.top),
            Math.max(outBounds.right, rect.right),
            Math.max(outBounds.bottom, rect.bottom)
        );
      }
    }
  }

  public <T> void setValue(LottieValue<T> value, KeyPath keyPath) {
    value.apply(this, keyPath);
  }

  public List<BaseLayer> getLayers() {
    return layers;
  }

  @Nullable public BaseLayer layerFor(String name) {
    if (name.equals(getName())) {
      return this;
    }
    for (int i = 0; i < layers.size(); i++) {
      BaseLayer layer = layers.get(i);
      if (name.equals(layer.getName())) {
        return layer;
      }
    }
    return null;
  }

  @Override public void setProgress(@FloatRange(from = 0f, to = 1f) float progress) {
    super.setProgress(progress);
    if (timeRemapping != null) {
      long duration = lottieDrawable.getComposition().getDuration();
      long remappedTime = (long) (timeRemapping.getValue() * 1000);
      progress = remappedTime / (float) duration;
    }
    if (layerModel.getTimeStretch() != 0) {
      progress /= layerModel.getTimeStretch();
    }

    progress -= layerModel.getStartProgress();
    for (int i = layers.size() - 1; i >= 0; i--) {
      layers.get(i).setProgress(progress);
    }
  }

  public boolean hasMasks() {
    if (hasMasks == null) {
      for (int i = layers.size() - 1; i >= 0; i--) {
        BaseLayer layer = layers.get(i);
        if (layer instanceof ShapeLayer) {
          if (layer.hasMasksOnThisLayer()) {
            hasMasks = true;
            return true;
          }
        } else if (layer instanceof CompositionLayer && ((CompositionLayer) layer).hasMasks()) {
          hasMasks = true;
          return  true;
        }
      }
      hasMasks = false;
    }
    return hasMasks;
  }

  public boolean hasMatte() {
    if (hasMatte == null) {
      if (hasMatteOnThisLayer()) {
        hasMatte = true;
        return true;
      }

      for (int i = layers.size() - 1; i >= 0; i--) {
        if (layers.get(i).hasMatteOnThisLayer()) {
          hasMatte = true;
          return true;
        }
      }
      hasMatte = false;
    }
    return hasMatte;
  }

  public String getAllKeyPaths() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < layers.size(); i++) {
      layers.get(i).appendAllKeyPaths(sb, "");
    }
    return sb.toString();
  }
}
