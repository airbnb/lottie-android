package com.airbnb.lottie;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

class ShapeLayer extends AnimatableLayer {
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
          onLineWidthChanged();
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


  private final Paint paint = new Paint();
  private final Path tempPath = new Path();
  private final Path currentPath = new Path();
  /**
   * Path for the trim path when it loops around the end back to the start
   */
  private final Path extraTrimPath = new Path();
  private final PathMeasure pathMeasure = new PathMeasure();

  private float currentPathScaleX;
  private float currentPathScaleY;
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

  private BaseKeyframeAnimation<?, Integer> shapeAlpha;
  private BaseKeyframeAnimation<?, Integer> transformAlpha;
  private List<BaseKeyframeAnimation<?, Float>> lineDashPattern;
  private BaseKeyframeAnimation<?, Float> lineDashPatternOffset;

  ShapeLayer(Drawable.Callback callback) {
    super(callback);
    paint.setStyle(Paint.Style.FILL);
    paint.setAntiAlias(true);
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
    onPathPropertiesChanged();
    invalidateSelf();
  }

  private void onPathPropertiesChanged() {
    boolean needsStrokeStart =
        strokeStart != null && strokeStart.getValue() != currentPathStrokeStart;
    boolean needsStrokeEnd = strokeEnd != null && strokeEnd.getValue() != currentPathStrokeEnd;
    boolean needsStrokeOffset =
        strokeOffset != null && strokeOffset.getValue() != currentPathStrokeOffset;
    boolean needsScaleX = scale != null && scale.getValue().getScaleX() != currentPathScaleX;
    boolean needsScaleY = scale != null && scale.getValue().getScaleY() != currentPathScaleY;

    if (!needsStrokeStart && !needsStrokeEnd && !needsScaleX && !needsScaleY &&
        !needsStrokeOffset) {
      return;
    }
    currentPath.set(path.getValue());

    if (needsScaleX || needsScaleY) {
      currentPath.computeBounds(tempRect, false);
      currentPathScaleX = scale.getValue().getScaleX();
      currentPathScaleY = scale.getValue().getScaleY();
      scaleMatrix
          .setScale(currentPathScaleX, currentPathScaleY, tempRect.centerX(), tempRect.centerY());
      currentPath.transform(scaleMatrix, currentPath);
    }

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

    currentPath.computeBounds(tempRect, false);
    setBounds((int) tempRect.left, (int) tempRect.top, (int) tempRect.right, (int) tempRect.bottom);

    invalidateSelf();
  }

  @SuppressLint("NewApi")
  @Override
  public void draw(@NonNull Canvas canvas) {
    if (paint.getStyle() == Paint.Style.STROKE && paint.getStrokeWidth() == 0f) {
      return;
    }
    paint.setAlpha(getAlpha());
    canvas.drawPath(currentPath, paint);
    if (!extraTrimPath.isEmpty()) {
      canvas.drawPath(extraTrimPath, paint);
    }
  }

  @Override public int getAlpha() {
    Integer shapeAlpha = this.shapeAlpha == null ? 255 : this.shapeAlpha.getValue();
    Integer transformAlpha = this.transformAlpha == null ? 255 : this.transformAlpha.getValue();
    int layerAlpha = super.getAlpha();
    return (int) ((shapeAlpha / 255f * transformAlpha / 255f * layerAlpha / 255f) * 255);
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

  @Override public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
    paint.setAlpha(alpha);
    invalidateSelf();
  }

  @Override public void setColorFilter(ColorFilter colorFilter) {

  }

  @Override public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
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
    addAnimation(lineDashPattern.get(0));
    lineDashPattern.get(0).addUpdateListener(dashPatternChangedListener);
    if (!lineDashPattern.get(1).equals(lineDashPattern.get(1))) {
      addAnimation(lineDashPattern.get(1));
      lineDashPattern.get(1).addUpdateListener(dashPatternChangedListener);
    }
    addAnimation(offset);
    offset.addUpdateListener(dashPatternChangedListener);
    onDashPatternChanged();
  }

  private void onDashPatternChanged() {
    float[] values = new float[lineDashPattern.size()];
    for (int i = 0; i < lineDashPattern.size(); i++) {
      values[i] = lineDashPattern.get(i).getValue();
      if (values[i] == 0) {
        values[i] = 0.01f;
      }
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
