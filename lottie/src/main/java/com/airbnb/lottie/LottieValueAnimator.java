package com.airbnb.lottie;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;

/**
 * This is a slightly modified {@link ValueAnimator} that allows us to update start and end values
 * easily optimizing for the fact that we know that it's a value animator with 2 floats.
 */
class LottieValueAnimator extends ValueAnimator {
  private boolean isReversed = false;
  private float startProgress = 0f;
  private float endProgress = 1f;
  private long duration;

  LottieValueAnimator() {
    setFloatValues(0f, 1f);

    /*
      This allows us to reset the values if they were temporarily reset by
      updateValues(float, float, long, boolean)
     */
    addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        updateValues();
      }

      @Override public void onAnimationCancel(Animator animation) {
        updateValues();
      }
    });
  }

  @Override public ValueAnimator setDuration(long duration) {
    this.duration = duration;
    updateValues();
    return this;
  }

  @Override public long getDuration() {
    return duration;
  }

  void setIsReversed(boolean isReversed) {
    this.isReversed = isReversed;
    updateValues();
  }

  void setStartProgress(float startProgress) {
    this.startProgress = startProgress;
    updateValues();
  }

  void setEndProgress(float endProgress) {
    this.endProgress = endProgress;
    updateValues();
  }

  /**
   * This lets you set the start and end progress for a single play of the animator. After the next
   * time the animation ends or is cancelled, the values will be reset to those set by
   * {@link #setStartProgress(float)} or {@link #setEndProgress(float)}.
   */
  void updateValues(float startProgress, float endProgress) {
    float minValue = Math.min(startProgress, endProgress);
    float maxValue = Math.max(startProgress, endProgress);
    setFloatValues(
        isReversed ? maxValue : minValue,
        isReversed ? minValue : maxValue
    );
    super.setDuration((long) (duration * (maxValue - minValue)));
  }

  private void updateValues() {
    updateValues(startProgress, endProgress);
  }
}
