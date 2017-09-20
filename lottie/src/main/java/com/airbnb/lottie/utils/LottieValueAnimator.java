package com.airbnb.lottie.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.support.annotation.FloatRange;

/**
 * This is a slightly modified {@link ValueAnimator} that allows us to update start and end values
 * easily optimizing for the fact that we know that it's a value animator with 2 floats.
 */
public class LottieValueAnimator extends ValueAnimator {
  private boolean systemAnimationsAreDisabled = false;
  private boolean isReversed = false;
  private float minProgress = 0f;
  private float maxProgress = 1f;
  private long originalDuration;

  private float progress = 0f;

  public LottieValueAnimator() {
    setFloatValues(0f, 1f);

    /*
      This allows us to reset the values if they were temporarily reset by
      updateValues(float, float, long, boolean)
     */
    addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        updateValues(minProgress, maxProgress);
      }

      @Override public void onAnimationCancel(Animator animation) {
        updateValues(minProgress, maxProgress);
      }
    });

    addUpdateListener(new AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(ValueAnimator animation) {
        if (!systemAnimationsAreDisabled) {
          // On older devices, getAnimatedValue and getAnimatedFraction
          // will always return 0 if animations are disabled.
          progress = (float) animation.getAnimatedValue();
        }
      }
    });
  }

  @Override public void start() {
    if (systemAnimationsAreDisabled) {
      setProgress(getMaxProgress());
      end();
    } else {
      super.start();
    }
  }

  public void systemAnimationsAreDisabled() {
    this.systemAnimationsAreDisabled = true;
  }

  @Override public ValueAnimator setDuration(long duration) {
    this.originalDuration = duration;
    updateValues(minProgress, maxProgress);
    return this;
  }

  /**
   * This progress is from 0 to 1 and doesn't take into account setMinProgress or setMaxProgress.
   * In other words, if you have set the min and max progress to 0.2 and 0.4, setting this to
   * 0.5f will set the progress to 0.5, not 0.3. However, the value will be clamped between 0.2 and
   * 0.4 so the resulting progress would be 0.4.
   */
  public void setProgress(@FloatRange(from = 0f, to = 1f) float progress) {
    if (this.progress == progress) {
      return;
    }
    setProgressInternal(progress);
  }

  /**
   * Forces the animation to update even if the progress hasn't changed.
   */
  public void forceUpdate() {
    setProgressInternal(getProgress());
  }

  private void setProgressInternal(@FloatRange(from = 0f, to = 1f) float progress) {
    if (progress < minProgress) {
      progress = minProgress;
    } else if (progress > maxProgress) {
      progress = maxProgress;
    }
    this.progress = progress;
    if (getDuration() > 0) {
      float offsetProgress = (progress - minProgress) / (maxProgress - minProgress);
      setCurrentPlayTime((long) (getDuration() * offsetProgress));
    }
  }

  public float getProgress() {
    return progress;
  }


  public void setIsReversed(boolean isReversed) {
    this.isReversed = isReversed;
    updateValues(minProgress, maxProgress);
  }

  public void setMinProgress(float minProgress) {
    this.minProgress = minProgress;
    updateValues(minProgress, maxProgress);
  }

  public void setMaxProgress(float maxProgress) {
    this.maxProgress = maxProgress;
    updateValues(minProgress, maxProgress);
  }

  public float getMinProgress() {
    return minProgress;
  }

  public float getMaxProgress() {
    return maxProgress;
  }

  public void resumeAnimation() {
    float startingProgress = progress;
    start();
    // This has to call through setCurrentPlayTime for compatibility reasons.
    setProgress(startingProgress);
  }

  /**
   * This lets you set the start and end progress for a single play of the animator. After the next
   * time the animation ends or is cancelled, the values will be reset to those set by
   * {@link #setMinProgress(float)} or {@link #setMaxProgress(float)}.
   */
  @SuppressWarnings("WeakerAccess")
  public void updateValues(float startProgress, float endProgress) {
    float minValue = Math.min(startProgress, endProgress);
    float maxValue = Math.max(startProgress, endProgress);
    setFloatValues(
        isReversed ? maxValue : minValue,
        isReversed ? minValue : maxValue
    );
    super.setDuration((long) (originalDuration * (maxValue - minValue)));
    setProgress(getProgress());
  }
}
