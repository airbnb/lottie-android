package com.airbnb.lottie;

import android.graphics.Path;
import android.graphics.PathMeasure;
import android.support.annotation.Nullable;

import java.util.List;

class ShapeContent implements Content, PathContent {
  private final PathMeasure pathMeasure = new PathMeasure();
  private final Path tempPath = new Path();
  private final Path tempPath2 = new Path();
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

    applyTrimPathIfNeeded();

    isPathValid = false;
    return path;
  }

  private void applyTrimPathIfNeeded() {
    if (trimPath == null) {
      return;
    }

    pathMeasure.setPath(path, false);

    float length = pathMeasure.getLength();
    float start = length * trimPath.getStart().getValue() / 100f;
    float end = length * trimPath.getEnd().getValue() / 100f;
    float newStart = Math.min(start, end);
    float newEnd = Math.max(start, end);

    float offset = trimPath.getOffset().getValue() / 360f * length;
    newStart += offset;
    newEnd += offset;

    // If the trim path has rotated around the path, we need to shift it back.
    if (newStart > length && newEnd > length) {
      newStart %= length;
      newEnd %= length;
    }
    if (newStart > newEnd) {
      newStart -= length;
    }

    tempPath.reset();
    pathMeasure.getSegment(
        newStart,
        newEnd,
        tempPath,
        true);

    if (newEnd > length) {
      tempPath2.reset();
      pathMeasure.getSegment(
          0,
          newEnd % length,
          tempPath2,
          true);
      tempPath.addPath(tempPath2);
    } else if (newStart < 0) {
      tempPath2.reset();
      pathMeasure.getSegment(
          length + newStart,
          length,
          tempPath2,
          true);
      tempPath.addPath(tempPath2);
    }
    path.set(tempPath);
  }
}
