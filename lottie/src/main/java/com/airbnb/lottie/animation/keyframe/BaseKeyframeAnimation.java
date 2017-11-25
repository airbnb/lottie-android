package com.airbnb.lottie.animation.keyframe;

import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;

import com.airbnb.lottie.animation.Keyframe;

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
  final List<AnimationListener> listeners = new ArrayList<>();
  private boolean isDiscrete = false;

  private final List<? extends Keyframe<K>> keyframes;
  private float progress = 0f;

  @Nullable private Keyframe<K> cachedKeyframe;

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
    if (progress < getStartDelayProgress()) {
      progress = 0f;
    } else if (progress > getEndProgress()) {
      progress = 1f;
    }

    if (progress == this.progress) {
      return;
    }
    this.progress = progress;

    notifyListeners();
  }

  private Keyframe<K> getCurrentKeyframe() {
    if (keyframes.isEmpty()) {
      throw new IllegalStateException("There are no keyframes");
    }

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

  public float getProgress() {
    return progress;
  }

  void notifyListeners() {
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).onValueChanged();
    }
  }

  /**
   * This will traverse the keyframes and either update the value in an exiting keyframe if
   * updateValue is true or add a new keyframe if updateValue is false.
   */
  public void setValue(K value, int frame, boolean updateValue) {
    if (updateValue) {
      updateValue(value, frame);
    }
  }

  /**
   * Traverses the keyframes. When it finds the keyframe that contains the given frame, it will
   * determine whether it is closer to the beginning or end of the keyframe. It will then update
   * the closer of the two. If the neighboring value in the previous/next keyframe was identical
   * to the original value, it will be updated as well to keep the values contiguous.
   */
  private void updateValue(K value, int frame) {
    for (int i = 0; i < keyframes.size(); i++) {
      Keyframe<K> keyframe = keyframes.get(i);
      if (keyframe.containsFrame(frame)) {
        // We found the keyframe that has the desired keyframe.
        if (Math.abs(frame - keyframe.startFrame) < Math.abs(frame - keyframe.endFrame)) {
          // We are closer to the beginning of the frame.
          if (i > 0) {
            Keyframe<K> lastKeyframe = keyframes.get(i - 1);
            // If the previous keyframe had the same end value as the keyframe start value, make
            // sure it gets updated to the new value as well.
            //noinspection ConstantConditions
            if (lastKeyframe.endValue.equals(keyframe.startValue)) {
              lastKeyframe.endValue = value;
            }
            updatePathIfNeeded(lastKeyframe);
          }
          keyframe.startValue = value;
          updatePathIfNeeded(keyframe);
          notifyListeners();
        } else {
          // We are closer to the end of the keyframe.
          if (i < keyframes.size() - 1) {
            Keyframe<K> nextKeyframe = keyframes.get(i + 1);
            // If the next keyframe had the same start value as the keyframe end value, make sure
            // it gets updated to the new value as well.
            //noinspection ConstantConditions
            if (nextKeyframe.startValue.equals(keyframe.endValue)) {
              nextKeyframe.startValue = value;
            }
            updatePathIfNeeded(nextKeyframe);
          }
          keyframe.endValue = value;
          updatePathIfNeeded(keyframe);
          notifyListeners();
        }
      }
    }
  }

  /**
   * PathKeyframes generates a path at the beginning and then just uses that instead of
   * startValue and endValue like other keyframes.
   */
  private void updatePathIfNeeded(Keyframe<K> keyframe) {
    if (keyframe instanceof PathKeyframe) {
      ((PathKeyframe) keyframe).updatePath();
    }
  }

  /**
   * keyframeProgress will be [0, 1] unless the interpolator has overshoot in which case, this
   * should be able to handle values outside of that range.
   */
  abstract A getValue(Keyframe<K> keyframe, float keyframeProgress);
}
