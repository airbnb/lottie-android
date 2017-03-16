package com.airbnb.lottie;

import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @param <K> Keyframe type
 * @param <A> Animation type
 */
abstract class BaseKeyframeAnimation<K, A> {
  interface AnimationListener {
    void onValueChanged();
  }

  final List<AnimationListener> listeners = new ArrayList<>();
  private boolean isDiscrete = false;

  private final List<? extends Keyframe<K>> keyframes;
  private float progress = 0f;

  @Nullable private Keyframe<K> cachedKeyframe;

  BaseKeyframeAnimation(List<? extends Keyframe<K>> keyframes) {
    this.keyframes = keyframes;
  }

  void setIsDiscrete() {
    isDiscrete = true;
  }

  void addUpdateListener(AnimationListener listener) {
    listeners.add(listener);
  }

  void setProgress(@FloatRange(from = 0f, to = 1f) float progress) {
    if (progress < getStartDelayProgress()) {
      progress = 0f;
    } else if (progress > getEndProgress()) {
      progress = 1f;
    }

    if (progress == this.progress) {
      return;
    }
    this.progress = progress;

    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).onValueChanged();
    }
  }

  private Keyframe<K> getCurrentKeyframe() {
    if (keyframes.isEmpty()) {
      throw new IllegalStateException("There are no keyframes");
    }

    if (cachedKeyframe != null && cachedKeyframe.containsProgress(progress)) {
      return cachedKeyframe;
    }

    int i = 0;
    Keyframe<K> keyframe = keyframes.get(0);
    if (progress < keyframe.getStartProgress()) {
      cachedKeyframe = keyframe;
      return keyframe;
    }

    while (!keyframe.containsProgress(progress) && i < keyframes.size()) {
      keyframe = keyframes.get(i);
      i++;
    }
    cachedKeyframe = keyframe;
    return keyframe;
  }

  /**
   * This wil be [0, 1] unless the interpolator has overshoot in which case getValue() should be
   * able to handle values outside of that range.
   */
  private float getCurrentKeyframeProgress() {
    if (isDiscrete) {
      return 0f;
    }

    Keyframe<K> keyframe = getCurrentKeyframe();
    if (keyframe.isStatic()) {
      return 0f;
    }
    float progressIntoFrame = progress - keyframe.getStartProgress();
    float keyframeProgress = keyframe.getEndProgress() - keyframe.getStartProgress();
    //noinspection ConstantConditions
    return keyframe.interpolator.getInterpolation(progressIntoFrame / keyframeProgress);
  }

  @FloatRange(from = 0f, to = 1f)
  private float getStartDelayProgress() {
    return keyframes.isEmpty() ? 0f : keyframes.get(0).getStartProgress();
  }

  @FloatRange(from = 0f, to = 1f)
  private float getEndProgress() {
    return keyframes.isEmpty() ? 1f : keyframes.get(keyframes.size() - 1).getEndProgress();
  }

  public A getValue() {
    return getValue(getCurrentKeyframe(), getCurrentKeyframeProgress());
  }

  float getProgress() {
    return progress;
  }

  /**
   * keyframeProgress will be [0, 1] unless the interpolator has overshoot in which case, this
   * should be able to handle values outside of that range.
   */
  abstract A getValue(Keyframe<K> keyframe, float keyframeProgress);
}
