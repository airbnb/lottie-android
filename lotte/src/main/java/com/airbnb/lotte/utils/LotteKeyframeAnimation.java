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
                setProgress(animation.getAnimatedFraction());
            }
        });
    }

    public LotteKeyframeAnimation setStartDelay(long startDelay) {
        this.startDelay = startDelay;
        animator.setDuration(duration + startDelay);
        return this;
    }

    public void setIsDiscrete() {
        isDiscrete = true;
    }

    public LotteKeyframeAnimation setInterpolators(@Nullable List<Interpolator> interpolators) {
        if (interpolators != null && interpolators.size() != (keyTimes.size() - 1)) {
            throw new IllegalArgumentException("There must be 1 fewer interpolator than keytime " + interpolators.size() + " vs " + keyTimes.size());
        }
        this.interpolators = interpolators;
        return this;
    }

    public void addUpdateListener(AnimationListener<T> listener) {
        listeners.add(listener);
    }

    public void setProgress(@FloatRange(from = 0f, to = 1f) float progress) {
        if (progress < getStartDelayProgress()) {
            progress = 0f;
        } else {
            progress = (progress - getStartDelayProgress()) / getDurationProgressRange();
        }
        this.progress = progress;

        for (AnimationListener<T> l : listeners) {
            l.onValueChanged(getValueForProgress(progress));
        }
    }

    int getKeyframeIndex() {
        int keyframeIndex = 1;
        float keyTime = keyTimes.get(1);
        while (keyTime < progress && keyframeIndex < keyTimes.size() - 1) {
            keyframeIndex++;
            keyTime = keyTimes.get(keyframeIndex);
        }
        return keyframeIndex - 1;
    }


    public long getDuration() {
        return duration;
    }

    @FloatRange(from=0f, to=1f)
    protected float getStartDelayProgress() {
        return (float) startDelay / (float) (startDelay + duration);
    }

    @FloatRange(from=0f, to=1f)
    private float getDurationProgressRange() {
        return 1f - getStartDelayProgress();
    }

    public long getStartDelay() {
        return startDelay;
    }

    public void play() {
        animator.start();
    }

    public abstract T getValueForProgress(@FloatRange(from = 0f, to = 1f) float progress);
}
