package com.airbnb.lottie.model.animatable;

import androidx.annotation.Nullable;

/**
 * Defines an animated range of text that should have an [AnimatableTextProperties] applied to it.
 */
public class AnimatableTextRangeSelector {
  @Nullable public final AnimatableIntegerValue start;
  @Nullable public final AnimatableIntegerValue end;
  @Nullable public final AnimatableIntegerValue offset;

  public AnimatableTextRangeSelector(
      @Nullable AnimatableIntegerValue start,
      @Nullable AnimatableIntegerValue end,
      @Nullable AnimatableIntegerValue offset
  ) {
    this.start = start;
    this.end = end;
    this.offset = offset;
  }
}
