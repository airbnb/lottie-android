package com.airbnb.lottie;

import android.graphics.Path;
import android.support.annotation.Nullable;

import java.util.List;

class ShapeContent implements Content, PathContent {
  private final Path path = new Path();

  private final LottieDrawable lottieDrawable;
  private final BaseKeyframeAnimation<?, Path> shapeAnimation;

  private boolean isPathValid;
  @Nullable private TrimPathContent trimPath;

  ShapeContent(LottieDrawable lottieDrawable, BaseLayer layer, ShapePath shape) {
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
    for (int i = 0; i < contentsBefore.size(); i++) {
      Content content = contentsBefore.get(i);
      if (content instanceof TrimPathContent) {
        trimPath = (TrimPathContent) content;
        trimPath.addListener(new BaseKeyframeAnimation.SimpleAnimationListener() {
          @Override public void onValueChanged() {
            lottieDrawable.invalidateSelf();
          }
        });
      }
    }
  }

  @Override public Path getPath() {
    if (isPathValid) {
      return path;
    }

    path.reset();

    path.set(shapeAnimation.getValue());

    Utils.applyTrimPathIfNeeded(path, trimPath);

    isPathValid = false;
    return path;
  }
}
