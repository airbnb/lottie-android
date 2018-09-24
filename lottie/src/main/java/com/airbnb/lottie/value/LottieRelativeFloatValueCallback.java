package com.airbnb.lottie.value;

import androidx.annotation.NonNull;

import com.airbnb.lottie.utils.MiscUtils;

/**
 * {@link LottieValueCallback} that provides a value offset from the original animation
 * rather than an absolute value.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class LottieRelativeFloatValueCallback extends LottieValueCallback<Float> {

  public LottieRelativeFloatValueCallback() {
  }

  public LottieRelativeFloatValueCallback(@NonNull Float staticValue) {
    super(staticValue);
  }

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

  public Float getOffset(LottieFrameInfo<Float> frameInfo) {
    if (value == null) {
      throw new IllegalArgumentException("You must provide a static value in the constructor " +
          ", call setValue, or override getValue.");
    }
    return value;
  }
}
