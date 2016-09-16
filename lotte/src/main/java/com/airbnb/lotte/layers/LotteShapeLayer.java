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
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;

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

    private float scaleX = 1f;
    private float scaleY = 1f;
    private final RectF scaleRect = new RectF();
    private final Matrix scaleMatrix = new Matrix();
    private Path scaledPath = new Path();

    private Path path;
    @IntRange(from = 0, to = 255) private int alpha;
    private float strokeStart = -1f;
    private float strokeEnd = -1f;

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
        return scaledPath;
    }

    public void setPath(Path path) {
        this.path = path;
        setScale(scaleX, scaleY);
        pathMeasure.setPath(scaledPath, false);
        // Cache for perf.
        pathLength = pathMeasure.getLength();
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        if (strokeStart != -1f && strokeEnd != -1f) {
            pathMeasure.getSegment(pathLength * (strokeStart / 100f), pathLength * (strokeEnd / 100f), trimPath, true);
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

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        paint.setAlpha(alpha);
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

    public void setDashPattern(List<Float> lineDashPattern, float offset) {
        if (lineDashPattern.isEmpty()) {
            return;
        }
        float[] values = new float[lineDashPattern.size()];
        for (int i = 0; i < lineDashPattern.size(); i++) {
            values[i] = lineDashPattern.get(i);
        }
        paint.setPathEffect(new DashPathEffect(values, offset));
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

    public void setStrokeEnd(float strokeEnd) {
        this.strokeEnd = strokeEnd;
    }

    public void setStrokeStart(float strokeStart) {
        this.strokeStart = strokeStart;
    }

    public void setScale(float scaleX, float scaleY) {
        path.computeBounds(scaleRect, true);
        scaleMatrix.setScale(scaleX, scaleY, scaleRect.centerX(), scaleRect.centerY());
        path.transform(scaleMatrix, scaledPath);
    }
}
