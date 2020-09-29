package com.airbnb.lottie.animation.keyframe;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.airbnb.lottie.L;
import com.airbnb.lottie.value.Keyframe;
import com.airbnb.lottie.value.LottieValueCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * @param <K> Keyframe type
 * @param <A> Animation type
 */
public abstract class BaseKeyframeAnimation<K, A> {
  public interface AnimationListener {
    void onValueChanged();
  }

  // This is not a Set because we don't want to create an iterator object on every setProgress.
  final List<AnimationListener> listeners = new ArrayList<>(1);
  private boolean isDiscrete = false;

  private final KeyframesWrapper<K> keyframesWrapper;
  protected float progress = 0f;
  @Nullable protected LottieValueCallback<A> valueCallback;

  @Nullable private A cachedGetValue = null;

  private float cachedStartDelayProgress = -1f;
  private float cachedEndProgress = -1f;

  BaseKeyframeAnimation(List<? extends Keyframe<K>> keyframes) {
    keyframesWrapper = wrap(keyframes);
  }

  public void setIsDiscrete() {
    isDiscrete = true;
  }

  public void addUpdateListener(AnimationListener listener) {
    listeners.add(listener);
  }

  public void setProgress(@FloatRange(from = 0f, to = 1f) float progress) {
    if (keyframesWrapper.isEmpty()) {
      return;
    }
    if (progress < getStartDelayProgress()) {
      progress = getStartDelayProgress();
    } else if (progress > getEndProgress()) {
      progress = getEndProgress();
    }

    if (progress == this.progress) {
      return;
    }
    this.progress = progress;
    if (keyframesWrapper.isValueChanged(progress)) {
      notifyListeners();
    }
  }

  public void notifyListeners() {
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).onValueChanged();
    }
  }

  protected Keyframe<K> getCurrentKeyframe() {
    L.beginSection("BaseKeyframeAnimation#getCurrentKeyframe");
    final Keyframe<K> keyframe = keyframesWrapper.getCurrentKeyframe();
    L.endSection("BaseKeyframeAnimation#getCurrentKeyframe");
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
  protected float getInterpolatedCurrentKeyframeProgress() {
    Keyframe<K> keyframe = getCurrentKeyframe();
    if (keyframe.isStatic()) {
      return 0f;
    }
    //noinspection ConstantConditions
    return keyframe.interpolator.getInterpolation(getLinearCurrentKeyframeProgress());
  }

  @FloatRange(from = 0f, to = 1f)
  private float getStartDelayProgress() {
      if (cachedStartDelayProgress == -1f) {
        cachedStartDelayProgress = keyframesWrapper.getStartDelayProgress();
      }
      return cachedStartDelayProgress;
  }

  @FloatRange(from = 0f, to = 1f)
  float getEndProgress() {
      if (cachedEndProgress == -1f) {
        cachedEndProgress = keyframesWrapper.getEndProgress();
      }
      return cachedEndProgress;
  }

  public A getValue() {
    float progress = getInterpolatedCurrentKeyframeProgress();
    if (valueCallback == null && keyframesWrapper.isCachedValueEnabled(progress)) {
      return cachedGetValue;
    }

    final Keyframe<K> keyframe = getCurrentKeyframe();
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

  private static <T> KeyframesWrapper<T> wrap(List<? extends Keyframe<T>> keyframes) {
    if (keyframes.isEmpty()) {
      return new EmptyKeyframeWrapper<>();
    }
    if (keyframes.size() == 1) {
      return new SingleKeyframeWrapper<>(keyframes);
    }
    return new KeyframesWrapperImpl<>(keyframes);
  }

  private interface KeyframesWrapper<T> {
    boolean isEmpty();

    boolean isValueChanged(float progress);

    Keyframe<T> getCurrentKeyframe();

    @FloatRange(from = 0f, to = 1f)
    float getStartDelayProgress();

    @FloatRange(from = 0f, to = 1f)
    float getEndProgress();

    boolean isCachedValueEnabled(float interpolatedProgress);
  }

  private static final class EmptyKeyframeWrapper<T> implements KeyframesWrapper<T> {
    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public boolean isValueChanged(float progress) {
      return false;
    }

    @Override
    public Keyframe<T> getCurrentKeyframe() {
      throw new IllegalStateException("not implemented");
    }

    @Override
    public float getStartDelayProgress() {
      return 0f;
    }

    @Override
    public float getEndProgress() {
      return 1f;
    }

    @Override
    public boolean isCachedValueEnabled(float interpolatedProgress) {
      throw new IllegalStateException("not implemented");
    }
  }

  private static final class SingleKeyframeWrapper<T> implements KeyframesWrapper<T> {
    @NonNull
    private final Keyframe<T> keyframe;
    private float cachedInterpolatedProgress = -1f;

    SingleKeyframeWrapper(List<? extends Keyframe<T>> keyframes) {
      this.keyframe = keyframes.get(0);
    }

    @Override
    public boolean isEmpty() {
      return false;
    }

    @Override
    public boolean isValueChanged(float progress) {
      return !keyframe.isStatic();
    }

    @Override
    public Keyframe<T> getCurrentKeyframe() {
      return keyframe;
    }

    @Override
    public float getStartDelayProgress() {
      return keyframe.getStartProgress();
    }

    @Override
    public float getEndProgress() {
      return keyframe.getEndProgress();
    }

    @Override
    public boolean isCachedValueEnabled(float interpolatedProgress) {
      if (cachedInterpolatedProgress == interpolatedProgress) {
        return true;
      }
      cachedInterpolatedProgress = interpolatedProgress;
      return false;
    }
  }

  private static final class KeyframesWrapperImpl<T> implements KeyframesWrapper<T> {
    private final List<? extends Keyframe<T>> keyframes;
    @NonNull
    private Keyframe<T> currentKeyframe;
    private Keyframe<T> cachedCurrentKeyframe = null;
    private float cachedInterpolatedProgress = -1f;

    KeyframesWrapperImpl(List<? extends Keyframe<T>> keyframes) {
      this.keyframes = keyframes;
      currentKeyframe = findKeyframe(0);
    }

    @Override
    public boolean isEmpty() {
      return false;
    }

    @Override
    public boolean isValueChanged(float progress) {
      if (currentKeyframe.containsProgress(progress)) {
        return !currentKeyframe.isStatic();
      }
      currentKeyframe = findKeyframe(progress);
      return true;
    }

    private Keyframe<T> findKeyframe(float progress) {
      Keyframe<T> keyframe = keyframes.get(keyframes.size() - 1);
      if (progress >= keyframe.getStartProgress()) {
        return keyframe;
      }
      for (int i = keyframes.size() - 2; i >= 1; i--) {
        keyframe = keyframes.get(i);
        if (currentKeyframe == keyframe) {
          continue;
        }
        if (keyframe.containsProgress(progress)) {
          return keyframe;
        }
      }
      return keyframes.get(0);
    }

    @Override
    @NonNull
    public Keyframe<T> getCurrentKeyframe() {
      return currentKeyframe;
    }

    @Override
    public float getStartDelayProgress() {
      return keyframes.get(0).getStartProgress();
    }

    @Override
    public float getEndProgress() {
      return keyframes.get(keyframes.size() - 1).getEndProgress();
    }

    @Override
    public boolean isCachedValueEnabled(float interpolatedProgress) {
      if (cachedCurrentKeyframe == currentKeyframe
              && cachedInterpolatedProgress == interpolatedProgress) {
        return true;
      }
      cachedCurrentKeyframe = currentKeyframe;
      cachedInterpolatedProgress = interpolatedProgress;
      return false;
    }
  }
}
