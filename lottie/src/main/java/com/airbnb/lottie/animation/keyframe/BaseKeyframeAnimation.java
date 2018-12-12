package com.airbnb.lottie.animation.keyframe;

import com.airbnb.lottie.value.Keyframe;
import com.airbnb.lottie.value.LottieValueCallback;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.FloatRange;
import androidx.annotation.Nullable;

/**
 * @param <K> Keyframe type
 * @param <A> Animation type
 */
public abstract class BaseKeyframeAnimation<K, A> {
  public interface AnimationListener {
    void onValueChanged();
  }

  // This is not a Set because we don't want to create an iterator object on every setProgress.
  final List<AnimationListener> listeners = new ArrayList<>();
  private boolean isDiscrete = false;

  private final List<? extends Keyframe<K>> keyframes;
  private float progress = 0f;
  @Nullable protected LottieValueCallback<A> valueCallback;

  @Nullable private Keyframe<K> cachedKeyframe;

  @Nullable private Keyframe<K> cachedGetValueKeyframe;
  private float cachedGetValueProgress = -1f;
  @Nullable private A cachedGetValue = null;

  BaseKeyframeAnimation(List<? extends Keyframe<K>> keyframes) {
    this.keyframes = keyframes;
  }

  public void setIsDiscrete() {
    isDiscrete = true;
  }

  public void addUpdateListener(AnimationListener listener) {
    listeners.add(listener);
  }

  public void setProgress(@FloatRange(from = 0f, to = 1f) float progress) {
    // Must use hashCode() since the actual object instance will be returned
    // from getValue() below with the new values.
    int previousValue = valueCallback == null ? 0 : getValue().hashCode();
    if (progress < getStartDelayProgress()) {
      progress = getStartDelayProgress();
    } else if (progress > getEndProgress()) {
      progress = getEndProgress();
    }

    if (progress == this.progress) {
      return;
    }
    this.progress = progress;
    // Just trigger a change but don't compute values if there is a value callback.
    int newValue = valueCallback == null ? -1 : getValue().hashCode();

    if (previousValue != newValue) {
      notifyListeners();
    }
  }

  public void notifyListeners() {
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).onValueChanged();
    }
  }

  private Keyframe<K> getCurrentKeyframe() {
    if (cachedKeyframe != null && cachedKeyframe.containsProgress(progress)) {
      return cachedKeyframe;
    }

    Keyframe<K> keyframe = keyframes.get(keyframes.size() - 1);
    if (progress < keyframe.getStartProgress()) {
      for (int i = keyframes.size() - 1; i >= 0; i--) {
        keyframe = keyframes.get(i);
        if (keyframe.containsProgress(progress)) {
          break;
        }
      }
    }

    cachedKeyframe = keyframe;
    return keyframe;
  }

  /**
   * Returns the progress into the current keyframe between 0 and 1. This does not take into account
   * any interpolation that the keyframe may have.
   */
  float getLinearCurrentKeyframeProgress() {
    if (isDiscrete) {
      return 0f;
    }

    Keyframe<K> keyframe = getCurrentKeyframe();
    if (keyframe.isStatic()) {
      return 0f;
    }
    float progressIntoFrame = progress - keyframe.getStartProgress();
    float keyframeProgress = keyframe.getEndProgress() - keyframe.getStartProgress();
    return progressIntoFrame / keyframeProgress;
  }

  /**
   * Takes the value of {@link #getLinearCurrentKeyframeProgress()} and interpolates it with
   * the current keyframe's interpolator.
   */
  private float getInterpolatedCurrentKeyframeProgress() {
    Keyframe<K> keyframe = getCurrentKeyframe();
    if (keyframe.isStatic()) {
      return 0f;
    }
    //noinspection ConstantConditions
    return keyframe.interpolator.getInterpolation(getLinearCurrentKeyframeProgress());
  }

  @FloatRange(from = 0f, to = 1f)
  private float getStartDelayProgress() {
    return keyframes.isEmpty() ? 0f : keyframes.get(0).getStartProgress();
  }

  @FloatRange(from = 0f, to = 1f)
  float getEndProgress() {
    return keyframes.isEmpty() ? 1f : keyframes.get(keyframes.size() - 1).getEndProgress();
  }

  public A getValue() {
    Keyframe<K> keyframe = getCurrentKeyframe();
    float progress = getInterpolatedCurrentKeyframeProgress();
    if (valueCallback != null && keyframe == cachedGetValueKeyframe && cachedGetValueProgress == progress) {
      return cachedGetValue;
    }

    cachedGetValueKeyframe = keyframe;
    cachedGetValueProgress = progress;
    A value = getValue(keyframe, progress);
    cachedGetValue = value;

    return value;
  }

  public float getProgress() {
    return progress;
  }

  public void setValueCallback(@Nullable LottieValueCallback<A> valueCallback) {
    if (this.valueCallback != null) {
      this.valueCallback.setAnimation(null);
    }
    this.valueCallback = valueCallback;
    if (valueCallback != null) {
      valueCallback.setAnimation(this);
    }
  }

  /**
   * keyframeProgress will be [0, 1] unless the interpolator has overshoot in which case, this
   * should be able to handle values outside of that range.
   */
  abstract A getValue(Keyframe<K> keyframe, float keyframeProgress);
}
