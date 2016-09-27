package com.airbnb.lotte.layers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;

import com.airbnb.lotte.animation.LotteAnimationGroup;
import com.airbnb.lotte.utils.LotteTransform3D;
import com.airbnb.lotte.utils.Observable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "WeakerAccess"})
public class LotteAnimatableLayer extends Drawable {
    private static final String TAG = LotteAnimatableLayer.class.getSimpleName();

    protected final List<LotteAnimatableLayer> layers = new ArrayList<>();

    protected final Observable.OnChangedListener changedListener = new Observable.OnChangedListener() {
        @Override
        public void onChanged() {
            invalidateSelf();
        }
    };

    /** This should mimic CALayer#position */
    private Observable<PointF> position;
    /** This should mimic CALayer#anchorPoint */
    protected Observable<PointF> anchorPoint;
    /** This should mimic CALayer#transform */
    private Observable<LotteTransform3D> transform;
    /** This should mimic CALayer#sublayerTransform */
    private Observable<Number> alpha;
    protected Observable<Number> sublayerTransform;
    protected long duration;
    protected float speed;

    private final Paint solidBackgroundPaint = new Paint();
    protected final List<LotteAnimationGroup> animations = new ArrayList<>();

    public LotteAnimatableLayer(long duration, Drawable.Callback callback) {
        setCallback(callback);
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
        if (position != null && position.getValue() != null) {
            canvas.translate(position.getValue().x, position.getValue().y);
        }
        if (transform != null && transform.getValue() != null) {
            canvas.scale(transform.getValue().getScaleX(), transform.getValue().getScaleY());
        }

        if (sublayerTransform != null && sublayerTransform.getValue() != null) {
            canvas.rotate((float) sublayerTransform.getValue());
        }

        if (anchorPoint != null && anchorPoint.getValue() != null) {
            canvas.translate(-anchorPoint.getValue().x, -anchorPoint.getValue().y);
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
        throw new IllegalArgumentException("This shouldn't be used.");
    }

    public void setAlpha(Observable<Number> alpha) {
        this.alpha = alpha;
        invalidateSelf();
    }

    @Override
    public int getAlpha() {
        return (int) alpha.getValue();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    public void setPosition(Observable<PointF> position) {
        if (this.position != null) {
            this.position.removeChangeListemer(changedListener);
        }
        this.position = position;
        position.addChangeListener(changedListener);
    }

    public void setTransform(Observable<LotteTransform3D> transform) {
        if (this.transform != null) {
            this.transform.removeChangeListemer(changedListener);
        }
        this.transform = transform;
        transform.addChangeListener(changedListener);
    }

    public void setSublayerTransform(Observable<Number> sublayerTransform) {
        if (this.sublayerTransform != null) {
            this.sublayerTransform.removeChangeListemer(changedListener);
        }
        this.sublayerTransform = sublayerTransform;
        sublayerTransform.addChangeListener(changedListener);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public void addLayer(LotteAnimatableLayer layer) {
        layers.add(layer);
        int width = Math.max(getBounds().width(), layer.getBounds().width());
        int height = Math.max(getBounds().height(), layer.getBounds().height());
        setBounds(0, 0, width, height);
        invalidateSelf();
    }

    public void play() {
        for (LotteAnimationGroup animation : animations) {
            animation.play();
        }

        for (Drawable layer : layers) {
            if (layer instanceof LotteAnimatableLayer) {
                ((LotteAnimatableLayer) layer).play();
            }
        }
    }

    public void setProgress(@FloatRange(from = 0f, to = 1f) float progress) {
        for (LotteAnimationGroup animation : animations) {
            animation.setProgress(progress);
        }

        for (LotteAnimatableLayer layer : layers) {
            layer.setProgress(progress);
        }
    }
}
