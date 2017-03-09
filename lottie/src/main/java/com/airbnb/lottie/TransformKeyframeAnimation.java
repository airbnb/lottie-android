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

  BaseKeyframeAnimation<?, PointF> getAnchorPoint() {
    return anchorPoint;
  }

  BaseKeyframeAnimation<?, PointF> getPosition() {
    return position;
  }

  BaseKeyframeAnimation<?, ScaleXY> getScale() {
    return scale;
  }

  BaseKeyframeAnimation<?, Float> getRotation() {
    return rotation;
  }

  BaseKeyframeAnimation<?, Integer> getOpacity() {
    return opacity;
  }

  void addListener(final BaseKeyframeAnimation.AnimationListener<Void> listener) {
    getAnchorPoint().addUpdateListener(new BaseKeyframeAnimation.AnimationListener<PointF>() {
     @Override public void onValueChanged(PointF value) {
       listener.onValueChanged(null);
     }
    });
    getPosition().addUpdateListener(new BaseKeyframeAnimation.AnimationListener<PointF>() {
      @Override public void onValueChanged(PointF value) {
        listener.onValueChanged(null);
      }
    });
    getScale().addUpdateListener(new BaseKeyframeAnimation.AnimationListener<ScaleXY>() {
      @Override public void onValueChanged(ScaleXY value) {
        listener.onValueChanged(null);
      }
    });
    getRotation().addUpdateListener(new BaseKeyframeAnimation.AnimationListener<Float>() {
      @Override public void onValueChanged(Float value) {
        listener.onValueChanged(null);
      }
    });
    getOpacity().addUpdateListener(new BaseKeyframeAnimation.AnimationListener<Integer>() {
      @Override public void onValueChanged(Integer value) {
        listener.onValueChanged(null);
      }
    });
  }

  Matrix getMatrix(LottieDrawable drawable) {
    matrix.reset();
    float scale = drawable.getScale();

    PointF position = getPosition().getValue();
    if (position.x != 0 || position.y != 0) {
      matrix.preTranslate(position.x * scale, position.y * scale);
    }

    float rotation = getRotation().getValue();
    if (rotation != 0f) {
      matrix.preRotate(rotation);
    }

    ScaleXY scaleTransform = getScale().getValue();
    if (scaleTransform.getScaleX() != 1f || scaleTransform.getScaleY() != 1f) {
      matrix.preScale(scaleTransform.getScaleX(), scaleTransform.getScaleY());
    }

    PointF anchorPoint = getAnchorPoint().getValue();
    if (anchorPoint.x != 0 || anchorPoint.y != 0) {
      matrix.preTranslate(-anchorPoint.x * scale, -anchorPoint.y * scale);
    }
    return matrix;
  }
}
