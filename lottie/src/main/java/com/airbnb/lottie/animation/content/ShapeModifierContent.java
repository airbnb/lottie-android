package com.airbnb.lottie.animation.content;

import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.model.content.ShapeData;

public interface ShapeModifierContent extends Content {
  void addUpdateListener(BaseKeyframeAnimation.AnimationListener listener);
  ShapeData modifyShape(ShapeData shapeData);
}
