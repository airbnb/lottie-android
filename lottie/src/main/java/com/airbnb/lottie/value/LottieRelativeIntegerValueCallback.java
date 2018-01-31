package com.airbnb.lottie.value;

import com.airbnb.lottie.utils.MiscUtils;

/**
 * {@link LottieValueCallback} that provides a value offset from the original animation
 * rather than an absolute value.
 */
public abstract class LottieRelativeIntegerValueCallback extends LottieValueCallback<Integer> {
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

  public abstract Integer getOffset(LottieFrameInfo<Integer> frameInfo);
}
