package com.airbnb.lottie;

class TransformKeyframeAnimation {

  private final BaseKeyframeAnimation<?, CPointF> anchorPoint;
  private final BaseKeyframeAnimation<?, CPointF> position;
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

  BaseKeyframeAnimation<?, CPointF> getAnchorPoint() {
    return anchorPoint;
  }

  BaseKeyframeAnimation<?, CPointF> getPosition() {
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
