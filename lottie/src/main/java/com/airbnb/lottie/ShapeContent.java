package com.airbnb.lottie;

import android.graphics.Path;
import android.support.annotation.Nullable;

import java.util.List;

class ShapeContent implements PathContent, BaseKeyframeAnimation.AnimationListener {
  private final Path path = new Path();

  private final String name;
  private final LottieDrawable lottieDrawable;
  private final BaseKeyframeAnimation<?, Path> shapeAnimation;

  private boolean isPathValid;
  @Nullable private TrimPathContent trimPath;

  ShapeContent(LottieDrawable lottieDrawable, BaseLayer layer, ShapePath shape) {
    name = shape.getName();
    this.lottieDrawable = lottieDrawable;
    shapeAnimation = shape.getShapePath().createAnimation();
    layer.addAnimation(shapeAnimation);
    shapeAnimation.addUpdateListener(this);
  }

  @Override public void onValueChanged() {
    invalidate();
  }

  private void invalidate() {
    isPathValid = false;
    lottieDrawable.invalidateSelf();
  }

  @Override public void setContents(List<Content> contentsBefore, List<Content> contentsAfter) {
    for (int i = 0; i < contentsBefore.size(); i++) {
      Content content = contentsBefore.get(i);
      if (content instanceof TrimPathContent &&
          ((TrimPathContent) content).getType() == ShapeTrimPath.Type.Simultaneously) {
        // Trim path individually will be handled by the stroke where paths are combined.
        trimPath = (TrimPathContent) content;
        trimPath.addListener(this);
      }
    }
  }

  @Override public Path getPath() {
    if (isPathValid) {
      return path;
    }

    path.reset();

    path.set(shapeAnimation.getValue());
    path.setFillType(Path.FillType.EVEN_ODD);

    Utils.applyTrimPathIfNeeded(path, trimPath);

    isPathValid = true;
    return path;
  }

  @Override public String getName() {
    return name;
  }
}
