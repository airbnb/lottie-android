package com.airbnb.lottie;

import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.Nullable;

import java.util.List;

class EllipseContent implements PathContent, BaseKeyframeAnimation.AnimationListener {
  private static final float ELLIPSE_CONTROL_POINT_PERCENTAGE = 0.55228f;

  private final Path path = new Path();

  private final String name;
  private final LottieDrawable lottieDrawable;
  private final BaseKeyframeAnimation<?, PointF> sizeAnimation;
  private final BaseKeyframeAnimation<?, PointF> positionAnimation;

  @Nullable private TrimPathContent trimPath;
  private boolean isPathValid;

  EllipseContent(LottieDrawable lottieDrawable, BaseLayer layer, CircleShape circleShape) {
    name = circleShape.getName();
    this.lottieDrawable = lottieDrawable;
    sizeAnimation = circleShape.getSize().createAnimation();
    positionAnimation = circleShape.getPosition().createAnimation();

    layer.addAnimation(sizeAnimation);
    layer.addAnimation(positionAnimation);

    sizeAnimation.addUpdateListener(this);
    positionAnimation.addUpdateListener(this);
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
        trimPath = (TrimPathContent) content;
        trimPath.addListener(this);
      }
    }
  }

  @Override public String getName() {
    return name;
  }

  @Override public Path getPath() {
    if (isPathValid) {
      return path;
    }

    path.reset();


    PointF size = sizeAnimation.getValue();
    float halfWidth = size.x / 2f;
    float halfHeight = size.y / 2f;
    // TODO: handle bounds

    float cpW = halfWidth * ELLIPSE_CONTROL_POINT_PERCENTAGE;
    float cpH = halfHeight * ELLIPSE_CONTROL_POINT_PERCENTAGE;

    path.reset();
    path.moveTo(0, -halfHeight);
    path.cubicTo(0 + cpW, -halfHeight, halfWidth, 0 - cpH, halfWidth, 0);
    path.cubicTo(halfWidth, 0 + cpH, 0 + cpW, halfHeight, 0, halfHeight);
    path.cubicTo(0 - cpW, halfHeight, -halfWidth, 0 + cpH, -halfWidth, 0);
    path.cubicTo(-halfWidth, 0 - cpH, 0 - cpW, -halfHeight, 0, -halfHeight);

    PointF position = positionAnimation.getValue();
    path.offset(position.x, position.y);

    path.close();

    Utils.applyTrimPathIfNeeded(path, trimPath);

    isPathValid = true;
    return path;
  }
}
