package com.airbnb.lottie.utils;

import android.animation.ValueAnimator;
import android.support.annotation.FloatRange;

/**
 * This is a slightly modified {@link ValueAnimator} that allows us to update start and end values
 * easily optimizing for the fact that we know that it's a value animator with 2 floats.
 */
public class LottieValueAnimator extends ValueAnimator {
  private boolean systemAnimationsAreDisabled = false;
  private long compositionDuration;
  private float speed = 1f;
  @FloatRange(from = 0f, to = 1f) private float value = 0f;
  @FloatRange(from = 0f, to = 1f) private float minValue = 0f;
  @FloatRange(from = 0f, to = 1f) private float maxValue = 1f;

  public LottieValueAnimator() {
    setInterpolator(null);
    addUpdateListener(new AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(ValueAnimator animation) {
        // On older devices, getAnimatedValue and getAnimatedFraction
        // will always return 0 if animations are disabled.
        if (!systemAnimationsAreDisabled) {
          value = (float) animation.getAnimatedValue();
        }
      }
    });
    updateValues();
  }

  public void systemAnimationsAreDisabled() {
    this.systemAnimationsAreDisabled = true;
  }

  public void setCompositionDuration(long compositionDuration) {
    this.compositionDuration = compositionDuration;
    updateValues();
  }

  /**
   * Sets the current animator value. This will update the play time as well.
   * It will also be clamped to the values set with {@link #setMinValue(float)} and
   * {@link #setMaxValue(float)}.
   */
  public void setValue(@FloatRange(from = 0f, to = 1f) float value) {
    value = MiscUtils.clamp(value, minValue, maxValue);

    this.value = value;
    float distFromStart = isReversed() ? (maxValue - value) : (value - minValue);
    float range = Math.abs(maxValue - minValue);
    float animatedPercentage = distFromStart / range;
    if (getDuration() > 0) {
      setCurrentPlayTime((long) (getDuration() * animatedPercentage));
    }
  }

  public float getValue() {
    return value;
  }

  public void setMinAndMaxValues(
      @FloatRange(from = 0f, to = 1f) float minValue,
      @FloatRange(from = 0f, to = 1f) float maxValue) {
    this.minValue = minValue;
    this.maxValue = maxValue;
    updateValues();
  }

  public void setMinValue(@FloatRange(from = 0f, to = 1f) float minValue) {
    if (minValue >= maxValue) {
      throw new IllegalArgumentException("Min value must be smaller then max value.");
    }
    this.minValue = minValue;
    updateValues();
  }

  public float getMinValue() {
    return minValue;
  }

  public void setMaxValue(@FloatRange(from = 0f, to = 1f) float maxValue) {
    if (maxValue <= minValue) {
      throw new IllegalArgumentException("Max value must be greater than min value.");
    }
    this.maxValue = maxValue;
    updateValues();
  }

  public void reverseAnimationSpeed() {
    setSpeed(-getSpeed());
  }

  public void setSpeed(float speed) {
    this.speed = speed;
    updateValues();
  }

  public float getSpeed() {
    return speed;
  }

  public void playAnimation() {
    start();
    setValue(isReversed() ? maxValue : minValue);
  }

  public void pauseAnimation() {
    float value = this.value;
    cancel();
    setValue(value);
  }

  public void resumeAnimation() {
    float value = this.value;
    if (isReversed() && this.value == minValue) {
      value = maxValue;
    } else if (!isReversed() && this.value == maxValue) {
      value = minValue;
    }
    start();
    setValue(value);
  }

  private boolean isReversed() {
    return speed < 0;
  }

  /**
   * Update the float values of the animator, scales the duration for the current min/max range
   * and updates the play time so that it matches the new min/max range.
   */
  private void updateValues() {
    setDuration((long) (compositionDuration * (maxValue - minValue) / Math.abs(speed)));
    setFloatValues(
        speed < 0 ? maxValue : minValue,
        speed < 0 ? minValue : maxValue
    );
    // This will force the play time to be correct for the current value.
    setValue(value);
  }
}
