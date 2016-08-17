package com.airbnb.lotte.layers;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;

/**
 * Mimics CAShapeLayer
 */
public class LotteShapeLayer extends Drawable {

    private final Paint paint = new Paint();

    private Path path;
    @IntRange(from=0, to=255) private int alpha;

    public LotteShapeLayer() {
        paint.setStyle(Paint.Style.FILL);
    }

    public int getFillColor() {
        return paint.getColor();
    }

    public void setFillColor(@ColorInt int fillColor) {
        paint.setColor(fillColor);
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
    public void setAlpha(@IntRange(from=0, to=255) int alpha) {
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
}
