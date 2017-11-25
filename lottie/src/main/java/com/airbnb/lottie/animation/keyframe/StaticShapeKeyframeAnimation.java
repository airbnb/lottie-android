package com.airbnb.lottie.animation.keyframe;

import android.graphics.Path;

import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.model.content.ShapeData;

import java.util.Collections;

public class StaticShapeKeyframeAnimation extends ShapeKeyframeAnimation {
  private final Path path;

  public StaticShapeKeyframeAnimation(Path path) {
    super(Collections.<Keyframe<ShapeData>>emptyList());
    this.path = path;
  }

  @Override public Path getValue() {
    return path;
  }
}
