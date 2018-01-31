package com.airbnb.lottie.value;

import com.airbnb.lottie.utils.MiscUtils;

/**
 * {@link LottieValueCallback} that provides a value offset from the original animation
 * rather than an absolute value.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class LottieRelativeIntegerValueCallback extends LottieValueCallback<Integer> {
  @Override
  public Integer getValue(LottieFrameInfo<Integer> frameInfo) {
    int originalValue = MiscUtils.lerp(
        frameInfo.getStartValue(),
        frameInfo.getEndValue(),
        frameInfo.getInterpolatedKeyframeProgress()
    );
    int newValue = getOffset(frameInfo);
    return originalValue + newValue;
  }

  /**
   * Override this to provide your own offset on every frame.
   */
  public Integer getOffset(LottieFrameInfo<Integer> frameInfo) {
    if (value == null) {
      throw new IllegalArgumentException("You must provide a static value in the constructor " +
          ", call setValue, or override getValue.");
    }
    return value;
  }
}
