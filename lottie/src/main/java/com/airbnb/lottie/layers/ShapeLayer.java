package com.airbnb.lottie.layers;

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

import com.airbnb.lottie.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.ShapeStroke;
import com.airbnb.lottie.utils.ScaleXY;
import com.airbnb.lottie.animatable.Observable;

import java.util.List;

class ShapeLayer extends AnimatableLayer {
    private final Observable.OnChangedListener pathChangedListener = new Observable.OnChangedListener() {
        @Override
        public void onChanged() {
            onPathChanged();
        }
    };

    private final Observable.OnChangedListener alphaChangedListener = new Observable.OnChangedListener() {
        @Override
        public void onChanged() {
            onAlphaChanged();
        }
    };

    private final Observable.OnChangedListener colorChangedListener = new Observable.OnChangedListener() {
        @Override
        public void onChanged() {
            onColorChanged();
        }
    };

    private final Observable.OnChangedListener lineWidthChangedListener = new Observable.OnChangedListener() {
        @Override
        public void onChanged() {
            onLineWidthChanged();
        }
    };

    private final Observable.OnChangedListener dashPatternChangedListener = new Observable.OnChangedListener() {
        @Override
        public void onChanged() {
            onDashPatternChanged();
        }
    };

    private final Observable.OnChangedListener pathPropertyChangedListener = new Observable.OnChangedListener() {
        @Override
        public void onChanged() {
            onPathPropertiesChanged();
        }
    };


    private final RectF bounds = new RectF();
    private final Paint paint = new Paint();
    private final Path tempPath = new Path();
    private final Path currentPath = new Path();
    private final PathMeasure pathMeasure = new PathMeasure();

    private float currentPathScaleX;
    private float currentPathScaleY;
    private float currentPathStrokeStart;
    private float currentPathStrokeEnd = 100;

    @Nullable private Observable<ScaleXY> scale;
    private final RectF scaleRect = new RectF();
    private final Matrix scaleMatrix = new Matrix();

    private Observable<Path> path;
    private Observable<Integer> color;
    private Observable<Float> lineWidth;
    @Nullable private Observable<Float> strokeStart;
    @Nullable private Observable<Float> strokeEnd;

    private Observable<Integer> shapeAlpha;
    private Observable<Integer> transformAlpha;
    private List<AnimatableFloatValue> lineDashPattern;
    private AnimatableFloatValue lineDashPatternOffset;

    ShapeLayer(Drawable.Callback callback) {
        super(0, callback);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
    }

    void setIsStroke() {
        paint.setStyle(Paint.Style.STROKE);
        invalidateSelf();
    }

    public void setColor(Observable<Integer> color) {
        if (this.color != null) {
            this.color.removeChangeListener(colorChangedListener);
        }
        this.color = color;
        color.addChangeListener(colorChangedListener);
        onColorChanged();
    }

    private void onColorChanged() {
        paint.setColor(color.getValue());
        invalidateSelf();
    }

    public void setPath(Observable<Path> path) {
        if (this.path != null) {
            this.path.removeChangeListener(pathChangedListener);
        }

        this.path = path;
        // TODO: When the path changes, we probably have to scale it again.
        path.addChangeListener(pathChangedListener);
        onPathChanged();
    }

    void onPathChanged() {
        currentPath.reset();
        currentPath.set(path.getValue());
        onPathPropertiesChanged();
        invalidateSelf();
    }

    private void onPathPropertiesChanged() {
        boolean needsStrokeStart = strokeStart != null && strokeStart.getValue() != currentPathStrokeStart;
        boolean needsStrokeEnd = strokeEnd != null && strokeEnd.getValue() != currentPathStrokeEnd;
        boolean needsScaleX = scale != null && scale.getValue().getScaleX() != currentPathScaleX;
        boolean needsScaleY = scale != null && scale.getValue().getScaleY() != currentPathScaleY;

        if (!needsStrokeStart && !needsStrokeEnd && !needsScaleX && !needsScaleY) {
            return;
        }
        currentPath.set(path.getValue());

        if (needsScaleX || needsScaleY) {
            currentPath.computeBounds(scaleRect, true);
            currentPathScaleX = scale.getValue().getScaleX();
            currentPathScaleY = scale.getValue().getScaleY();
            scaleMatrix.setScale(currentPathScaleX, currentPathScaleY, scaleRect.centerX(), scaleRect.centerY());
            currentPath.transform(scaleMatrix, currentPath);
        }

        if (needsStrokeStart || needsStrokeEnd) {
            tempPath.set(currentPath);
            pathMeasure.setPath(tempPath, false);
            float length = pathMeasure.getLength();
            float start = length * strokeStart.getValue() / 100f;
            float end = length * strokeEnd.getValue() / 100f;

            currentPath.reset();
            // Workaround to get hardware acceleration on KitKat
            // https://developer.android.com/reference/android/graphics/PathMeasure.html#getSegment(float, float, android.graphics.Path, boolean)
            currentPath.rLineTo(0, 0);
            currentPathStrokeStart = Math.min(start, end);
            currentPathStrokeEnd = Math.max(start, end);
            pathMeasure.getSegment(
                    currentPathStrokeStart,
                    currentPathStrokeEnd,
                    currentPath,
                    true);
        }

        invalidateSelf();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (paint.getStyle() == Paint.Style.STROKE && paint.getStrokeWidth() == 0f) {
            return;
        }
        canvas.drawPath(currentPath, paint);
    }

