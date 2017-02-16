package com.airbnb.lottie;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

class RectLayer extends AnimatableLayer {
  @Nullable private RectShapeLayer fillLayer;
  @Nullable private RectShapeLayer strokeLayer;

  RectLayer(RectangleShape rectShape, @Nullable ShapeFill fill, @Nullable ShapeStroke stroke,
      @Nullable ShapeTrimPath trim, Transform transform, Drawable.Callback callback) {
    super(callback);

    setBounds(transform.getBounds());
    setAnchorPoint(transform.getAnchor().createAnimation());
    setAlpha(transform.getOpacity().createAnimation());
    setPosition(transform.getPosition().createAnimation());
    setTransform(transform.getScale().createAnimation());
    setRotation(transform.getRotation().createAnimation());

    if (fill != null) {
      fillLayer = new RectShapeLayer(getCallback());
      fillLayer.setColor(fill.getColor().createAnimation());
      fillLayer.setShapeAlpha(fill.getOpacity().createAnimation());
      fillLayer.setTransformAlpha(transform.getOpacity().createAnimation());
      fillLayer.setRectCornerRadius(rectShape.getCornerRadius().createAnimation());
      fillLayer.setRectSize(rectShape.getSize().createAnimation());
      fillLayer.setRectPosition(rectShape.getPosition().createAnimation());
      if (trim != null) {
        fillLayer.setTrimPath(trim.getStart().createAnimation(), trim.getEnd().createAnimation(),
            trim.getOffset().createAnimation());
      }
      addLayer(fillLayer);
    }

    if (stroke != null) {
      strokeLayer = new RectShapeLayer(getCallback());
      strokeLayer.setIsStroke();
      strokeLayer.setColor(stroke.getColor().createAnimation());
      strokeLayer.setShapeAlpha(stroke.getOpacity().createAnimation());
      strokeLayer.setTransformAlpha(transform.getOpacity().createAnimation());
      strokeLayer.setLineWidth(stroke.getWidth().createAnimation());
      if (!stroke.getLineDashPattern().isEmpty()) {
        List<BaseKeyframeAnimation<?, Float>> dashPatternAnimations = new ArrayList<>(stroke
            .getLineDashPattern().size());
        for (AnimatableFloatValue dashPattern : stroke.getLineDashPattern()) {
          dashPatternAnimations.add(dashPattern.createAnimation());
        }
        strokeLayer.setDashPattern(dashPatternAnimations, stroke.getDashOffset().createAnimation());
      }
      strokeLayer.setLineCapType(stroke.getCapType());
      strokeLayer.setRectCornerRadius(rectShape.getCornerRadius().createAnimation());
      strokeLayer.setRectSize(rectShape.getSize().createAnimation());
      strokeLayer.setRectPosition(rectShape.getPosition().createAnimation());
      strokeLayer.setLineJoinType(stroke.getJoinType());
      if (trim != null) {
        strokeLayer.setTrimPath(trim.getStart().createAnimation(), trim.getEnd().createAnimation(),
            trim.getOffset().createAnimation());
      }
      addLayer(strokeLayer);
    }
  }

  @Override
  public void setAlpha(int alpha) {
    super.setAlpha(alpha);
    if (fillLayer != null) {
      fillLayer.setAlpha(alpha);
    }
    if (strokeLayer != null) {
      strokeLayer.setAlpha(alpha);
    }
  }

  private static class RectShapeLayer extends ShapeLayer {

    private final KeyframeAnimation.AnimationListener<PointF> sizeChangedListener =
        new KeyframeAnimation.AnimationListener<PointF>() {
          @Override
          public void onValueChanged(PointF value) {
            onRectChanged();
          }
        };

    private final KeyframeAnimation.AnimationListener<Float> cornerRadiusChangedListener =
        new KeyframeAnimation.AnimationListener<Float>() {
          @Override
          public void onValueChanged(Float value) {
            onRectChanged();
          }
        };

    private final KeyframeAnimation.AnimationListener<PointF> positionChangedListener =
        new KeyframeAnimation.AnimationListener<PointF>() {
          @Override
          public void onValueChanged(PointF value) {
            onRectChanged();
          }
        };

