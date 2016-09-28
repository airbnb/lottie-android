package com.airbnb.lotte.layers;

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

import com.airbnb.lotte.animation.LotteAnimatableNumberValue;
import com.airbnb.lotte.model.LotteShapeStroke;
import com.airbnb.lotte.utils.LotteTransform3D;
import com.airbnb.lotte.utils.Observable;

import java.util.List;

/**
 * Mimics CAShapeLayer
 */
public class LotteShapeLayer extends LotteAnimatableLayer {

    private final Observable.OnChangedListener changedListener = new Observable.OnChangedListener() {
        @Override
        public void onChanged() {
            invalidateSelf();
        }
    };

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

    private final RectF bounds = new RectF();
    private final Paint paint = new Paint();
    private final Path trimPath = new Path();
    private PathMeasure pathMeasure = new PathMeasure();
    private float pathLength;

    private Observable<LotteTransform3D> scale = new Observable<>(new LotteTransform3D());
    private final RectF scaleRect = new RectF();
    private final Matrix scaleMatrix = new Matrix();
    private Path scaledPath = new Path();

    private Observable<Path> path;
    private Observable<Integer> color;
    private Observable<Number> lineWidth;
    @Nullable private Observable<Number> strokeStart;
    @Nullable private Observable<Number> strokeEnd;

    private Observable<Number> shapeAlpha;
    private Observable<Number> transformAlpha;
    private List<LotteAnimatableNumberValue> lineDashPattern;
    private LotteAnimatableNumberValue lineDashPatternOffset;

    public LotteShapeLayer(Drawable.Callback callback) {
        super(0, callback);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        scale.getValue().scale(1f, 1f);
    }

    public void setStyle(Paint.Style style) {
        paint.setStyle(style);
    }

    public int getStrokeColor() {
        return paint.getColor();
    }

    public void setColor(Observable<Integer> color) {
        if (this.color != null) {
            this.color.removeChangeListemer(colorChangedListener);
        }
        this.color = color;
        color.addChangeListener(colorChangedListener);
        onColorChanged();
    }

    private void onColorChanged() {
        paint.setColor(color.getValue());
        invalidateSelf();
    }

    public Path getPath() {
        return scaledPath;
    }

    public void setPath(Observable<Path> path) {
        if (this.path != null) {
            this.path.removeChangeListemer(pathChangedListener);
        }

        this.path = path;
        // TODO: When the path changes, we probably have to scale it again.
        path.addChangeListener(pathChangedListener);
        onPathChanged();
    }

    private void onPathChanged() {
        if (path != null && path.getValue() != null && scale != null && scale.getValue() != null) {
            path.getValue().computeBounds(scaleRect, true);
            scaleMatrix.setScale(scale.getValue().getScaleX(), scale.getValue().getScaleY(), scaleRect.centerX(), scaleRect.centerY());
            path.getValue().transform(scaleMatrix, scaledPath);
        }
        pathMeasure.setPath(scaledPath, false);
        // Cache for perf.
        pathLength = pathMeasure.getLength();
        invalidateSelf();
        updateBounds();
    }

    private void updateBounds() {
        scaledPath.computeBounds(bounds, true);
        bounds.left -= paint.getStrokeWidth();
        bounds.top -= paint.getStrokeWidth();
        bounds.right += paint.getStrokeWidth();
        bounds.bottom += paint.getStrokeWidth();
        setBounds(0, 0, (int) bounds.width(), (int) bounds.height());
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (paint.getStyle() == Paint.Style.STROKE && paint.getStrokeWidth() == 0f) {
            return;
        }
        if (strokeStart != null && strokeEnd != null) {
            trimPath.reset();
            pathMeasure.getSegment(pathLength * (((Float) strokeStart.getValue()) / 100f), pathLength * (((Float) strokeEnd.getValue()) / 100f), trimPath, true);
            // Workaround to get hardware acceleration on KitKat
            // https://developer.android.com/reference/android/graphics/PathMeasure.html#getSegment(float, float, android.graphics.Path, boolean)
            trimPath.rLineTo(0, 0);
            canvas.drawPath(trimPath, paint);
        } else {
            canvas.drawPath(scaledPath, paint);
        }
    }

    @Override
    public int getAlpha() {
        return paint.getAlpha();
    }

    public void setShapeAlpha(Observable<Number> shapeAlpha) {
        if (this.shapeAlpha != null) {
            this.shapeAlpha.removeChangeListemer(alphaChangedListener);
        }
        this.shapeAlpha = shapeAlpha;
        shapeAlpha.addChangeListener(alphaChangedListener);
        onAlphaChanged();
    }

    public void setTransformAlpha(Observable<Number> transformAlpha) {
        if (this.transformAlpha != null) {
            this.transformAlpha.removeChangeListemer(alphaChangedListener);
        }
        this.transformAlpha = transformAlpha;
        transformAlpha.addChangeListener(alphaChangedListener);
        onAlphaChanged();
    }

    private void onAlphaChanged() {
        Float shapeAlpha = this.shapeAlpha == null ? 1f : (Float) this.shapeAlpha.getValue();
        Float transformAlpha = this.transformAlpha == null ? 1f : (Float) this.transformAlpha.getValue();
        setAlpha((int) ((shapeAlpha * transformAlpha) * 255));
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

    public void setLineWidth(Observable<Number> lineWidth) {
        if (this.lineWidth != null) {
            this.lineWidth.removeChangeListemer(lineWidthChangedListener);
        }
        this.lineWidth = lineWidth;
        lineWidth.addChangeListener(lineWidthChangedListener);
        onLineWidthChanged();
    }

    private void onLineWidthChanged() {
        paint.setStrokeWidth((float) lineWidth.getValue());
        updateBounds();
        invalidateSelf();
    }

    public void setDashPattern(List<LotteAnimatableNumberValue> lineDashPattern, LotteAnimatableNumberValue offset) {
        if (this.lineDashPattern != null) {
            this.lineDashPattern.get(0).getObservable().removeChangeListemer(dashPatternChangedListener);
            this.lineDashPattern.get(1).getObservable().removeChangeListemer(dashPatternChangedListener);
        }
        if (this.lineDashPatternOffset != null) {
            this.lineDashPatternOffset.getObservable().removeChangeListemer(dashPatternChangedListener);
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
            values[i] = (float) lineDashPattern.get(i).getObservable().getValue();
            if (values[i] == 0) {
                values[i] = 0.01f;
            }
        }
        paint.setPathEffect(new DashPathEffect(values, (float) lineDashPatternOffset.getObservable().getValue()));
        invalidateSelf();
    }

    public void setLineCapType(LotteShapeStroke.LineCapType lineCapType) {
        switch (lineCapType) {
            case Round:
                paint.setStrokeCap(Paint.Cap.ROUND);
                break;
            case Butt:
            default:
                paint.setStrokeCap(Paint.Cap.BUTT);
        }
    }

    public void setLineJoinType(LotteShapeStroke.LineJoinType lineJoinType) {
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

    public void setTrimPath(Observable<Number> strokeStart, Observable<Number> strokeEnd) {
        if (this.strokeStart != null) {
            this.strokeStart.removeChangeListemer(changedListener);
        }
        if (this.strokeEnd != null) {
            this.strokeEnd.removeChangeListemer(changedListener);
        }
        this.strokeStart = strokeStart;
        this.strokeEnd = strokeEnd;
        strokeStart.addChangeListener(changedListener);
        strokeEnd.addChangeListener(changedListener);
    }

    public void setScale(Observable<LotteTransform3D> scale) {
        this.scale = scale;
        onPathChanged();
    }
}
