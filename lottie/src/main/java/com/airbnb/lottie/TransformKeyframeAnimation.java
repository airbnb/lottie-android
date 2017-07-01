package com.airbnb.lottie;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.support.annotation.Nullable;

class TransformKeyframeAnimation {
  private final Matrix matrix = new Matrix();
  private final Matrix matrix2 = new Matrix();
  private final Matrix matrix3 = new Matrix();
  private final Matrix matrix4 = new Matrix();

  private final KeyframeAnimation<PointF> anchorPoint;
  private final BaseKeyframeAnimation<?, PointF> position;
  private final KeyframeAnimation<ScaleXY> scale;
  private final KeyframeAnimation<Float> rotation;
  private final KeyframeAnimation<Integer> opacity;

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

  Matrix getMatrixForRepeater(float count, float offset) {
    matrix4.set(getMatrixForRepeater(offset));
    matrix4.preConcat(getMatrixForRepeater(count));
    return matrix4;
  }

  private Matrix getMatrixForRepeater(float count) {
    Matrix singleMatrix = getSingleMatrixForRepeater(1f);
    matrix3.reset();
    // matrix2.reset();
    PointF anchorPoint = this.anchorPoint.getValue();
    // matrix3.preTranslate(anchorPoint.x, anchorPoint.y);
    float rotation = this.rotation.getValue() * count;
    // matrix3.setRotate(rotation, anchorPoint.x, anchorPoint.y);
    // matrix3.preRotate(rotation);
    // matrix3.preTranslate(anchorPoint.x, anchorPoint.y);
    // matrix3.preConcat(matrix2);

    while (count > 0) {
      float amount = Math.min(count, 1f);
      if (amount == 1f) {
        matrix3.preConcat(singleMatrix);
      } else {
        matrix3.preConcat(getSingleMatrixForRepeater(amount));
      }
      count -= amount;
    }
    return matrix3;
  }

  /**
   * TODO: understand why repeaters need a different transform matrix than layers.
   */
  private Matrix getSingleMatrixForRepeater(float amount) {
    matrix.reset();
    PointF position = this.position.getValue();
    if (position.x != 0 || position.y != 0) {
      matrix.preTranslate(position.x * amount, position.y * amount);
    }

    PointF anchorPoint = this.anchorPoint.getValue();
    ScaleXY scale = this.scale.getValue();
    matrix.preScale(
        1 + (scale.getScaleX() - 1) * amount,
        1 + (scale.getScaleY() - 1) * amount);
    float rotation = this.rotation.getValue() * amount;
    matrix.setRotate(rotation, anchorPoint.x, anchorPoint.y);
    return matrix;
  }
}
