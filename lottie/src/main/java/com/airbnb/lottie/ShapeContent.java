package com.airbnb.lottie;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

class ShapeContent extends AnimatableLayer {
  private final KeyframeAnimation.AnimationListener<Path> pathChangedListener =
      new KeyframeAnimation.AnimationListener<Path>() {
        @Override
        public void onValueChanged(Path value) {
          onPathChanged();
        }
      };

  private final KeyframeAnimation.AnimationListener<Integer> alphaChangedListener =
      new KeyframeAnimation.AnimationListener<Integer>() {
        @Override
        public void onValueChanged(Integer value) {
          invalidateSelf();
        }
      };

  private final KeyframeAnimation.AnimationListener<Integer> colorChangedListener =
      new KeyframeAnimation.AnimationListener<Integer>() {
        @Override
        public void onValueChanged(Integer value) {
          onColorChanged();
        }
      };

  private final KeyframeAnimation.AnimationListener<Float> lineWidthChangedListener =
      new KeyframeAnimation.AnimationListener<Float>() {
        @Override
        public void onValueChanged(Float value) {
          invalidateSelf();
        }
      };

  private final KeyframeAnimation.AnimationListener<Float> dashPatternChangedListener =
      new KeyframeAnimation.AnimationListener<Float>() {
        @Override
        public void onValueChanged(Float value) {
          onDashPatternChanged();
        }
      };

  private final KeyframeAnimation.AnimationListener<Float> strokeChangedListener =
      new KeyframeAnimation.AnimationListener<Float>() {
        @Override
        public void onValueChanged(Float value) {
          onPathPropertiesChanged();
        }
      };

