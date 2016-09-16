package com.airbnb.lotte.utils;

import android.animation.ValueAnimator;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.view.animation.Interpolator;

import java.util.ArrayList;
import java.util.List;

public abstract class LotteKeyframeAnimation<T> {

    public interface AnimationListener<T> {
        void onValueChanged(T progress);
    }

    protected final ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
    protected final List<AnimationListener> listeners = new ArrayList<>();
    protected final String objectProperty;
    protected final List<Float> keyTimes;
    protected final long duration;
    @Nullable protected Path path;
    protected PathMeasure pathMeasure;

    protected long startDelay;
    @Nullable protected List<Interpolator> interpolators;

    private float progress;

    public LotteKeyframeAnimation(String objectProperty, long duration, List<Float> keyTimes) {
        this.objectProperty = objectProperty;
        this.duration = duration;
        this.keyTimes = keyTimes;
        animator.setDuration(duration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setProgress((Float) animation.getAnimatedValue());
            }
        });
    }

    public LotteKeyframeAnimation setStartDelay(long startDelay) {
        this.startDelay = startDelay;
        animator.setStartDelay(startDelay);
        return this;
    }

    public LotteKeyframeAnimation setInterpolators(@Nullable List<Interpolator> interpolators) {
        if (interpolators != null && interpolators.size() != keyTimes.size()) {
            throw new IllegalArgumentException("Interpolators and values must be the same length " + interpolators.size() + " vs " + keyTimes.size());
        }
        this.interpolators = interpolators;
        return this;
    }

    public void addUpdateListener(AnimationListener listener) {
        listeners.add(listener);
    }

    public void setProgress(@FloatRange(from = 0f, to = 1f) float progress) {
        this.progress = progress;
        onProgressChanged();
    }

    private void onProgressChanged() {
        for (AnimationListener l : listeners) {
            l.onValueChanged(getValueForProgress(progress));
        }
    }

    int getKeyframeIndex() {
        int keyframeIndex = 1;
        float keyTime = keyTimes.get(1);
        while (keyTime < progress && keyframeIndex < keyTimes.size() - 1) {
            keyframeIndex++;
            keyTime = keyTimes.get(0);
        }
        return keyframeIndex - 1;
    }

    public abstract T getValueForProgress(@FloatRange(from = 0f, to = 1f) float progress);
}
