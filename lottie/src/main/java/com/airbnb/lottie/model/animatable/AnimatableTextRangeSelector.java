package com.airbnb.lottie.model.animatable;

import androidx.annotation.Nullable;
import com.airbnb.lottie.model.content.TextRangeUnits;

/**
 * Defines an animated range of text that should have an [AnimatableTextProperties] applied to it.
 */
public class AnimatableTextRangeSelector {
  @Nullable public final AnimatableIntegerValue start;
  @Nullable public final AnimatableIntegerValue end;
  @Nullable public final AnimatableIntegerValue offset;
  public final TextRangeUnits units;

  public AnimatableTextRangeSelector(
      @Nullable AnimatableIntegerValue start,
      @Nullable AnimatableIntegerValue end,
      @Nullable AnimatableIntegerValue offset,
      TextRangeUnits units) {
    this.start = start;
    this.end = end;
    this.offset = offset;
    this.units = units;
  }
}
