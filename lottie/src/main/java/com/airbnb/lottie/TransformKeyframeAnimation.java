package com.airbnb.lottie;

import android.graphics.PointF;

class TransformKeyframeAnimation {

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
}
