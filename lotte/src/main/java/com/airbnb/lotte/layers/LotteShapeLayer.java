package com.airbnb.lotte.layers;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;

import com.airbnb.lotte.model.LotteShapeStroke;

import java.util.List;

/**
 * Mimics CAShapeLayer
 */
public class LotteShapeLayer extends Drawable {

    private final Paint paint = new Paint();
    private final Path trimPath = new Path();
    private PathMeasure pathMeasure = new PathMeasure();
    private float pathLength;

    private Path path;
    @IntRange(from = 0, to = 255) private int alpha;
    @Nullable private List<Float> lineDashPattern;
    private LotteShapeStroke.LineCapType lineCapType;
    private LotteShapeStroke.LineJoinType lineJoinType;
    private float strokeStart;
    private float strokeEnd;

    public LotteShapeLayer() {
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
    }

    public void setStyle(Paint.Style style) {
        paint.setStyle(style);
    }

    public int getStrokeColor() {
        return paint.getColor();
    }

    public void setColor(@ColorInt int strokeColor) {
        paint.setColor(strokeColor);
        invalidateSelf();
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
        pathMeasure.setPath(path, false);
        // Cache for perf.
        pathLength = pathMeasure.getLength();
    }

    @Override
    public void draw(Canvas canvas) {
        if (strokeStart != strokeEnd) {
            pathMeasure.getSegment(pathLength * (strokeStart / 100f), pathLength * (strokeEnd / 100f), trimPath, true);
            // Workaround to get hardware acceleration on KitKat
            // https://developer.android.com/reference/android/graphics/PathMeasure.html#getSegment(float, float, android.graphics.Path, boolean)
            trimPath.rLineTo(0, 0);
            canvas.drawPath(trimPath, paint);
        } else {
            canvas.drawPath(path, paint);
        }

    }

    @Override
    public int getAlpha() {
        return alpha;
    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        this.alpha = alpha;
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public void setLineWidth(float width) {
        paint.setStrokeWidth(width);
        invalidateSelf();
    }

    public void setDashPattern(List<Float> lineDashPattern) {
        this.lineDashPattern = lineDashPattern;
    }

    public void setLineCapType(LotteShapeStroke.LineCapType lineCapType) {
        this.lineCapType = lineCapType;
        switch (lineCapType) {
            case Butt:
                paint.setStrokeCap(Paint.Cap.BUTT);
            case Round:
            default:
                paint.setStrokeCap(Paint.Cap.ROUND);
        }
    }

    public void setLineJoinType(LotteShapeStroke.LineJoinType lineJoinType) {
        this.lineJoinType = lineJoinType;
    }

    public void setStrokeEnd(float strokeEnd) {
        this.strokeEnd = strokeEnd;
    }

    public void setStrokeStart(float strokeStart) {
        this.strokeStart = strokeStart;
    }
}
