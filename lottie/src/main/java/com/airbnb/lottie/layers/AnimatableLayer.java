package com.airbnb.lottie.layers;

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

import com.airbnb.lottie.animatable.AnimationGroup;
import com.airbnb.lottie.utils.ScaleXY;
import com.airbnb.lottie.animatable.Observable;

import java.util.ArrayList;
import java.util.List;

public class AnimatableLayer extends Drawable {

    final List<AnimatableLayer> layers = new ArrayList<>();
    private final Observable.OnChangedListener changedListener = new Observable.OnChangedListener() {
        @Override
        public void onChanged() {
            invalidateSelf();
        }
    };

    private Observable<PointF> position;
    private Observable<PointF> anchorPoint;
    /** This should mimic CALayer#transform */
    private Observable<ScaleXY> transform;
    private Observable<Integer> alpha;
    private Observable<Float> rotation;
    final long compDuration;

    private final Paint solidBackgroundPaint = new Paint();
    private final List<AnimationGroup> animations = new ArrayList<>();
    @FloatRange(from = 0f, to = 1f) private float progress;

    AnimatableLayer(long compDuration, Drawable.Callback callback) {
        setCallback(callback);
        this.compDuration = compDuration;

        solidBackgroundPaint.setAlpha(0);
        solidBackgroundPaint.setStyle(Paint.Style.FILL);
    }

    void setBackgroundColor(@ColorInt int color) {
        solidBackgroundPaint.setColor(color);
        solidBackgroundPaint.setAlpha(Color.alpha(color));
        invalidateSelf();
    }

    void addAnimation(AnimationGroup animation) {
        animations.add(animation);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.save();
        if (position != null && position.getValue() != null) {
            if (position.getValue().x != 0 || position.getValue().y != 0) {
                canvas.translate(position.getValue().x, position.getValue().y);
            }
        }
        if (transform != null && transform.getValue() != null) {
            if (transform.getValue().getScaleX() != 1f || transform.getValue().getScaleY() != 1f) {
                canvas.scale(transform.getValue().getScaleX(), transform.getValue().getScaleY());
            }
        }

        if (rotation != null && rotation.getValue() != null) {
            float rotation = this.rotation.getValue();
            if (rotation != 0f) {
                canvas.rotate(rotation);
            }
        }

        if (anchorPoint != null && anchorPoint.getValue() != null) {
            if (anchorPoint.getValue().x != 0 || anchorPoint.getValue().y != 0) {
                canvas.translate(-anchorPoint.getValue().x, -anchorPoint.getValue().y);
            }
        }

        if (solidBackgroundPaint.getAlpha() != 0) {
            canvas.drawRect(getBounds(), solidBackgroundPaint);
        }
        for (int i = 0; i < layers.size(); i++) {
            layers.get(i).draw(canvas);
        }
        canvas.restore();
    }

    @Override
    public void setAlpha(int alpha) {
        throw new IllegalArgumentException("This shouldn't be used.");
    }

    void setAlpha(Observable<Integer> alpha) {
        this.alpha = alpha;
        invalidateSelf();
    }

    @Override
    public int getAlpha() {
        return alpha.getValue();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    void setAnchorPoint(Observable<PointF> anchorPoint) {
        if (this.anchorPoint != null) {
            this.anchorPoint.removeChangeListener(changedListener);
        }
        this.anchorPoint = anchorPoint;
        anchorPoint.addChangeListener(changedListener);
    }

    void setPosition(Observable<PointF> position) {
        if (this.position != null) {
            this.position.removeChangeListener(changedListener);
        }
        this.position = position;
        position.addChangeListener(changedListener);
    }

    void setTransform(Observable<ScaleXY> transform) {
        if (this.transform != null) {
            this.transform.removeChangeListener(changedListener);
        }
        this.transform = transform;
        transform.addChangeListener(changedListener);
    }

    void setRotation(Observable<Float> rotation) {
        if (this.rotation != null) {
            this.rotation.removeChangeListener(changedListener);
        }
        this.rotation = rotation;
        rotation.addChangeListener(changedListener);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    void addLayer(AnimatableLayer layer) {
        layers.add(layer);
        layer.setProgress(progress);
        invalidateSelf();
    }

    public void setProgress(@FloatRange(from = 0f, to = 1f) float progress) {
        this.progress = progress;
        for (int i = 0; i < animations.size(); i++) {
            animations.get(i).setProgress(progress);
        }

        for (int i = 0; i < layers.size(); i++) {
            layers.get(i).setProgress(progress);
        }
    }

    public float getProgress() {
        return progress;
    }
}
