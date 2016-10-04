package com.airbnb.lotte.utils;

import android.graphics.Path;
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

    private final List<AnimationListener<T>> listeners = new ArrayList<>();
    protected final @AnimatableProperty int property;
    private final long compDuration;
    protected final List<Float> keyTimes;
    protected final long duration;
    @Nullable protected Path path;

    private long startDelay;
    boolean isDiscrete = false;
    @Nullable protected List<Interpolator> interpolators;

    private float progress;

    public LotteKeyframeAnimation(@AnimatableProperty int property, long duration, long compDuration, List<Float> keyTimes) {
        this.property = property;
        this.duration = duration;
        this.compDuration = compDuration;
        this.keyTimes = keyTimes;
    }

    public LotteKeyframeAnimation setStartDelay(long startDelay) {
        this.startDelay = startDelay;
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
        } else if (progress > getDurationEndProgress()){
            progress = 1f;
        } else {
            progress = (progress - getStartDelayProgress()) / getDurationRangeProgress();
        }
        this.progress = progress;

        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onValueChanged(getValueForProgress(progress));
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
        return (float) startDelay / (float) (compDuration);
    }

    @FloatRange(from=0f, to=1f)
    private float getDurationEndProgress() {
        return getStartDelayProgress() + getDurationRangeProgress();
    }

    @FloatRange(from=0f, to=1f)
    private float getDurationRangeProgress() {
        return (float) duration / (float) compDuration;
    }

    public abstract T getValueForProgress(@FloatRange(from = 0f, to = 1f) float progress);
}
