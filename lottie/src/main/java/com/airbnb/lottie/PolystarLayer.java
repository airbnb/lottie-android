package com.airbnb.lottie;

import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

class PolystarLayer extends AnimatableLayer {
  PolystarLayer(PolystarShape polystarShape, ShapeFill fill, ShapeStroke stroke,
      ShapeTrimPath trim, Transform transform, Drawable.Callback callback) {
    super(callback);

    setBounds(transform.getBounds());
    setAnchorPoint(transform.getAnchor().createAnimation());
    setAlpha(transform.getOpacity().createAnimation());
    setPosition(transform.getPosition().createAnimation());
    setTransform(transform.getScale().createAnimation());
    setRotation(transform.getRotation().createAnimation());

    if (fill != null) {
      PolystarShapeLayer fillLayer = new PolystarShapeLayer(getCallback());
      fillLayer.setColor(fill.getColor().createAnimation());
      fillLayer.setAlpha(fill.getOpacity().createAnimation());
      fillLayer.updateCircle(polystarShape);
      if (trim != null) {
        fillLayer.setTrimPath(trim.getStart().createAnimation(), trim.getEnd().createAnimation(),
            trim.getOffset().createAnimation());
      }
      addLayer(fillLayer);
    }

    if (stroke != null) {
      PolystarShapeLayer strokeLayer = new PolystarShapeLayer(getCallback());
      strokeLayer.setIsStroke();
      strokeLayer.setColor(stroke.getColor().createAnimation());
      strokeLayer.setAlpha(stroke.getOpacity().createAnimation());
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
      strokeLayer.updateCircle(polystarShape);
      if (trim != null) {
        strokeLayer.setTrimPath(trim.getStart().createAnimation(), trim.getEnd().createAnimation(),
            trim.getOffset().createAnimation());
      }

      addLayer(strokeLayer);
    }
  }

  private static final class PolystarShapeLayer extends ShapeLayer {

    private final KeyframeAnimation.AnimationListener<PointF> pointChangedListener =
        new KeyframeAnimation.AnimationListener<PointF>() {
          @Override
          public void onValueChanged(PointF value) {
            onPolystarChanged();
          }
        };

    private final KeyframeAnimation.AnimationListener<Float> floatChangedListener =
        new KeyframeAnimation.AnimationListener<Float>() {
          @Override
          public void onValueChanged(Float value) {
            onPolystarChanged();
          }
        };

    private final Path path = new Path();

    private PolystarShape.Type type;
    private BaseKeyframeAnimation<?, Float> pointsAnimation;
    private BaseKeyframeAnimation<?, PointF> positionAnimation;
    private BaseKeyframeAnimation<?, Float> rotationAnimation;
    private BaseKeyframeAnimation<?, Float> outerRadiusAnimation;
    private BaseKeyframeAnimation<?, Float> outerRoundednessAnimation;
    private BaseKeyframeAnimation<?, Float> innerRadiusAnimation;
    private BaseKeyframeAnimation<?, Float> innerRoundednessAnimation;

    PolystarShapeLayer(Drawable.Callback callback) {
      super(callback);
      setPath(new StaticKeyframeAnimation<>(path));
    }

