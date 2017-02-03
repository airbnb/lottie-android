package com.airbnb.lottie;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

class RectLayer extends AnimatableLayer {
  @Nullable private RoundRectLayer fillLayer;
  @Nullable private RoundRectLayer strokeLayer;

  RectLayer(RectangleShape rectShape, @Nullable ShapeFill fill,
      @Nullable ShapeStroke stroke, Transform transform, Drawable.Callback callback) {
    super(callback);

    setBounds(transform.getBounds());
    setAnchorPoint(transform.getAnchor().createAnimation());
    setAlpha(transform.getOpacity().createAnimation());
    setPosition(transform.getPosition().createAnimation());
    setTransform(transform.getScale().createAnimation());
    setRotation(transform.getRotation().createAnimation());

    if (fill != null) {
      fillLayer = new RoundRectLayer(getCallback());
      fillLayer.setColor(fill.getColor().createAnimation());
      fillLayer.setShapeAlpha(fill.getOpacity().createAnimation());
      fillLayer.setTransformAlpha(transform.getOpacity().createAnimation());
      fillLayer.setRectCornerRadius(rectShape.getCornerRadius().createAnimation());
      fillLayer.setRectSize(rectShape.getSize().createAnimation());
      fillLayer.setRectPosition(rectShape.getPosition().createAnimation());
      addLayer(fillLayer);
    }

    if (stroke != null) {
      strokeLayer = new RoundRectLayer(getCallback());
      strokeLayer.setIsStroke();
      strokeLayer.setColor(stroke.getColor().createAnimation());
      strokeLayer.setShapeAlpha(stroke.getOpacity().createAnimation());
      strokeLayer.setTransformAlpha(transform.getOpacity().createAnimation());
      strokeLayer.setLineWidth(stroke.getWidth().createAnimation());
      if (!stroke.getLineDashPattern().isEmpty()) {
        List<KeyframeAnimation<Float>> dashPatternAnimations = new ArrayList<>(stroke.getLineDashPattern().size());
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

  private static class RoundRectLayer extends AnimatableLayer {
    private final KeyframeAnimation.AnimationListener<Integer> alphaChangedListener = new KeyframeAnimation.AnimationListener<Integer>() {
      @Override
      public void onValueChanged(Integer value) {
        invalidateSelf();
      }
    };

    private final KeyframeAnimation.AnimationListener<Integer> colorChangedListener = new KeyframeAnimation.AnimationListener<Integer>() {
      @Override
      public void onValueChanged(Integer value) {
        onColorChanged();
      }
    };

    private final KeyframeAnimation.AnimationListener<Float> lineWidthChangedListener = new KeyframeAnimation.AnimationListener<Float>() {
      @Override
      public void onValueChanged(Float value) {
        onLineWidthChanged();
      }
    };

    private final KeyframeAnimation.AnimationListener<Float> dashPatternChangedListener = new KeyframeAnimation.AnimationListener<Float>() {
      @Override
      public void onValueChanged(Float value) {
        onDashPatternChanged();
      }
    };

    private final KeyframeAnimation.AnimationListener<Float> cornerRadiusChangedListener = new KeyframeAnimation.AnimationListener<Float>() {
      @Override
      public void onValueChanged(Float value) {
        invalidateSelf();
      }
    };

    private final KeyframeAnimation.AnimationListener<PointF> rectPositionChangedListener = new KeyframeAnimation.AnimationListener<PointF>() {
      @Override
      public void onValueChanged(PointF value) {
        invalidateSelf();
      }
    };

    private final KeyframeAnimation.AnimationListener<PointF> rectSizeChangedListener = new KeyframeAnimation.AnimationListener<PointF>() {
      @Override
      public void onValueChanged(PointF value) {
        invalidateSelf();
      }
    };

    private final Paint paint = new Paint();
    private final RectF fillRect = new RectF();

    private KeyframeAnimation<Integer> color;
    private KeyframeAnimation<Float> lineWidth;
    private KeyframeAnimation<Integer> shapeAlpha;
    private KeyframeAnimation<Integer> transformAlpha;
    private KeyframeAnimation<Float> rectCornerRadius;
    private KeyframeAnimation<PointF> rectPosition;
    private KeyframeAnimation<PointF> rectSize;

    @Nullable private List<KeyframeAnimation<Float>> lineDashPattern;
    @Nullable private KeyframeAnimation<Float> lineDashPatternOffset;

    RoundRectLayer(Drawable.Callback callback) {
      super(callback);
      paint.setAntiAlias(true);
      paint.setStyle(Paint.Style.FILL);
    }

    void setShapeAlpha(KeyframeAnimation<Integer> shapeAlpha) {
      if (this.shapeAlpha != null) {
        removeAnimation(this.shapeAlpha);
        this.shapeAlpha.removeUpdateListener(alphaChangedListener);
      }
      this.shapeAlpha = shapeAlpha;
      addAnimation(shapeAlpha);
      shapeAlpha.addUpdateListener(alphaChangedListener);
      invalidateSelf();
    }

    void setTransformAlpha(KeyframeAnimation<Integer> transformAlpha) {
      if (this.transformAlpha != null) {
        removeAnimation(this.transformAlpha);
        this.transformAlpha.removeUpdateListener(alphaChangedListener);
      }
      this.transformAlpha = transformAlpha;
      addAnimation(transformAlpha);
      transformAlpha.addUpdateListener(alphaChangedListener);
      invalidateSelf();
    }


    @Override
    public void setAlpha(int alpha) {
      paint.setAlpha(alpha);
    }

    @Override
    public int getAlpha() {
      Integer shapeAlpha = this.shapeAlpha == null ? 255 : this.shapeAlpha.getValue();
      Integer transformAlpha = this.transformAlpha == null ? 255 : this.transformAlpha.getValue();
      int layerAlpha = super.getAlpha();
      return (int) ((shapeAlpha / 255f * transformAlpha / 255f * layerAlpha / 255f) * 255);
    }

    public void setColor(KeyframeAnimation<Integer> color) {
      if (this.color != null) {
        removeAnimation(this.color);
        this.color.removeUpdateListener(colorChangedListener);
      }
      this.color = color;
      addAnimation(color);
      color.addUpdateListener(colorChangedListener);
      onColorChanged();
    }

    private void onColorChanged() {
      paint.setColor(color.getValue());
      invalidateSelf();
    }

    private void setIsStroke() {
      paint.setStyle(Paint.Style.STROKE);
      invalidateSelf();
    }

    void setLineWidth(KeyframeAnimation<Float> lineWidth) {
      if (this.lineWidth != null) {
        removeAnimation(this.lineWidth);
        this.lineWidth.removeUpdateListener(lineWidthChangedListener);
      }
      this.lineWidth = lineWidth;
      addAnimation(lineWidth);
      lineWidth.addUpdateListener(lineWidthChangedListener);
      onLineWidthChanged();
    }

    private void onLineWidthChanged() {
      paint.setStrokeWidth(lineWidth.getValue());
      invalidateSelf();
    }

    void setDashPattern(List<KeyframeAnimation<Float>> lineDashPattern, KeyframeAnimation<Float> offset) {
      if (this.lineDashPattern != null) {
        removeAnimation(this.lineDashPattern.get(0));
        this.lineDashPattern.get(0).removeUpdateListener(dashPatternChangedListener);
        removeAnimation(this.lineDashPattern.get(1));
        this.lineDashPattern.get(1).removeUpdateListener(dashPatternChangedListener);
      }
      if (this.lineDashPatternOffset != null) {
        removeAnimation(this.lineDashPatternOffset);
        this.lineDashPatternOffset.removeUpdateListener(dashPatternChangedListener);
      }
      if (lineDashPattern.isEmpty()) {
        return;
      }
      this.lineDashPattern = lineDashPattern;
      this.lineDashPatternOffset = offset;
      addAnimation(lineDashPattern.get(0));
      addAnimation(lineDashPattern.get(1));
      lineDashPattern.get(0).addUpdateListener(dashPatternChangedListener);
      if (!lineDashPattern.get(1).equals(lineDashPattern.get(1))) {
        lineDashPattern.get(1).addUpdateListener(dashPatternChangedListener);
      }
      addAnimation(offset);
      offset.addUpdateListener(dashPatternChangedListener);
      onDashPatternChanged();
    }

    private void onDashPatternChanged() {
      if (lineDashPattern == null || lineDashPatternOffset == null) {
        throw new IllegalStateException("LineDashPattern is null");
      }
      float[] values = new float[lineDashPattern.size()];
      for (int i = 0; i < lineDashPattern.size(); i++) {
        values[i] = lineDashPattern.get(i).getValue();
      }
      paint.setPathEffect(new DashPathEffect(values, lineDashPatternOffset.getValue()));
      invalidateSelf();
    }

    void setLineCapType(ShapeStroke.LineCapType lineCapType) {
      switch (lineCapType) {
        case Round:
          paint.setStrokeCap(Paint.Cap.ROUND);
          break;
        case Butt:
          paint.setStrokeCap(Paint.Cap.BUTT);
        default:
      }
    }

    void setLineJoinType(ShapeStroke.LineJoinType lineJoinType) {
      switch (lineJoinType) {
        case Bevel:
          paint.setStrokeJoin(Paint.Join.BEVEL);
          break;
        case Miter:
          paint.setStrokeJoin(Paint.Join.MITER);
          break;
        case Round:
          paint.setStrokeJoin(Paint.Join.ROUND);
          break;
      }
    }

    void setRectCornerRadius(KeyframeAnimation<Float> rectCornerRadius) {
      if (this.rectCornerRadius != null) {
        removeAnimation(rectCornerRadius);
        this.rectCornerRadius.removeUpdateListener(cornerRadiusChangedListener);
      }
      this.rectCornerRadius = rectCornerRadius;
      addAnimation(rectCornerRadius);
      rectCornerRadius.addUpdateListener(cornerRadiusChangedListener);
      invalidateSelf();
    }

    void setRectPosition(KeyframeAnimation<PointF> rectPosition) {
      if (this.rectPosition != null) {
        removeAnimation(this.rectPosition);
        this.rectPosition.removeUpdateListener(rectPositionChangedListener);
      }
      this.rectPosition = rectPosition;
      addAnimation(rectPosition);
      rectPosition.addUpdateListener(rectPositionChangedListener);
      invalidateSelf();
    }

    void setRectSize(KeyframeAnimation<PointF> rectSize) {
      if (this.rectSize != null) {
        removeAnimation(this.rectSize);
        this.rectSize.removeUpdateListener(rectSizeChangedListener);
      }
      this.rectSize = rectSize;
      addAnimation(rectSize);
      rectSize.addUpdateListener(rectSizeChangedListener);
      invalidateSelf();
    }

    @SuppressLint("NewApi")
    @Override
    public void draw(@NonNull Canvas canvas) {
      if (paint.getStyle() == Paint.Style.STROKE && paint.getStrokeWidth() == 0f) {
        return;
      }
      paint.setAlpha(getAlpha());
      float halfWidth = rectSize.getValue().x / 2f;
      float halfHeight = rectSize.getValue().y / 2f;

      fillRect.set(rectPosition.getValue().x - halfWidth,
          rectPosition.getValue().y - halfHeight,
          rectPosition.getValue().x + halfWidth,
          rectPosition.getValue().y + halfHeight);
      if (rectCornerRadius.getValue() == 0) {
        canvas.drawRect(fillRect, paint);
      } else {
        canvas.drawRoundRect(fillRect, rectCornerRadius.getValue(), rectCornerRadius.getValue(), paint);
      }
    }
  }

}
