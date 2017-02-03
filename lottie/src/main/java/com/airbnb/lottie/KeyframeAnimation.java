package com.airbnb.lottie;

import android.support.annotation.FloatRange;
import android.view.animation.Interpolator;

import java.util.ArrayList;
import java.util.List;

abstract class KeyframeAnimation<T> {
  interface AnimationListener<T> {
    void onValueChanged(T progress);
  }

  private final List<AnimationListener<T>> listeners = new ArrayList<>();
  private final long duration;
  private final LottieComposition composition;
  final List<Float> keyTimes;

  private long startDelay;
  boolean isDiscrete = false;
  final List<Interpolator> interpolators;

  float progress;

  private int cachedKeyframeIndex = -1;
  private float cachedKeyframeIndexStart;
  private float cachedKeyframeIndexEnd;
  private float cachedDurationEndProgress = Float.MIN_VALUE;

  KeyframeAnimation(long duration, LottieComposition composition, List<Float> keyTimes,
      List<Interpolator> interpolators) {
    this.duration = duration;
    this.composition = composition;
    this.keyTimes = keyTimes;
    this.interpolators = interpolators;
    if (!interpolators.isEmpty() && interpolators.size() != (keyTimes.size() - 1)) {
      throw new IllegalArgumentException(
          "There must be 1 fewer interpolator than keytime " + interpolators.size() + " vs " +
              keyTimes.size());
    }
  }

  void setStartDelay(long startDelay) {
    this.startDelay = startDelay;
    cachedDurationEndProgress = Float.MIN_VALUE;
  }

  void setIsDiscrete() {
    isDiscrete = true;
  }

  void addUpdateListener(AnimationListener<T> listener) {
    listeners.add(listener);
  }

  void removeUpdateListener(AnimationListener<T> listener) {
    listeners.remove(listener);
  }

  void setProgress(@FloatRange(from = 0f, to = 1f) float progress) {
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

    T value = getValue();
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).onValueChanged(value);
    }
  }

  int getKeyframeIndex() {
    int keyframeIndex = 1;
    if (cachedKeyframeIndex != -1 && progress >= cachedKeyframeIndexStart &&
        progress <= cachedKeyframeIndexEnd) {
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

  @FloatRange(from = 0f, to = 1f)
  private float getStartDelayProgress() {
    return (float) startDelay / (float) (composition.getDuration());
  }

  @FloatRange(from = 0f, to = 1f)
  private float getDurationEndProgress() {
    if (cachedDurationEndProgress == Float.MIN_VALUE) {
      // This was taking a surprisingly long time according to systrace. Cache it!
      cachedDurationEndProgress = getStartDelayProgress() + getDurationRangeProgress();
    }
    return cachedDurationEndProgress;
  }

  @FloatRange(from = 0f, to = 1f)
  private float getDurationRangeProgress() {
    return (float) duration / (float) composition.getDuration();
  }

  abstract T getValue();
}
