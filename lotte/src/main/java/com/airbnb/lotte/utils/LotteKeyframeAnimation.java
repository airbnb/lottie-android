package com.airbnb.lotte.utils;

import android.animation.ValueAnimator;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.view.animation.Interpolator;

import com.airbnb.lotte.animation.LotteAnimatableProperty.AnimatableProperty;

import java.util.ArrayList;
import java.util.List;

public abstract class LotteKeyframeAnimation<T> {

    public interface AnimationListener<T> {
        void onValueChanged(T progress);
    }

    protected final ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
    protected final List<AnimationListener<T>> listeners = new ArrayList<>();
    protected final @AnimatableProperty int property;
    protected final List<Float> keyTimes;
    protected final long duration;
    @Nullable protected Path path;
    protected PathMeasure pathMeasure;

    protected long startDelay;
    protected boolean isDiscrete = false;
    @Nullable protected List<Interpolator> interpolators;

    private float progress;

    public LotteKeyframeAnimation(@AnimatableProperty int property, long duration, List<Float> keyTimes) {
        this.property = property;
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

    public void setIsDiscrete() {
        isDiscrete = true;
    }

    public LotteKeyframeAnimation setInterpolators(@Nullable List<Interpolator> interpolators) {
        if (interpolators != null && interpolators.size() != (keyTimes.size() - 1)) {
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
        for (AnimationListener<T> l : listeners) {
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

    public long getDuration() {
        return duration;
    }

    @FloatRange(from=0f, to=1f)
    protected float getStartDelayProgress() {
        return startDelay / (float) (startDelay + duration);
    }

    public long getStartDelay() {
        return startDelay;
    }

    public void play() {
        animator.start();
    }

    public abstract T getValueForProgress(@FloatRange(from = 0f, to = 1f) float progress);
}
