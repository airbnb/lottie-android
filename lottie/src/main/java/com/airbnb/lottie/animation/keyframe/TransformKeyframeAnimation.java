package com.airbnb.lottie.animation.keyframe;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.support.annotation.Nullable;

import com.airbnb.lottie.model.ScaleXY;
import com.airbnb.lottie.model.animatable.AnimatableTransform;
import com.airbnb.lottie.model.layer.BaseLayer;

public class TransformKeyframeAnimation {
  private final Matrix matrix = new Matrix();

  private final BaseKeyframeAnimation<PointF, PointF> anchorPoint;
  private final BaseKeyframeAnimation<?, PointF> position;
  private final BaseKeyframeAnimation<ScaleXY, ScaleXY> scale;
  private final BaseKeyframeAnimation<Float, Float> rotation;
  private final BaseKeyframeAnimation<Integer, Integer> opacity;

  // Used for repeaters
  @Nullable private final BaseKeyframeAnimation<?, Float> startOpacity;
  @Nullable private final BaseKeyframeAnimation<?, Float> endOpacity;

  public TransformKeyframeAnimation(AnimatableTransform animatableTransform) {
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

  public void addAnimationsToLayer(BaseLayer layer) {
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

  public void addListener(final BaseKeyframeAnimation.AnimationListener listener) {
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

  public void setProgress(float progress) {
    anchorPoint.setProgress(progress);
    position.setProgress(progress);
    scale.setProgress(progress);
    rotation.setProgress(progress);
    opacity.setProgress(progress);
    if (startOpacity != null) {
      startOpacity.setProgress(progress);
    }
    if (endOpacity != null) {
      endOpacity.setProgress(progress);
    }
  }

  public BaseKeyframeAnimation<?, Integer> getOpacity() {
    return opacity;
  }

  @Nullable public BaseKeyframeAnimation<?, Float> getStartOpacity() {
    return startOpacity;
  }

  @Nullable public BaseKeyframeAnimation<?, Float> getEndOpacity() {
    return endOpacity;
  }


  public Matrix getMatrix() {
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
   * TODO: see if we can use this for the main {@link #getMatrix()} method.
   */
  public Matrix getMatrixForRepeater(float amount) {
    PointF position = this.position.getValue();
    PointF anchorPoint = this.anchorPoint.getValue();
    ScaleXY scale = this.scale.getValue();
    float rotation = this.rotation.getValue();

    matrix.reset();
    matrix.preTranslate(position.x * amount, position.y * amount);
    matrix.preScale(
        (float) Math.pow(scale.getScaleX(), amount),
         (float) Math.pow(scale.getScaleY(), amount));
    matrix.preRotate(rotation * amount, anchorPoint.x, anchorPoint.y);

    return matrix;
  }

}
