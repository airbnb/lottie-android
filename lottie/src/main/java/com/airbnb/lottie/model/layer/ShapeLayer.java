package com.airbnb.lottie.model.layer;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.ContentGroup;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.model.content.BlurEffect;
import com.airbnb.lottie.model.content.ShapeGroup;
import com.airbnb.lottie.parser.DropShadowEffect;

import java.util.Collections;
import java.util.List;

public class ShapeLayer extends BaseLayer {
  private final ContentGroup contentGroup;
  private final CompositionLayer compositionLayer;

  ShapeLayer(LottieDrawable lottieDrawable, Layer layerModel, CompositionLayer compositionLayer, LottieComposition composition) {
    super(lottieDrawable, compositionLayer, layerModel);
    this.compositionLayer = compositionLayer;

    // Naming this __container allows it to be ignored in KeyPath matching.
    ShapeGroup shapeGroup = new ShapeGroup("__container", layerModel.getShapes(), false);
    contentGroup = new ContentGroup(lottieDrawable, this, shapeGroup, composition);
    contentGroup.setContents(Collections.<Content>emptyList(), Collections.<Content>emptyList());
  }

  @Override void updateDirtyNodes() {
    super.updateDirtyNodes();
  }

  @Override void drawLayer(@NonNull Canvas canvas, Matrix parentMatrix, int parentAlpha) {
    contentGroup.draw(canvas, parentMatrix, parentAlpha);
  }

  @Override public void getBounds(RectF outBounds, Matrix parentMatrix, boolean applyParents) {
    super.getBounds(outBounds, parentMatrix, applyParents);
    contentGroup.getBounds(outBounds, boundsMatrix, applyParents);
  }

  @Nullable @Override public BlurEffect getBlurEffect() {
    BlurEffect layerBlur = super.getBlurEffect();
    if (layerBlur != null) {
      return layerBlur;
    }
    return compositionLayer.getBlurEffect();
  }

  @Nullable @Override public DropShadowEffect getDropShadowEffect() {
    DropShadowEffect layerDropShadow = super.getDropShadowEffect();
    if (layerDropShadow != null) {
      return layerDropShadow;
    }
    return compositionLayer.getDropShadowEffect();
  }

  @Override
  protected void resolveChildKeyPath(KeyPath keyPath, int depth, List<KeyPath> accumulator,
      KeyPath currentPartialKeyPath) {
    contentGroup.resolveKeyPath(keyPath, depth, accumulator, currentPartialKeyPath);
  }
}
