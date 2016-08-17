package com.airbnb.lotte.layers;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
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
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawPath(path, paint);
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
