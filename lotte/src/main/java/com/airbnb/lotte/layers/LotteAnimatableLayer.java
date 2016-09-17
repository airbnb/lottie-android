package com.airbnb.lotte.layers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.airbnb.lotte.animation.LotteAnimationGroup;
import com.airbnb.lotte.utils.LotteTransform3D;
import com.airbnb.lotte.utils.Observable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "WeakerAccess"})
public class LotteAnimatableLayer extends Drawable {
    private static final String TAG = LotteAnimatableLayer.class.getSimpleName();

    protected final List<Drawable> layers = new ArrayList<>();

    /** This should mimic CALayer#position */
    protected final Observable<PointF> position = new Observable<>();
    /** This should mimic CALayer#anchorPoint */
    protected PointF anchorPoint;
    /** This should mimic CALayer#transform */
    protected LotteTransform3D transform;
    /** This should mimic CALayer#sublayerTransform */
    protected LotteTransform3D sublayerTransform;
    protected long duration;
    protected float speed;
    @IntRange(from=0, to=255) private int alpha = 255;

    private final Paint solidBackgroundPaint = new Paint();
    private final List<LotteAnimationGroup> animations = new ArrayList<>();

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
        solidBackgroundPaint.setAlpha(Color.alpha(color));
        invalidateSelf();
    }

    public void addAnimation(LotteAnimationGroup animation) {
        animations.add(animation);
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
            canvas.translate(position.getValue().x, position.getValue().y);
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
        this.alpha = alpha;
        invalidateSelf();
    }

    @Override
    public int getAlpha() {
        return alpha;
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
