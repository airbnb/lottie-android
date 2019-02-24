package com.airbnb.lottie.animation.content;

import android.graphics.Path;

import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.model.content.ShapePath;
import com.airbnb.lottie.model.content.ShapeTrimPath;
import com.airbnb.lottie.model.layer.BaseLayer;

import java.util.List;

public class ShapeContent implements PathContent, BaseKeyframeAnimation.AnimationListener {
  private final Path path = new Path();

  private final String name;
  private final boolean hidden;
  private final LottieDrawable lottieDrawable;
  private final BaseKeyframeAnimation<?, Path> shapeAnimation;

  private boolean isPathValid;
  private CompoundTrimPathContent trimPaths = new CompoundTrimPathContent();

  public ShapeContent(LottieDrawable lottieDrawable, BaseLayer layer, ShapePath shape) {
    name = shape.getName();
    hidden = shape.isHidden();
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
          ((TrimPathContent) content).getType() == ShapeTrimPath.Type.SIMULTANEOUSLY) {
        // Trim path individually will be handled by the stroke where paths are combined.
        TrimPathContent trimPath = (TrimPathContent) content;
        trimPaths.addTrimPath(trimPath);
        trimPath.addListener(this);
      }
    }
  }

  @Override public Path getPath() {
    if (isPathValid) {
      return path;
    }

    path.reset();

    if (hidden) {
      isPathValid = true;
      return path;
    }

    path.set(shapeAnimation.getValue());
    path.setFillType(Path.FillType.EVEN_ODD);

    trimPaths.apply(path);

    isPathValid = true;
    return path;
  }

  @Override public String getName() {
    return name;
  }
}
