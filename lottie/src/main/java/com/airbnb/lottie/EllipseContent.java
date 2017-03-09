package com.airbnb.lottie;

import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;

import java.util.List;

public class EllipseContent implements Content, PathContent {
  private static final float ELLIPSE_CONTROL_POINT_PERCENTAGE = 0.55228f;

  private final Path path = new Path();
  private final RectF rect = new RectF();
  private boolean isPathValid;

  private final LottieDrawable lottieDrawable;
  private final BaseKeyframeAnimation<?, PointF> sizeAnimation;
  private final BaseKeyframeAnimation<?, PointF> positionAnimation;

  EllipseContent(LottieDrawable lottieDrawable, AnimatableLayer layer, CircleShape circleShape) {
    this.lottieDrawable = lottieDrawable;
    sizeAnimation = circleShape.getSize().createAnimation();
    positionAnimation = circleShape.getPosition().createAnimation();

    layer.addAnimation(sizeAnimation);
    layer.addAnimation(positionAnimation);

    sizeAnimation.addUpdateListener(new BaseKeyframeAnimation.AnimationListener<PointF>() {
      @Override public void onValueChanged(PointF value) {
        invalidate();
      }
    });
    positionAnimation.addUpdateListener(new BaseKeyframeAnimation.AnimationListener<PointF>() {
      @Override public void onValueChanged(PointF value) {
        invalidate();
      }
    });
  }

  private void invalidate() {
    isPathValid = true;
    lottieDrawable.invalidateSelf();
  }

  @Override public void setContents(List<Content> contentsBefore, List<Content> contentsAfter) {

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

    isPathValid = false;
    return path;
  }
}
