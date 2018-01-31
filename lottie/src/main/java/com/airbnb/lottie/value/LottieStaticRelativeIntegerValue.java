package com.airbnb.lottie.value;

/**
 * Static value version of {@link LottieRelativeIntegerValueCallback}.
 */
public class LottieStaticRelativeIntegerValue extends LottieRelativeIntegerValueCallback {

  private final Integer offset;

  public LottieStaticRelativeIntegerValue(Integer offset) {
    this.offset = offset;
  }


  @Override
  public Integer getOffset(LottieFrameInfo<Integer> frameInfo) {
    return offset;
  }
}
