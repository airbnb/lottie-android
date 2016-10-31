package com.airbnb.lottie.animation;

import android.support.annotation.FloatRange;
import android.view.animation.Interpolator;

import java.util.ArrayList;
import java.util.List;

public abstract class KeyframeAnimation<T> {

    public interface AnimationListener<T> {
        void onValueChanged(T progress);
    }

    private final List<AnimationListener<T>> listeners = new ArrayList<>();
    private final long compDuration;
    private final long duration;
    final List<Float> keyTimes;

    private long startDelay;
    boolean isDiscrete = false;
    final List<Interpolator> interpolators;

    float progress;

    private int cachedKeyframeIndex = -1;
    private float cachedKeyframeIndexStart;
    private float cachedKeyframeIndexEnd;
    private float cachedDurationEndProgress = Float.MIN_VALUE;

    KeyframeAnimation(long duration, long compDuration, List<Float> keyTimes, List<Interpolator> interpolators) {
        this.duration = duration;
        this.compDuration = compDuration;
        this.keyTimes = keyTimes;
        this.interpolators = interpolators;
        if (!interpolators.isEmpty() && interpolators.size() != (keyTimes.size() - 1)) {
            throw new IllegalArgumentException("There must be 1 fewer interpolator than keytime " + interpolators.size() + " vs " + keyTimes.size());
        }
    }

    public void setStartDelay(long startDelay) {
        this.startDelay = startDelay;
        cachedDurationEndProgress = Float.MIN_VALUE;
    }

    public void setIsDiscrete() {
        isDiscrete = true;
    }

    public void addUpdateListener(AnimationListener<T> listener) {
        listeners.add(listener);
    }

    public void setProgress(@FloatRange(from = 0f, to = 1f) float progress) {
        if (progress < getStartDelayProgress()) {
            progress = 0f;
        } else if (progress > getDurationEndProgress()) {
            progress = 1f;
        } else {
            progress = (progress - getStartDelayProgress()) / getDurationRangeProgress();
        }
        if (progress == this.progress) {
            return;
        }
        this.progress = progress;

        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onValueChanged(getValue());
        }
    }

    int getKeyframeIndex() {
        int keyframeIndex = 1;
        if (cachedKeyframeIndex != -1 && progress >= cachedKeyframeIndexStart && progress <= cachedKeyframeIndexEnd) {
            keyframeIndex = cachedKeyframeIndex;
        } else {
            float keyTime = keyTimes.get(1);
            while (keyTime < progress && keyframeIndex < keyTimes.size() - 1) {
                keyframeIndex++;
                keyTime = keyTimes.get(keyframeIndex);
            }
            cachedKeyframeIndex = keyframeIndex;
            cachedKeyframeIndexStart = keyTimes.get(cachedKeyframeIndex - 1);
            cachedKeyframeIndexEnd = keyTimes.get(cachedKeyframeIndex);
        }

        return keyframeIndex - 1;
    }

    @FloatRange(from=0f, to=1f)
    private float getStartDelayProgress() {
        return (float) startDelay / (float) (compDuration);
    }

    @FloatRange(from=0f, to=1f)
    private float getDurationEndProgress() {
        if (cachedDurationEndProgress == Float.MIN_VALUE) {
            // This was taking a surprisingly long time according to systrace. Cache it!
            cachedDurationEndProgress = getStartDelayProgress() + getDurationRangeProgress();
        }
        return cachedDurationEndProgress;
    }

    @FloatRange(from=0f, to=1f)
    private float getDurationRangeProgress() {
        return (float) duration / (float) compDuration;
    }

    protected abstract T getValue();
}
