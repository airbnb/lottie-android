package com.airbnb.lottie.model.layer;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.ContentGroup;
import com.airbnb.lottie.animation.keyframe.BlurKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.DropShadowKeyframeAnimation;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.model.content.BlurEffect;
import com.airbnb.lottie.model.content.ShapeGroup;
import com.airbnb.lottie.utils.DropShadow;
import com.airbnb.lottie.value.LottieValueCallback;

import java.util.Collections;
import java.util.List;

public class ShapeLayer extends BaseLayer {
  private final ContentGroup contentGroup;
  private final CompositionLayer compositionLayer;

  @Nullable private DropShadowKeyframeAnimation dropShadowAnimation;
  @Nullable private BlurKeyframeAnimation blurAnimation;

  ShapeLayer(LottieDrawable lottieDrawable, Layer layerModel, CompositionLayer compositionLayer, LottieComposition composition) {
    super(lottieDrawable, layerModel);
    this.compositionLayer = compositionLayer;

    // Naming this __container allows it to be ignored in KeyPath matching.
    ShapeGroup shapeGroup = new ShapeGroup("__container", layerModel.getShapes(), false);
    contentGroup = new ContentGroup(lottieDrawable, this, shapeGroup, composition);
    contentGroup.setContents(Collections.<Content>emptyList(), Collections.<Content>emptyList());

    if (getDropShadowEffect() != null) {
      dropShadowAnimation = new DropShadowKeyframeAnimation(this, this, getDropShadowEffect());
    }

    if (getBlurEffect() != null) {
      blurAnimation = new BlurKeyframeAnimation(this, this, getBlurEffect());
    }
  }

  @Override void drawLayer(@NonNull Canvas canvas, Matrix parentMatrix, int parentAlpha, @Nullable DropShadow parentShadowToApply, float parentBlurToApply) {
    // If a parent composition layer has a shadow and we have one too, prioritize our own.
    DropShadow shadowToApply = dropShadowAnimation != null
        ? dropShadowAnimation.evaluate(parentMatrix, parentAlpha)
        : parentShadowToApply;
    float blurToApply = parentBlurToApply +
        (blurAnimation != null ? blurAnimation.evaluate(parentMatrix) : 0.0f);
    contentGroup.draw(canvas, parentMatrix, parentAlpha, shadowToApply, blurToApply);
  }

  @Override public void getBounds(RectF outBounds, Matrix parentMatrix, boolean applyParents) {
    super.getBounds(outBounds, parentMatrix, applyParents);
    contentGroup.getBounds(outBounds, boundsMatrix, applyParents);
  }

  @Override
  protected void resolveChildKeyPath(KeyPath keyPath, int depth, List<KeyPath> accumulator,
      KeyPath currentPartialKeyPath) {
    contentGroup.resolveKeyPath(keyPath, depth, accumulator, currentPartialKeyPath);
  }

  @Override
  @CallSuper
  public <T> void addValueCallback(T property, @Nullable LottieValueCallback<T> callback) {
    super.addValueCallback(property, callback);
    if (property == LottieProperty.DROP_SHADOW_COLOR && dropShadowAnimation != null) {
      dropShadowAnimation.setColorCallback((LottieValueCallback<Integer>) callback);
    } else if (property == LottieProperty.DROP_SHADOW_OPACITY && dropShadowAnimation != null) {
      dropShadowAnimation.setOpacityCallback((LottieValueCallback<Float>) callback);
    } else if (property == LottieProperty.DROP_SHADOW_DIRECTION && dropShadowAnimation != null) {
      dropShadowAnimation.setDirectionCallback((LottieValueCallback<Float>) callback);
    } else if (property == LottieProperty.DROP_SHADOW_DISTANCE && dropShadowAnimation != null) {
      dropShadowAnimation.setDistanceCallback((LottieValueCallback<Float>) callback);
    } else if (property == LottieProperty.DROP_SHADOW_RADIUS && dropShadowAnimation != null) {
      dropShadowAnimation.setRadiusCallback((LottieValueCallback<Float>) callback);
    } else if (property == LottieProperty.BLUR_RADIUS && blurAnimation != null) {
      blurAnimation.setBlurrinessCallback((LottieValueCallback<Float>) callback);
    }
  }
}
