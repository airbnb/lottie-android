package com.airbnb.lotte.layers;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import com.airbnb.lotte.utils.LotteTransform3D;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "WeakerAccess"})
public class LotteAnimatableLayer extends Drawable {
    private static final String TAG = LotteAnimatableLayer.class.getSimpleName();

    protected final List<Drawable> layers = new ArrayList<>();

    /** This should mimic CALayer#position */
    protected PointF position;
    /** This should mimic CALayer#anchorPoint */
    protected PointF anchorPoint;
    /** This should mimic CALayer#transform */
    protected LotteTransform3D transform;
    /** This should mimic CALayer#sublayerTransform */
    protected LotteTransform3D sublayerTransform;
    protected long duration;
    protected float speed;

    private final Paint solidBackgroundPaint = new Paint();

    public LotteAnimatableLayer(long duration) {
        this.duration = duration;

        solidBackgroundPaint.setAlpha(0);
        solidBackgroundPaint.setStyle(Paint.Style.FILL);
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setBackgroundColor(@ColorInt int color) {
        solidBackgroundPaint.setColor(color);
        invalidateSelf();
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.save();
        if (position != null) {
            canvas.translate(position.x, position.y);
        }
        if (transform != null) {
            canvas.scale(transform.getScaleX(), transform.getScaleY());
        }

        if (sublayerTransform != null) {
            canvas.rotate(sublayerTransform.getRotationZ());
        }

        if (anchorPoint != null) {
            canvas.translate(-anchorPoint.x, -anchorPoint.y);
        }

        if (solidBackgroundPaint.getAlpha() != 0) {
            canvas.drawRect(getBounds(), solidBackgroundPaint);
        }
        for (Drawable layer : layers) {
            layer.draw(canvas);
        }
        canvas.restore();
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
        layers.add(layer);
        int width = Math.max(getBounds().width(), layer.getBounds().width());
        int height = Math.max(getBounds().height(), layer.getBounds().height());
        setBounds(0, 0, width, height);
        invalidateSelf();
    }
}