    void updateCircle(PolystarShape polystarShape) {
      type = polystarShape.getType();

      if (pointsAnimation != null) {
        removeAnimation(pointsAnimation);
      }

      if (positionAnimation != null) {
        removeAnimation(positionAnimation);
      }

      if (rotationAnimation != null) {
        removeAnimation(rotationAnimation);
      }

      if (outerRadiusAnimation != null) {
        removeAnimation(outerRadiusAnimation);
      }

      if (outerRoundednessAnimation != null) {
        removeAnimation(outerRoundednessAnimation);
      }

      if (innerRadiusAnimation != null) {
        removeAnimation(innerRadiusAnimation);
      }

      if (innerRoundednessAnimation != null) {
        removeAnimation(innerRoundednessAnimation);
      }
      pointsAnimation = polystarShape.getPoints().createAnimation();
      positionAnimation = polystarShape.getPosition().createAnimation();
      rotationAnimation = polystarShape.getRotation().createAnimation();
      outerRadiusAnimation = polystarShape.getOuterRadius().createAnimation();
      outerRoundednessAnimation = polystarShape.getOuterRoundedness().createAnimation();
      innerRadiusAnimation = polystarShape.getInnerRadius().createAnimation();
      innerRoundednessAnimation = polystarShape.getInnerRoundedness().createAnimation();

      pointsAnimation.addUpdateListener(floatChangedListener);
      positionAnimation.addUpdateListener(pointChangedListener);
      rotationAnimation.addUpdateListener(floatChangedListener);
      outerRadiusAnimation.addUpdateListener(floatChangedListener);
      outerRoundednessAnimation.addUpdateListener(floatChangedListener);
      innerRadiusAnimation.addUpdateListener(floatChangedListener);
      innerRoundednessAnimation.addUpdateListener(floatChangedListener);

      addAnimation(pointsAnimation);
      addAnimation(positionAnimation);
      addAnimation(rotationAnimation);
      addAnimation(outerRadiusAnimation);
      addAnimation(outerRoundednessAnimation);
      addAnimation(innerRadiusAnimation);
      addAnimation(innerRoundednessAnimation);
      onPolystarChanged();
    }

    private void onPolystarChanged() {
      switch (type) {
        case Star:
          createStarPath();
          break;
        case Polygon:
          createPolygonPath();
          break;
      }
      onPathChanged();
    }

    private void createStarPath() {
      float points = pointsAnimation.getValue();
      double currentAngle = rotationAnimation == null ? 0f : rotationAnimation.getValue();
      // Start at +y instead of +9
      currentAngle -= 90;
      // convert to radians
      currentAngle = Math.toRadians(currentAngle);
      // adjust current angle for partial points
      double anglePerPoint = 2 * Math.PI / (double) points;
      double halfAnglePerPoint = anglePerPoint / 2.0;
      float partialPointAmount = points - (int) points;
      if (partialPointAmount != 0) {
        currentAngle += halfAnglePerPoint * (1f - partialPointAmount);
      }

      double outerRadius = outerRadiusAnimation.getValue();
      double innerRadius = innerRadiusAnimation.getValue();

      path.reset();


      double partialPointRadius = 0;
      if (partialPointAmount != 0) {
        partialPointRadius = innerRadius + partialPointAmount * (outerRadius - innerRadius);
        path.moveTo((float) (partialPointRadius * Math.cos(currentAngle)),
            (float) (partialPointRadius * Math.sin(currentAngle)));
        currentAngle += halfAnglePerPoint * partialPointAmount;
      } else {
        path.moveTo((float) (outerRadius * Math.cos(currentAngle)),
            (float) (outerRadius * Math.sin(currentAngle)));
        currentAngle += halfAnglePerPoint;
      }

      for (int i = 0; i < Math.floor(points); i++) {
        path.lineTo((float) (innerRadius * Math.cos(currentAngle)),
            (float) (innerRadius * Math.sin(currentAngle)));
        currentAngle += halfAnglePerPoint;
        path.lineTo((float) (outerRadius * Math.cos(currentAngle)),
            (float) (outerRadius * Math.sin(currentAngle)));
        currentAngle += halfAnglePerPoint;
      }

      if (partialPointAmount != 0) {
        // currentAngle -= halfAnglePerPoint;
        path.lineTo((float) (innerRadius * Math.cos(currentAngle)),
            (float) (innerRadius * Math.sin(currentAngle)));
        currentAngle += halfAnglePerPoint * partialPointAmount;
        path.lineTo((float) (partialPointRadius * Math.cos(currentAngle)),
            (float) (partialPointRadius * Math.sin(currentAngle)));
      }


      PointF position = positionAnimation.getValue();
      path.offset(position.x, position.y);
      path.close();
    }

    private void createPolygonPath() {

    }
  }
}