    private final Path path = new Path();
    private final RectF rect = new RectF();
    private BaseKeyframeAnimation<?, Float> rectCornerRadius;
    private BaseKeyframeAnimation<?, PointF> rectPosition;
    private BaseKeyframeAnimation<?, PointF> rectSize;

    private boolean updateRectOnNextDraw;

    RectShapeLayer(Drawable.Callback callback) {
      super(callback);
      setPath(new StaticKeyframeAnimation<>(path));
    }

    void setRectCornerRadius(KeyframeAnimation<Float> rectCornerRadius) {
      if (this.rectCornerRadius != null) {
        removeAnimation(rectCornerRadius);
        this.rectCornerRadius.removeUpdateListener(cornerRadiusChangedListener);
      }
      this.rectCornerRadius = rectCornerRadius;
      addAnimation(rectCornerRadius);
      rectCornerRadius.addUpdateListener(cornerRadiusChangedListener);
      onRectChanged();
    }

    void setRectSize(KeyframeAnimation<PointF> rectSize) {
      if (this.rectSize != null) {
        removeAnimation(this.rectSize);
        this.rectSize.removeUpdateListener(sizeChangedListener);
      }
      this.rectSize = rectSize;
      addAnimation(rectSize);
      rectSize.addUpdateListener(sizeChangedListener);
      onRectChanged();
    }

    void setRectPosition(BaseKeyframeAnimation<?, PointF> rectPosition) {
      if (this.rectPosition != null) {
        removeAnimation(this.rectPosition);
        this.rectPosition.removeUpdateListener(positionChangedListener);
      }
      this.rectPosition = rectPosition;
      addAnimation(rectPosition);
      rectPosition.addUpdateListener(positionChangedListener);
      onRectChanged();
    }

    private void onRectChanged() {
      updateRectOnNextDraw = true;
      invalidateSelf();
    }

    private void updateRect() {
      path.reset();

      if (rectSize == null) {
        return;
      }

      PointF size = rectSize.getValue();
      float halfWidth = size.x / 2f;
      float halfHeight = size.y / 2f;
      float radius = rectCornerRadius == null ? 0f : rectCornerRadius.getValue();

      // Draw the rectangle top right to bottom left.
      PointF position = rectPosition == null ? Utils.emptyPoint() : rectPosition.getValue();

      path.moveTo(position.x + halfWidth, position.y - halfHeight + radius);

      path.lineTo(position.x + halfWidth, position.y + halfHeight - radius);

      if (radius > 0) {
        rect.set(position.x + halfWidth - 2 * radius,
            position.y + halfHeight - 2 * radius,
            position.x + halfWidth,
            position.y + halfHeight);
        path.arcTo(rect, 0, 90, false);
      }

      path.lineTo(position.x - halfWidth + radius, position.y + halfHeight);

      if (radius > 0) {
        rect.set(position.x - halfWidth,
            position.y + halfHeight - 2 * radius,
            position.x - halfWidth + 2 * radius,
            position.y + halfHeight);
        path.arcTo(rect, 90, 90, false);
      }

      path.lineTo(position.x - halfWidth, position.y - halfHeight + 2 * radius);

      if (radius > 0) {
        rect.set(position.x - halfWidth,
            position.y - halfHeight,
            position.x - halfWidth + 2 * radius,
            position.y - halfHeight + 2 * radius);
        path.arcTo(rect, 180, 90, false);
      }

      path.lineTo(position.x + halfWidth - 2 * radius, position.y - halfHeight);

      if (radius > 0) {
        rect.set(position.x + halfWidth - 2 * radius,
            position.y - halfHeight,
            position.x + halfWidth,
            position.y - halfHeight + 2 * radius);
        path.arcTo(rect, 270, 90, false);
      }
      path.close();

      onPathChanged();
    }

    @Override public void draw(@NonNull Canvas canvas) {
      if (updateRectOnNextDraw) {
        updateRectOnNextDraw = false;
        updateRect();
      }
      super.draw(canvas);
    }
  }
}
