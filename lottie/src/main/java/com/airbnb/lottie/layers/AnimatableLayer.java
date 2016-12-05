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
import android.support.annotation.Nullable;

import com.airbnb.lottie.animation.KeyframeAnimation;
import com.airbnb.lottie.utils.ScaleXY;

import java.util.ArrayList;
import java.util.List;

public class AnimatableLayer extends Drawable {

    private final KeyframeAnimation.AnimationListener<Integer> integerChangedListener = new KeyframeAnimation.AnimationListener<Integer>() {
        @Override
        public void onValueChanged(Integer progress) {
            invalidateSelf();
        }
    };
    private final KeyframeAnimation.AnimationListener<Float> floatChangedListener = new KeyframeAnimation.AnimationListener<Float>() {
        @Override
        public void onValueChanged(Float progress) {
            invalidateSelf();
        }
    };
    private final KeyframeAnimation.AnimationListener<ScaleXY> scaleChangedListener = new KeyframeAnimation.AnimationListener<ScaleXY>() {
        @Override
        public void onValueChanged(ScaleXY progress) {
            invalidateSelf();
        }
    };
    private final KeyframeAnimation.AnimationListener<PointF> pointChangedListener = new KeyframeAnimation.AnimationListener<PointF>() {
        @Override
        public void onValueChanged(PointF progress) {
            invalidateSelf();
        }
    };

    final List<AnimatableLayer> layers = new ArrayList<>();


    private KeyframeAnimation<PointF> position;
    private KeyframeAnimation<PointF> anchorPoint;
    /** This should mimic CALayer#transform */
    private KeyframeAnimation<ScaleXY> transform;
    private KeyframeAnimation<Integer> alpha = null;
    private KeyframeAnimation<Float> rotation;
    final long compDuration;

    private final Paint solidBackgroundPaint = new Paint();
    @ColorInt private int backgroundColor;
    private final List<KeyframeAnimation<?>> animations = new ArrayList<>();
    @FloatRange(from = 0f, to = 1f) private float progress;

    AnimatableLayer(long compDuration, Drawable.Callback callback) {
        setCallback(callback);
        this.compDuration = compDuration;

        solidBackgroundPaint.setAlpha(0);
        solidBackgroundPaint.setStyle(Paint.Style.FILL);
    }

    void setBackgroundColor(@ColorInt int color) {
        this.backgroundColor = color;
        solidBackgroundPaint.setColor(color);
        invalidateSelf();
    }

    void addAnimation(KeyframeAnimation<?> newAnimation) {
        animations.add(newAnimation);
    }

    void removeAnimation(KeyframeAnimation<?> animation) {
        animations.remove(animation);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        int saveCount = canvas.save();
        applyTransformForLayer(canvas, this);

        int backgroundAlpha = Color.alpha(backgroundColor);
        if (backgroundAlpha != 0) {
            int alpha = backgroundAlpha;
            if (this.alpha != null) {
                alpha = alpha * this.alpha.getValue() / 255;
            }
            solidBackgroundPaint.setAlpha(alpha);
            canvas.drawRect(getBounds(), solidBackgroundPaint);
        }
        for (int i = 0; i < layers.size(); i++) {
            layers.get(i).draw(canvas);
        }
        canvas.restoreToCount(saveCount);
    }

    int saveCanvas(@Nullable Canvas canvas) {
        if (canvas == null) {
            return 0;
        }
        return canvas.save();
    }

    void restoreCanvas(@Nullable Canvas canvas, int count) {
        if (canvas == null) {
            return;
        }
        canvas.restoreToCount(count);
    }

    void applyTransformForLayer(@Nullable Canvas canvas, AnimatableLayer layer) {
        if (canvas == null) {
            return;
        }
        // TODO: Determine if these null checks are necessary.
        if (layer.position != null) {
            PointF position = layer.position.getValue();
            if (position.x != 0 || position.y != 0) {
                canvas.translate(position.x, position.y);
            }
        }

        if (layer.transform != null) {
            ScaleXY scale = layer.transform.getValue();
            if (scale.getScaleX() != 1f || scale.getScaleY() != 1f) {
                canvas.scale(scale.getScaleX(), scale.getScaleY());
            }
        }

        if (layer.rotation != null) {
            float rotation = layer.rotation.getValue();
            if (rotation != 0f) {
                canvas.rotate(rotation);
            }
        }

        if (layer.anchorPoint != null) {
            PointF anchorPoint = layer.anchorPoint.getValue();
            if (anchorPoint.x != 0 || anchorPoint.y != 0) {
                canvas.translate(-anchorPoint.x, -anchorPoint.y);
            }
        }
    }


    @Override
    public void setAlpha(int alpha) {
        throw new IllegalArgumentException("This shouldn't be used.");
    }

    void setAlpha(KeyframeAnimation<Integer> alpha) {
        if (this.alpha != null) {
            removeAnimation(this.alpha);
            this.alpha.removeUpdateListener(integerChangedListener);
        }
        this.alpha = alpha;
        addAnimation(alpha);
        alpha.addUpdateListener(integerChangedListener);
        for (AnimatableLayer layer : layers) {
            layer.setAlpha(alpha);
        }

        invalidateSelf();
    }

    @Override
    public int getAlpha() {
        return alpha.getValue();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    void setAnchorPoint(KeyframeAnimation<PointF> anchorPoint) {
        if (this.anchorPoint != null) {
            removeAnimation(this.anchorPoint);
            this.anchorPoint.removeUpdateListener(pointChangedListener);
        }
        this.anchorPoint = anchorPoint;
        addAnimation(anchorPoint);
        anchorPoint.addUpdateListener(pointChangedListener);
    }

    void setPosition(KeyframeAnimation<PointF> position) {
        if (this.position != null) {
            removeAnimation(this.position);
            this.position.removeUpdateListener(pointChangedListener);
        }
        this.position = position;
        addAnimation(position);
        position.addUpdateListener(pointChangedListener);
    }

    void setTransform(KeyframeAnimation<ScaleXY> transform) {
        if (this.transform != null) {
            removeAnimation(this.transform);
            this.transform.removeUpdateListener(scaleChangedListener);
        }
        this.transform = transform;
        addAnimation(this.transform);
        transform.addUpdateListener(scaleChangedListener);
    }

    void setRotation(KeyframeAnimation<Float> rotation) {
        if (this.rotation != null) {
            removeAnimation(this.rotation);
            this.rotation.removeUpdateListener(floatChangedListener);
        }
        this.rotation = rotation;
        addAnimation(this.rotation);
        rotation.addUpdateListener(floatChangedListener);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    void addLayer(AnimatableLayer layer) {
        layers.add(layer);
        layer.setProgress(progress);
        if (this.alpha != null) {
            layer.setAlpha(this.alpha);
        }
        invalidateSelf();
    }

    public void clearLayers() {
        layers.clear();
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
