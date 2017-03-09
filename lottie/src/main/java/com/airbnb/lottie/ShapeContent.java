package com.airbnb.lottie;

import android.graphics.Path;

import java.util.List;

class ShapeContent implements Content, PathContent {
  private static final float ELLIPSE_CONTROL_POINT_PERCENTAGE = 0.55228f;

  private final Path path = new Path();
  private boolean isPathValid;

  private final LottieDrawable lottieDrawable;
  private final BaseKeyframeAnimation<?, Path> shapeAnimation;

  ShapeContent(LottieDrawable lottieDrawable, AnimatableLayer layer, ShapePath shape) {
    this.lottieDrawable = lottieDrawable;
    shapeAnimation = shape.getShapePath().createAnimation();
    layer.addAnimation(shapeAnimation);
    shapeAnimation.addUpdateListener(new BaseKeyframeAnimation.AnimationListener<Path>() {
      @Override public void onValueChanged(Path value) {
        invalidate();
      }
    });
  }

  private void invalidate() {
    isPathValid = false;
    lottieDrawable.invalidateSelf();
  }

  @Override public void setContents(List<Content> contentsBefore, List<Content> contentsAfter) {

  }

  @Override public Path getPath() {
    if (isPathValid) {
      return path;
    }

    path.reset();

    path.set(shapeAnimation.getValue());

    isPathValid = false;
    return path;
  }
}
