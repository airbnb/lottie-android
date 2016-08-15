package com.airbnb.lotte.layers;

import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;

public class LotteAnimatableLayer extends Drawable {

    @Nullable private LayerDrawable layerDrawable;

    /** This should mimic CALayer#position */
    protected Point position;
    /** This should mimic CALayer#anchorPoint */
    protected Point anchorPoint;
    /** This should mimic CALayer#transform */
    protected Camera transform;
    /** This should mimic CALayer#sublayerTransform */
    protected Camera sublayerTransform;
    protected long duration;
    protected LotteMaskLayer mask;
    protected float speed;

    private final Paint solidBackgroundPaint = new Paint();

    public LotteAnimatableLayer(long duration) {
        this.duration = duration;

        solidBackgroundPaint.setStyle(Paint.Style.FILL);
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setBackgroundColor(@ColorInt int color) {
        solidBackgroundPaint.setColor(color);
        invalidateSelf();
    }

    public void setMask(LotteMaskLayer mask) {
        this.mask = mask;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawRect(getBounds(), solidBackgroundPaint);
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public void addLayer(Drawable layer) {
        if (layerDrawable == null) {
            layerDrawable = new LayerDrawable(new Drawable[]{layer});
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            layerDrawable.addLayer(layer);
        } else {
            int count = layerDrawable.getNumberOfLayers();
            Drawable[] drawables = new Drawable[count + 1];
            for (int i = 0; i < count; i++) {
                drawables[i] = layerDrawable.getDrawable(i);
            }
            drawables[count] = layer;
            layerDrawable = new LayerDrawable(drawables);
        }
        int width = Math.max(getBounds().width(), layer.getBounds().width());
        int height = Math.max(getBounds().height(), layer.getBounds().height());
        setBounds(0, 0, width, height);
        invalidateSelf();
    }
}
