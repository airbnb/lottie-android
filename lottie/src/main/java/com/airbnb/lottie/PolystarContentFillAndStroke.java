package com.airbnb.lottie;

import android.graphics.Path;
import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

class PolystarContentFillAndStroke extends AnimatableLayer {
  PolystarContentFillAndStroke(PolystarShape polystarShape, ShapeFill fill, ShapeStroke stroke,
      ShapeTrimPath trim, AnimatableTransform transform, LottieDrawable lottieDrawable) {
    super(lottieDrawable);

    setTransform(transform.createAnimation());

    if (fill != null) {
      PolystarContent fillLayer = new PolystarContent(lottieDrawable);
      fillLayer.setColor(fill.getColor().createAnimation());
      fillLayer.setTransformOpacity(transform.getOpacity().createAnimation());
      fillLayer.setShapeOpacity(fill.getOpacity().createAnimation());
      fillLayer.setShape(polystarShape);
      if (trim != null) {
        fillLayer.setTrimPath(trim.getStart().createAnimation(), trim.getEnd().createAnimation(),
            trim.getOffset().createAnimation());
      }
      addLayer(fillLayer);
    }

    if (stroke != null) {
      PolystarContent strokeLayer = new PolystarContent(lottieDrawable);
      strokeLayer.setIsStroke();
      strokeLayer.setColor(stroke.getColor().createAnimation());
      strokeLayer.setTransformOpacity(transform.getOpacity().createAnimation());
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
      strokeLayer.setShape(polystarShape);
      if (trim != null) {
        strokeLayer.setTrimPath(trim.getStart().createAnimation(), trim.getEnd().createAnimation(),
            trim.getOffset().createAnimation());
      }

      addLayer(strokeLayer);
    }
  }

  private static final class PolystarContent extends ShapeContent {

    /**
     * This was empirically derived by creating polystars, converting them to
     * curves, and calculating a scale factor.
     * It works best for polygons and stars with 3 points and needs more
     * work otherwise.
     */
    private static final float POLYSTAR_MAGIC_NUMBER = .47829f;
    private static final float POLYGON_MAGIC_NUMBER = .25f;
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

    PolystarContent(LottieDrawable lottieDrawable) {
      super(lottieDrawable);
      setPath(new StaticKeyframeAnimation<>(path));
    }

    void setShape(PolystarShape polystarShape) {
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
      // Not used for polygons.
      if (polystarShape.getInnerRadius() != null) {
        innerRadiusAnimation = polystarShape.getInnerRadius().createAnimation();
      }
      if (polystarShape.getInnerRoundedness() != null) {
        innerRoundednessAnimation = polystarShape.getInnerRoundedness().createAnimation();
      }

      pointsAnimation.addUpdateListener(floatChangedListener);
      positionAnimation.addUpdateListener(pointChangedListener);
      rotationAnimation.addUpdateListener(floatChangedListener);
      outerRadiusAnimation.addUpdateListener(floatChangedListener);
      outerRoundednessAnimation.addUpdateListener(floatChangedListener);
      if (innerRadiusAnimation != null) {
        innerRadiusAnimation.addUpdateListener(floatChangedListener);
      }
      if (innerRoundednessAnimation != null) {
        innerRoundednessAnimation.addUpdateListener(floatChangedListener);
      }

      addAnimation(pointsAnimation);
      addAnimation(positionAnimation);
      addAnimation(rotationAnimation);
      addAnimation(outerRadiusAnimation);
      addAnimation(outerRoundednessAnimation);
      if (innerRadiusAnimation != null) {
        addAnimation(innerRadiusAnimation);
      }
      if (innerRoundednessAnimation != null) {
        addAnimation(innerRoundednessAnimation) ;
      }
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
      // Start at +y instead of +x
      currentAngle -= 90;
      // convert to radians
      currentAngle = Math.toRadians(currentAngle);
      // adjust current angle for partial points
      float anglePerPoint = (float) (2 * Math.PI / points);
      float halfAnglePerPoint = anglePerPoint / 2.0f;
      float partialPointAmount = points - (int) points;
      if (partialPointAmount != 0) {
        currentAngle += halfAnglePerPoint * (1f - partialPointAmount);
      }

      float outerRadius = outerRadiusAnimation.getValue();
      float innerRadius = innerRadiusAnimation.getValue();

      float innerRoundedness = 0f;
      if (innerRoundednessAnimation != null) {
        innerRoundedness = innerRoundednessAnimation.getValue() / 100f;
      }
      float outerRoundedness = 0f;
      if (outerRoundednessAnimation != null) {
        outerRoundedness = outerRoundednessAnimation.getValue() / 100f;
      }

      path.reset();


      float x;
      float y;
      float previousX;
      float previousY;
      float partialPointRadius = 0;
      if (partialPointAmount != 0) {
        partialPointRadius = innerRadius + partialPointAmount * (outerRadius - innerRadius);
        x = (float) (partialPointRadius * Math.cos(currentAngle));
        y = (float) (partialPointRadius * Math.sin(currentAngle));
        path.moveTo(x, y);
        currentAngle += anglePerPoint * partialPointAmount / 2f;
      } else {
        x = (float) (outerRadius * Math.cos(currentAngle));
        y = (float) (outerRadius * Math.sin(currentAngle));
        path.moveTo(x, y);
        currentAngle += halfAnglePerPoint;
      }

      // True means the line will go to outer radius. False means inner radius.
      boolean longSegment = false;
      double numPoints = Math.ceil(points) * 2;
      for (int i = 0; i < numPoints; i++) {
        float radius = longSegment ? outerRadius : innerRadius;
        float dTheta = halfAnglePerPoint;
        if (partialPointRadius != 0 && i == numPoints - 2) {
          dTheta = anglePerPoint * partialPointAmount / 2f;
        }
        if (partialPointRadius != 0 && i == numPoints - 1) {
          radius = partialPointRadius;
        }
        previousX = x;
        previousY = y;
        x = (float) (radius * Math.cos(currentAngle));
        y = (float) (radius * Math.sin(currentAngle));

        if (innerRoundedness == 0 && outerRoundedness == 0) {
          path.lineTo(x, y);
        } else {
          float cp1Theta = (float) (Math.atan2(previousY, previousX) - Math.PI / 2f);
          float cp1Dx = (float) Math.cos(cp1Theta);
          float cp1Dy = (float) Math.sin(cp1Theta);

          float cp2Theta = (float) (Math.atan2(y, x) - Math.PI / 2f);
          float cp2Dx = (float) Math.cos(cp2Theta);
          float cp2Dy = (float) Math.sin(cp2Theta);

          float cp1Roundedness = longSegment ? innerRoundedness : outerRoundedness;
          float cp2Roundedness = longSegment ? outerRoundedness : innerRoundedness;
          float cp1Radius = longSegment ? innerRadius : outerRadius;
          float cp2Radius = longSegment ? outerRadius : innerRadius;

          float cp1x = cp1Radius * cp1Roundedness * POLYSTAR_MAGIC_NUMBER * cp1Dx;
          float cp1y = cp1Radius * cp1Roundedness * POLYSTAR_MAGIC_NUMBER * cp1Dy;
          float cp2x = cp2Radius * cp2Roundedness * POLYSTAR_MAGIC_NUMBER * cp2Dx;
          float cp2y = cp2Radius * cp2Roundedness * POLYSTAR_MAGIC_NUMBER * cp2Dy;
          if (partialPointAmount != 0) {
            if (i == 0) {
              cp1x *= partialPointAmount;
              cp1y *= partialPointAmount;
            } else if (i == numPoints - 1) {
              cp2x *= partialPointAmount;
              cp2y *= partialPointAmount;
            }
          }

          path.cubicTo(previousX - cp1x,previousY - cp1y, x + cp2x, y + cp2y, x, y);
        }

        currentAngle += dTheta;
        longSegment = !longSegment;
      }


      PointF position = positionAnimation.getValue();
      path.offset(position.x, position.y);
      path.close();
    }

