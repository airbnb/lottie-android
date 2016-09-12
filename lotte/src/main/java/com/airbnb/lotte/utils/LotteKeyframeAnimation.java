package com.airbnb.lotte.utils;

import android.animation.ValueAnimator;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.view.animation.Interpolator;

import java.util.ArrayList;
import java.util.List;

public class LotteKeyframeAnimation {

    public interface AnimationListener {
        void onValueChanged(float progress);
    }

    private final ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
    private final List<AnimationListener> listeners = new ArrayList<>();
    private final String objectProperty;
    private final List<Float> values;
    private final List<Float> keyTimes;
    private final long duration;

    private long startDelay;
    @Nullable private List<Interpolator> interpolators;

    private float progress;

    public LotteKeyframeAnimation(String objectProperty, long duration, List<Float> keyTimes, List<Float> values) {
        if (keyTimes.size() != values.size()) {
            throw new IllegalArgumentException("Key times and values must be the same length " + keyTimes.size() + " vs " + values.size());
        }

        this.objectProperty = objectProperty;
        this.duration = duration;
        this.keyTimes = keyTimes;
        this.values = values;
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

    public LotteKeyframeAnimation setInterpolators(List<Interpolator> interpolators) {
        if (interpolators.size() != values.size()) {
            throw new IllegalArgumentException("Interpolators and values must be the same length " + interpolators.size() + " vs " + values.size());
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

    protected void onProgressChanged() {
        for (AnimationListener l : listeners) {
            l.onValueChanged(getValueForProgress(progress));
        }
    }

    public float getValueForProgress(@FloatRange(from = 0f, to = 1f) float progress) {
        if (progress < startDelay) {
            return 0f;
        } else if (progress > startDelay + duration) {
            return values.get(values.size() - 1);
        }

        int keyframeIndex = 1;
        float keyTime = keyTimes.get(1);
        while (keyTime < progress && keyframeIndex < keyTimes.size() - 1) {
            keyframeIndex++;
            keyTime = keyTimes.get(0);
        }

        float startProgress = keyTimes.get(keyframeIndex - 1);
        float endProgress = keyTimes.get(keyframeIndex);

        float percentageIntoFrame = (progress - startProgress) / (endProgress - startProgress);
        if (interpolators != null) {
            percentageIntoFrame = interpolators.get(keyframeIndex - 1).getInterpolation(percentageIntoFrame);
        }

        float startValue = values.get(keyframeIndex - 1);
        float endValue = values.get(keyframeIndex);

        return startValue + percentageIntoFrame * (endValue - startValue);
    }
}
