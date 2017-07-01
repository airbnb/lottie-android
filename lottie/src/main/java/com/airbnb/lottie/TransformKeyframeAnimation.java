package com.airbnb.lottie;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.support.annotation.Nullable;

class TransformKeyframeAnimation {
  private final Matrix matrix = new Matrix();
  private final Matrix matrix2 = new Matrix();

  private final BaseKeyframeAnimation<?, PointF> anchorPoint;
  private final BaseKeyframeAnimation<?, PointF> position;
  private final BaseKeyframeAnimation<?, ScaleXY> scale;
  private final BaseKeyframeAnimation<?, Float> rotation;
  private final BaseKeyframeAnimation<?, Integer> opacity;

  // Used for repeaters
  @Nullable private final BaseKeyframeAnimation<?, Float> startOpacity;
  @Nullable private final BaseKeyframeAnimation<?, Float> endOpacity;

  TransformKeyframeAnimation(AnimatableTransform animatableTransform) {
    anchorPoint = animatableTransform.getAnchorPoint().createAnimation();
    position = animatableTransform.getPosition().createAnimation();
    scale = animatableTransform.getScale().createAnimation();
    rotation = animatableTransform.getRotation().createAnimation();
    opacity = animatableTransform.getOpacity().createAnimation();
    if (animatableTransform.getStartOpacity() != null) {
      startOpacity = animatableTransform.getStartOpacity().createAnimation();
    } else {
      startOpacity = null;
    }
    if (animatableTransform.getEndOpacity() != null) {
      endOpacity = animatableTransform.getEndOpacity().createAnimation();
    } else {
      endOpacity = null;
    }
  }

  void addAnimationsToLayer(BaseLayer layer) {
    layer.addAnimation(anchorPoint);
    layer.addAnimation(position);
    layer.addAnimation(scale);
    layer.addAnimation(rotation);
    layer.addAnimation(opacity);
    if (startOpacity != null) {
      layer.addAnimation(startOpacity);
    }
    if (endOpacity != null) {
      layer.addAnimation(endOpacity);
    }
  }

  void addListener(final BaseKeyframeAnimation.AnimationListener listener) {
    anchorPoint.addUpdateListener(listener);
    position.addUpdateListener(listener);
    scale.addUpdateListener(listener);
    rotation.addUpdateListener(listener);
    opacity.addUpdateListener(listener);
    if (startOpacity != null) {
      startOpacity.addUpdateListener(listener);
    }
    if (endOpacity != null) {
      endOpacity.addUpdateListener(listener);
    }
  }

  BaseKeyframeAnimation<?, Integer> getOpacity() {
    return opacity;
  }

  @Nullable public BaseKeyframeAnimation<?, Float> getStartOpacity() {
    return startOpacity;
  }

  @Nullable public BaseKeyframeAnimation<?, Float> getEndOpacity() {
    return endOpacity;
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

  /**
   * TODO: understand why repeaters need a different transform matrix than layers.
   */
  Matrix getMatrixForRepeater() {
    matrix.reset();
    PointF position = this.position.getValue();
    if (position.x != 0 || position.y != 0) {
      matrix.preTranslate(position.x, position.y);
    }

    ScaleXY scaleTransform = this.scale.getValue();
    PointF anchorPoint = this.anchorPoint.getValue();
    matrix2.reset();
    matrix2.preTranslate(anchorPoint.x, anchorPoint.y);
    matrix2.preScale(scaleTransform.getScaleX(), scaleTransform.getScaleY());
    matrix2.preTranslate(-anchorPoint.x, -anchorPoint.y);
    matrix.preConcat(matrix2);

    float rotation = this.rotation.getValue();
    matrix2.reset();
    matrix2.preTranslate(anchorPoint.x, anchorPoint.y);
    matrix2.preRotate(rotation);
    matrix.preConcat(matrix2);

    if (anchorPoint.x != 0 || anchorPoint.y != 0) {
      matrix.preTranslate(-anchorPoint.x, -anchorPoint.y);
    }
    return matrix;
  }
}