    private void createPolygonPath() {
      int points = (int) Math.floor(pointsAnimation.getValue());
      double currentAngle = rotationAnimation == null ? 0f : rotationAnimation.getValue();
      // Start at +y instead of +x
      currentAngle -= 90;
      // convert to radians
      currentAngle = Math.toRadians(currentAngle);
      // adjust current angle for partial points
      float anglePerPoint = (float) (2 * Math.PI / points);

      path.reset();

      float roundedness = outerRoundednessAnimation.getValue() / 100f;
      float radius = outerRadiusAnimation.getValue();
      float x;
      float y;
      float previousX;
      float previousY;
      x = (float) (radius * Math.cos(currentAngle));
      y = (float) (radius * Math.sin(currentAngle));
      path.moveTo(x, y);
      currentAngle += anglePerPoint;

      double numPoints = Math.ceil(points);
      for (int i = 0; i < numPoints; i++) {
        previousX = x;
        previousY = y;
        x = (float) (radius * Math.cos(currentAngle));
        y = (float) (radius * Math.sin(currentAngle));

        if (roundedness != 0) {
          float cp1Theta = (float) (Math.atan2(previousY, previousX) - Math.PI / 2f);
          float cp1Dx = (float) Math.cos(cp1Theta);
          float cp1Dy = (float) Math.sin(cp1Theta);

          float cp2Theta = (float) (Math.atan2(y, x) - Math.PI / 2f);
          float cp2Dx = (float) Math.cos(cp2Theta);
          float cp2Dy = (float) Math.sin(cp2Theta);

          float cp1x = radius * roundedness * POLYGON_MAGIC_NUMBER * cp1Dx;
          float cp1y = radius * roundedness * POLYGON_MAGIC_NUMBER * cp1Dy;
          float cp2x = radius * roundedness * POLYGON_MAGIC_NUMBER * cp2Dx;
          float cp2y = radius * roundedness * POLYGON_MAGIC_NUMBER * cp2Dy;
          path.cubicTo(previousX - cp1x,previousY - cp1y, x + cp2x, y + cp2y, x, y);
        } else {
          path.lineTo(x, y);
        }

        currentAngle += anglePerPoint;
      }


      PointF position = positionAnimation.getValue();
      path.offset(position.x, position.y);
      path.close();
    }
  }
}
