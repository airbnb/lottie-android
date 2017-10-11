package com.airbnb.lottie.utils;

import android.animation.ValueAnimator;

/**
 * This is a slightly modified {@link ValueAnimator} that allows us to update start and end values
 * easily optimizing for the fact that we know that it's a value animator with 2 floats.
 */
public class LottieValueAnimator extends ValueAnimator {
  private boolean systemAnimationsAreDisabled = false;
  private long compositionDuration;
  private float minValue = 0f;
  private float maxValue = 1f;
  private float speed = 1f;

  private float value = 0f;

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

  public void setValue(float value) {
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

  public void setMinValue(float minValue) {
    if (minValue >= maxValue) {
      throw new IllegalArgumentException("Min value must be smaller then max value.");
    }
    this.minValue = minValue;
    updateValues();
  }

  public void setMaxValue(float maxValue) {
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
