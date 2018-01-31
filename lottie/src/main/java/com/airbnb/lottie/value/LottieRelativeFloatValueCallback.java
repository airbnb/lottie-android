package com.airbnb.lottie.value;

import com.airbnb.lottie.utils.MiscUtils;

/**
 * {@link LottieValueCallback} that provides a value offset from the original animation
 * rather than an absolute value.
 */
public abstract class LottieRelativeFloatValueCallback extends LottieValueCallback<Float> {
  @Override
  public Float getValue(LottieFrameInfo<Float> frameInfo) {
    float originalValue = MiscUtils.lerp(
        frameInfo.getStartValue(),
        frameInfo.getEndValue(),
        frameInfo.getInterpolatedKeyframeProgress()
    );
    float offset = getOffset(frameInfo);
    return originalValue + offset;
  }

  public abstract Float getOffset(LottieFrameInfo<Float> frameInfo);
}