    @Override
    public int getAlpha() {
        return paint.getAlpha();
    }

    void setShapeAlpha(Observable<Integer> shapeAlpha) {
        if (this.shapeAlpha != null) {
            this.shapeAlpha.removeChangeListener(alphaChangedListener);
        }
        this.shapeAlpha = shapeAlpha;
        shapeAlpha.addChangeListener(alphaChangedListener);
        onAlphaChanged();
    }

    void setTransformAlpha(Observable<Integer> transformAlpha) {
        if (this.transformAlpha != null) {
            this.transformAlpha.removeChangeListener(alphaChangedListener);
        }
        this.transformAlpha = transformAlpha;
        transformAlpha.addChangeListener(alphaChangedListener);
        onAlphaChanged();
    }

    private void onAlphaChanged() {
        Integer shapeAlpha = this.shapeAlpha == null ? 255 : this.shapeAlpha.getValue();
        Integer transformAlpha = this.transformAlpha == null ? 255 : this.transformAlpha.getValue();
        setAlpha((int) ((shapeAlpha / 255f * transformAlpha / 255f) * 255));
    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        paint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    void setLineWidth(Observable<Float> lineWidth) {
        if (this.lineWidth != null) {
            this.lineWidth.removeChangeListener(lineWidthChangedListener);
        }
        this.lineWidth = lineWidth;
        lineWidth.addChangeListener(lineWidthChangedListener);
        onLineWidthChanged();
    }

    private void onLineWidthChanged() {
        paint.setStrokeWidth(lineWidth.getValue());
        invalidateSelf();
    }

    void setDashPattern(List<AnimatableFloatValue> lineDashPattern, AnimatableFloatValue offset) {
        if (this.lineDashPattern != null) {
            this.lineDashPattern.get(0).getObservable().removeChangeListener(dashPatternChangedListener);
            this.lineDashPattern.get(1).getObservable().removeChangeListener(dashPatternChangedListener);
        }
        if (this.lineDashPatternOffset != null) {
            this.lineDashPatternOffset.getObservable().removeChangeListener(dashPatternChangedListener);
        }
        if (lineDashPattern.isEmpty()) {
            return;
        }
        this.lineDashPattern = lineDashPattern;
        this.lineDashPatternOffset = offset;
        lineDashPattern.get(0).getObservable().addChangeListener(dashPatternChangedListener);
        if (!lineDashPattern.get(1).equals(lineDashPattern.get(1))) {
            lineDashPattern.get(1).getObservable().addChangeListener(dashPatternChangedListener);
        }
        offset.getObservable().addChangeListener(dashPatternChangedListener);
        onDashPatternChanged();
    }

    private void onDashPatternChanged() {
        float[] values = new float[lineDashPattern.size()];
        for (int i = 0; i < lineDashPattern.size(); i++) {
            values[i] = lineDashPattern.get(i).getObservable().getValue();
            if (values[i] == 0) {
                values[i] = 0.01f;
            }
        }
        paint.setPathEffect(new DashPathEffect(values, lineDashPatternOffset.getObservable().getValue()));
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

    void setTrimPath(Observable<Float> strokeStart, Observable<Float> strokeEnd) {
        if (this.strokeStart != null) {
            this.strokeStart.removeChangeListener(pathPropertyChangedListener);
        }
        if (this.strokeEnd != null) {
            this.strokeEnd.removeChangeListener(pathPropertyChangedListener);
        }
        this.strokeStart = strokeStart;
        this.strokeEnd = strokeEnd;
        strokeStart.addChangeListener(pathPropertyChangedListener);
        strokeEnd.addChangeListener(pathPropertyChangedListener);
        onPathPropertiesChanged();
    }

    void setScale(@SuppressWarnings("NullableProblems") Observable<ScaleXY> scale) {
        if (this.scale != null) {
            this.scale.removeChangeListener(pathPropertyChangedListener);
        }
        this.scale = scale;
        scale.addChangeListener(pathPropertyChangedListener);
        onPathPropertiesChanged();
    }
}
