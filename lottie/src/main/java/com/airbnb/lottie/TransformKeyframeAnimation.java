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

  void addListener(final BaseKeyframeAnimation.AnimationListener<Void> listener) {
    anchorPoint.addUpdateListener(new BaseKeyframeAnimation.AnimationListener<PointF>() {
     @Override public void onValueChanged(PointF value) {
       listener.onValueChanged(null);
     }
    });
    position.addUpdateListener(new BaseKeyframeAnimation.AnimationListener<PointF>() {
      @Override public void onValueChanged(PointF value) {
        listener.onValueChanged(null);
      }
    });
    scale.addUpdateListener(new BaseKeyframeAnimation.AnimationListener<ScaleXY>() {
      @Override public void onValueChanged(ScaleXY value) {
        listener.onValueChanged(null);
      }
    });
    rotation.addUpdateListener(new BaseKeyframeAnimation.AnimationListener<Float>() {
      @Override public void onValueChanged(Float value) {
        listener.onValueChanged(null);
      }
    });
    opacity.addUpdateListener(new BaseKeyframeAnimation.AnimationListener<Integer>() {
      @Override public void onValueChanged(Integer value) {
        listener.onValueChanged(null);
      }
    });
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
