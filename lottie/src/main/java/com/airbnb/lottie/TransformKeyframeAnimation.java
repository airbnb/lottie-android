package com.airbnb.lottie;

import android.graphics.Matrix;
import android.graphics.PointF;

class TransformKeyframeAnimation {
  private final Matrix matrix = new Matrix();

  private final BaseKeyframeAnimation<?, PointF> anchorPoint;
  private final BaseKeyframeAnimation<?, PointF> position;
  private final BaseKeyframeAnimation<?, ScaleXY> scale;
  private final BaseKeyframeAnimation<?, Float> rotation;
  private final BaseKeyframeAnimation<?, Integer> opacity;

  TransformKeyframeAnimation(AnimatableTransform animatableTransform) {
    anchorPoint = animatableTransform.getAnchorPoint().createAnimation();
    position = animatableTransform.getPosition().createAnimation();
    scale = animatableTransform.getScale().createAnimation();
    rotation = animatableTransform.getRotation().createAnimation();
    opacity = animatableTransform.getOpacity().createAnimation();
  }

  void addAnimationsToLayer(BaseLayer layer) {
    layer.addAnimation(anchorPoint);
    layer.addAnimation(position);
    layer.addAnimation(scale);
    layer.addAnimation(rotation);
    layer.addAnimation(opacity);
  }

  void addListener(final BaseKeyframeAnimation.AnimationListener listener) {
    anchorPoint.addUpdateListener(listener);
    position.addUpdateListener(listener);
    scale.addUpdateListener(listener);
    rotation.addUpdateListener(listener);
    opacity.addUpdateListener(listener);
  }

  BaseKeyframeAnimation<?, Integer> getOpacity() {
    return opacity;
  }

  Matrix getMatrix() {
    matrix.reset();
    PointF position = this.position.getValue();
    if (position.x != 0 || position.y != 0) {
      matrix.preTranslate(position.x, position.y);
    }

    float rotation = this.rotation.getValue();
    if (rotation != 0f) {
      matrix.preRotate(rotation);
    }

    ScaleXY scaleTransform = this.scale.getValue();
    if (scaleTransform.getScaleX() != 1f || scaleTransform.getScaleY() != 1f) {
      matrix.preScale(scaleTransform.getScaleX(), scaleTransform.getScaleY());
    }

    PointF anchorPoint = this.anchorPoint.getValue();
    if (anchorPoint.x != 0 || anchorPoint.y != 0) {
      matrix.preTranslate(-anchorPoint.x, -anchorPoint.y);
    }
    return matrix;
  }
}
