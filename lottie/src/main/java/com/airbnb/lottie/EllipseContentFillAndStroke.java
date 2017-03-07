package com.airbnb.lottie;

import android.graphics.Path;
import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

class EllipseContentFillAndStroke extends AnimatableLayer {
  EllipseContentFillAndStroke(CircleShape circleShape, ShapeFill fill, ShapeStroke stroke,
      ShapeTrimPath trim, AnimatableTransform transform, LottieDrawable lottieDrawable) {
    super(lottieDrawable);

    setTransform(transform.createAnimation());

    if (fill != null) {
      EllipseContent fillLayer = new EllipseContent(lottieDrawable);
      //noinspection ConstantConditions
      fillLayer.setColor(fill.getColor().createAnimation());
      fillLayer.setTransformOpacity(transform.getOpacity().createAnimation());
      //noinspection ConstantConditions
      fillLayer.setShapeOpacity(fill.getOpacity().createAnimation());
      fillLayer.updateCircle(
          circleShape.getPosition().createAnimation(),
          circleShape.getSize().createAnimation());
      if (trim != null) {
        fillLayer.setTrimPath(trim.getStart().createAnimation(), trim.getEnd().createAnimation(),
            trim.getOffset().createAnimation());
      }
      addLayer(fillLayer);
    }

    if (stroke != null) {
      EllipseContent strokeLayer = new EllipseContent(lottieDrawable);
      strokeLayer.setIsStroke();
      strokeLayer.setColor(stroke.getColor().createAnimation());
      strokeLayer.setTransformOpacity(stroke.getOpacity().createAnimation());
      strokeLayer.setShapeOpacity(stroke.getOpacity().createAnimation());
      strokeLayer.setLineWidth(stroke.getWidth().createAnimation());
      if (!stroke.getLineDashPattern().isEmpty()) {
        List<BaseKeyframeAnimation<?, Float>> dashPatternAnimations =
            new ArrayList<>(stroke.getLineDashPattern().size());
        for (AnimatableFloatValue dashPattern : stroke.getLineDashPattern()) {
          dashPatternAnimations.add(dashPattern.createAnimation());
        }
        strokeLayer.setDashPattern(dashPatternAnimations, stroke.getDashOffset().createAnimation());
      }
      strokeLayer.setLineCapType(stroke.getCapType());
      strokeLayer.updateCircle(
          circleShape.getPosition().createAnimation(),
          circleShape.getSize().createAnimation());
      if (trim != null) {
        strokeLayer.setTrimPath(trim.getStart().createAnimation(), trim.getEnd().createAnimation(),
            trim.getOffset().createAnimation());
      }

      addLayer(strokeLayer);
    }
  }

  private static final class EllipseContent extends ShapeContent {
    private static final float ELLIPSE_CONTROL_POINT_PERCENTAGE = 0.55228f;

    private final KeyframeAnimation.AnimationListener<PointF> circleSizeChangedListener =
        new KeyframeAnimation.AnimationListener<PointF>() {
          @Override
          public void onValueChanged(PointF value) {
            onCircleSizeChanged();
          }
        };

    private final KeyframeAnimation.AnimationListener<PointF> circlePositionChangedListener =
        new KeyframeAnimation.AnimationListener<PointF>() {
          @Override
          public void onValueChanged(PointF value) {
            invalidateSelf();
          }
        };

    private final Path path = new Path();

    private BaseKeyframeAnimation<?, PointF> circleSize;
    private BaseKeyframeAnimation<?, PointF> circlePosition;

    EllipseContent(LottieDrawable lottieDrawable) {
      super(lottieDrawable);
      setPath(new StaticKeyframeAnimation<>(path));
    }

    void updateCircle(BaseKeyframeAnimation<?, PointF> circlePosition,
        BaseKeyframeAnimation<?, PointF> circleSize) {
      if (this.circleSize != null) {
        removeAnimation(this.circleSize);
        this.circleSize.removeUpdateListener(circleSizeChangedListener);
      }
      if (this.circlePosition != null) {
        removeAnimation(this.circlePosition);
        this.circlePosition.removeUpdateListener(circlePositionChangedListener);
      }
      this.circleSize = circleSize;
      this.circlePosition = circlePosition;
      addAnimation(circleSize);
      circleSize.addUpdateListener(circleSizeChangedListener);
      addAnimation(circlePosition);
      circlePosition.addUpdateListener(circlePositionChangedListener);
      onCircleSizeChanged();
    }

    private void onCircleSizeChanged() {
      float halfWidth = circleSize.getValue().x / 2f;
      float halfHeight = circleSize.getValue().y / 2f;
      // TODO: handle bounds

      float cpW = halfWidth * ELLIPSE_CONTROL_POINT_PERCENTAGE;
      float cpH = halfHeight * ELLIPSE_CONTROL_POINT_PERCENTAGE;

      path.reset();
      path.moveTo(0, -halfHeight);
      path.cubicTo(0 + cpW, -halfHeight, halfWidth, 0 - cpH, halfWidth, 0);
      path.cubicTo(halfWidth, 0 + cpH, 0 + cpW, halfHeight, 0, halfHeight);
      path.cubicTo(0 - cpW, halfHeight, -halfWidth, 0 + cpH, -halfWidth, 0);
      path.cubicTo(-halfWidth, 0 - cpH, 0 - cpW, -halfHeight, 0, -halfHeight);

      path.offset(circlePosition.getValue().x, circlePosition.getValue().y);

      path.close();

      onPathChanged();
    }
  }
}