  private final KeyframeAnimation.AnimationListener<ScaleXY> scaleChangedListener =
      new KeyframeAnimation.AnimationListener<ScaleXY>() {
        @Override
        public void onValueChanged(ScaleXY value) {
          onPathPropertiesChanged();
        }
      };


  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG) {{
    setStyle(Paint.Style.FILL);
  }};
  private final Path tempPath = new Path();
  private final Path currentPath = new Path();
  /**
   * Path for the trim path when it loops around the end back to the start
   */
  private final Path extraTrimPath = new Path();
  private final PathMeasure pathMeasure = new PathMeasure();

  private float currentPathScaleX;
  private float currentPathScaleY;
  private float currentDrawableScale = 1f;
  private float currentPathStrokeStart;
  private float currentPathStrokeEnd = 100;
  private float currentPathStrokeOffset = 0;

  @Nullable private KeyframeAnimation<ScaleXY> scale;
  private final RectF tempRect = new RectF();
  private final Matrix scaleMatrix = new Matrix();

  private BaseKeyframeAnimation<?, Path> path;
  private BaseKeyframeAnimation<?, Integer> color;
  private BaseKeyframeAnimation<?, Float> lineWidth;
  @Nullable private BaseKeyframeAnimation<?, Float> strokeStart;
  @Nullable private BaseKeyframeAnimation<?, Float> strokeEnd;
  @Nullable private BaseKeyframeAnimation<?, Float> strokeOffset;

  private BaseKeyframeAnimation<?, Integer> shapeOpacity;
  private BaseKeyframeAnimation<?, Integer> transformOpacity;
  private List<BaseKeyframeAnimation<?, Float>> lineDashPattern;
  private BaseKeyframeAnimation<?, Float> lineDashPatternOffset;
  private boolean pathPropertiesChanged = true;
  private boolean dashPatternChanged;

  ShapeContent(LottieDrawable lottieDrawable) {
    super(lottieDrawable);
  }

  void setIsStroke() {
    paint.setStyle(Paint.Style.STROKE);
    invalidateSelf();
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

  public void setPath(BaseKeyframeAnimation<?, Path> path) {
    if (this.path != null) {
      removeAnimation(this.path);
      this.path.removeUpdateListener(pathChangedListener);
    }

    this.path = path;
    addAnimation(path);
    // TODO: When the path changes, we probably have to scale it again.
    path.addUpdateListener(pathChangedListener);
    onPathChanged();
  }

  void onPathChanged() {
    currentPath.reset();
    currentPath.set(path.getValue());
    currentPathStrokeStart = Float.NaN;
    currentPathStrokeEnd = Float.NaN;
    currentPathScaleX = Float.NaN;
    currentPathScaleY = Float.NaN;
    currentDrawableScale = 1f;
    onPathPropertiesChanged();
    invalidateSelf();
  }

  private void onPathPropertiesChanged() {
    pathPropertiesChanged = true;
    invalidateSelf();
  }

  @SuppressLint("NewApi")
  @Override
  public void draw(@NonNull Canvas canvas) {
    float scale = lottieDrawable.getScale();
    if (currentDrawableScale != scale) {
      updateShape();
    }

    if (pathPropertiesChanged) {
      updateShape();
    }
    if (dashPatternChanged) {
      updateDashPattern();
    }
    if (lineWidth != null) {
      paint.setStrokeWidth(lineWidth.getValue() * lottieDrawable.getScale());
    }
    if (paint.getStyle() == Paint.Style.STROKE && paint.getStrokeWidth() == 0f) {
      return;
    }
    paint.setAlpha(getAlpha());
    canvas.drawPath(currentPath, paint);
    if (!extraTrimPath.isEmpty()) {
      canvas.drawPath(extraTrimPath, paint);
    }
  }

  private void updateShape() {
    pathPropertiesChanged = false;
    boolean needsStrokeStart =
        strokeStart != null && strokeStart.getValue() != currentPathStrokeStart;
    boolean needsStrokeEnd = strokeEnd != null && strokeEnd.getValue() != currentPathStrokeEnd;
    boolean needsStrokeOffset =
        strokeOffset != null && strokeOffset.getValue() != currentPathStrokeOffset;
    boolean needsScaleX = scale != null && scale.getValue().getScaleX() != currentPathScaleX;
    boolean needsScaleY = scale != null && scale.getValue().getScaleY() != currentPathScaleY;
    boolean needsDrawableScale = currentDrawableScale != lottieDrawable.getScale();

    if (!needsStrokeStart && !needsStrokeEnd && !needsScaleX && !needsScaleY &&
        !needsStrokeOffset && !needsDrawableScale) {
      return;
    }

    currentPath.set(path.getValue());

    currentPath.computeBounds(tempRect, false);
    currentPathScaleX = scale == null ? 1f : scale.getValue().getScaleX();
    currentPathScaleY = scale == null ? 1f : scale.getValue().getScaleY();
    currentDrawableScale = lottieDrawable.getScale();
    scaleMatrix.reset();
    scaleMatrix
          .setScale(
              currentPathScaleX,
              currentPathScaleY,
              tempRect.centerX(),
              tempRect.centerY());
      currentPath.transform(scaleMatrix, currentPath);
    scaleMatrix.reset();
    scaleMatrix.setScale(currentDrawableScale, currentDrawableScale ,0, 0);
    currentPath.transform(scaleMatrix, currentPath);

    if (needsStrokeStart || needsStrokeEnd || needsStrokeOffset) {
      tempPath.set(currentPath);
      pathMeasure.setPath(tempPath, false);
      currentPathStrokeStart = strokeStart.getValue();
      currentPathStrokeEnd = strokeEnd.getValue();
      float length = pathMeasure.getLength();
      float start = length * currentPathStrokeStart / 100f;
      float end = length * currentPathStrokeEnd / 100f;
      float newStart = Math.min(start, end);
      float newEnd = Math.max(start, end);

      currentPath.reset();
      currentPathStrokeOffset = strokeOffset.getValue() / 360f * length;
      newStart += currentPathStrokeOffset;
      newEnd += currentPathStrokeOffset;

      // If the trim path has rotated around the path, we need to shift it back.
      if (newStart > length && newEnd > length) {
        newStart %= length;
        newEnd %= length;
      }
      if (newStart > newEnd) {
        newStart -= length;
      }

      pathMeasure.getSegment(
          newStart,
          newEnd,
          currentPath,
          true);

      extraTrimPath.reset();
      if (newEnd > length) {
        pathMeasure.getSegment(
            0,
            newEnd % length,
            extraTrimPath,
            true);
      } else if (newStart < 0) {
        pathMeasure.getSegment(
            length + newStart,
            length,
            extraTrimPath,
            true);
      }
    }
  }

  private void updateDashPattern() {
    float scale = lottieDrawable.getScale();
    float[] values = new float[lineDashPattern.size()];
    for (int i = 0; i < lineDashPattern.size(); i++) {
      values[i] = lineDashPattern.get(i).getValue();
      // If the value of the dash pattern or gap is too small, the number of individual sections
      // approaches infinity as the value approaches 0.
      // To mitigate this, we essentially put a minimum value on the dash pattern size of 1px
      // and a minimum gap size of 0.01.
      if (i % 2 == 0) {
        if (values[i] < 1f) {
          values[i] = 1f;
        }
      } else {
        if (values[i] < 0.1f) {
          values[i] = 0.1f;
        }
      }
      values[i] *= scale;
    }
    paint.setPathEffect(new DashPathEffect(values, lineDashPatternOffset.getValue()));
  }

  @Override public int getAlpha() {
    int shapeOpacity = this.shapeOpacity == null ? 255 : this.shapeOpacity.getValue();
    int transformOpacity = this.transformOpacity == null ? 255 : this.transformOpacity.getValue();
    int layerOpacity = super.getAlpha();
    return (int) ((shapeOpacity / 255f * transformOpacity / 255f * layerOpacity / 255f) * 255);
  }

  void setShapeOpacity(KeyframeAnimation<Integer> shapeOpacity) {
    if (this.shapeOpacity != null) {
      removeAnimation(this.shapeOpacity);
      this.shapeOpacity.removeUpdateListener(alphaChangedListener);
    }
    this.shapeOpacity = shapeOpacity;
    addAnimation(shapeOpacity);
    shapeOpacity.addUpdateListener(alphaChangedListener);
    invalidateSelf();
  }

  void setTransformOpacity(KeyframeAnimation<Integer> transformOpacity) {
    this.transformOpacity = transformOpacity;
    addAnimation(transformOpacity);
    transformOpacity.addUpdateListener(alphaChangedListener);
    invalidateSelf();
  }

  public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
    paint.setAlpha(alpha);
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
    invalidateSelf();
  }

  void setDashPattern(List<BaseKeyframeAnimation<?, Float>> lineDashPattern,
      BaseKeyframeAnimation<?, Float> offset) {
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
    for (int i = 0; i < lineDashPattern.size(); i++) {
      BaseKeyframeAnimation<?, Float> dashPattern = lineDashPattern.get(i);
      addAnimation(dashPattern);
      dashPattern.addUpdateListener(dashPatternChangedListener);
    }
    addAnimation(offset);
    offset.addUpdateListener(dashPatternChangedListener);
    onDashPatternChanged();
  }

  private void onDashPatternChanged() {
    dashPatternChanged = true;
    invalidateSelf();
  }

  void setLineCapType(ShapeStroke.LineCapType lineCapType) {
    switch (lineCapType) {
      case Round:
        paint.setStrokeCap(Paint.Cap.ROUND);
        break;
      case Butt:
      default:
        paint.setStrokeCap(Paint.Cap.BUTT);
    }
    invalidateSelf();
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

  void setTrimPath(KeyframeAnimation<Float> strokeStart, KeyframeAnimation<Float> strokeEnd,
      KeyframeAnimation<Float> strokeOffset) {
    if (this.strokeStart != null) {
      removeAnimation(this.strokeStart);
      this.strokeStart.removeUpdateListener(strokeChangedListener);
    }
    if (this.strokeEnd != null) {
      removeAnimation(this.strokeEnd);
      this.strokeEnd.removeUpdateListener(strokeChangedListener);
    }
    if (this.strokeOffset != null) {
      removeAnimation(this.strokeOffset);
      this.strokeOffset.removeUpdateListener(strokeChangedListener);
    }
    this.strokeStart = strokeStart;
    this.strokeEnd = strokeEnd;
    this.strokeOffset = strokeOffset;
    addAnimation(strokeStart);
    strokeStart.addUpdateListener(strokeChangedListener);
    addAnimation(strokeEnd);
    strokeEnd.addUpdateListener(strokeChangedListener);
    addAnimation(strokeOffset);
    strokeOffset.addUpdateListener(strokeChangedListener);
    onPathPropertiesChanged();
  }

  void setScale(@SuppressWarnings("NullableProblems") KeyframeAnimation<ScaleXY> scale) {
    if (this.scale != null) {
      removeAnimation(this.scale);
      this.scale.removeUpdateListener(scaleChangedListener);
    }
    this.scale = scale;
    addAnimation(scale);
    scale.addUpdateListener(scaleChangedListener);
    onPathPropertiesChanged();
  }
}
