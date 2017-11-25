package com.airbnb.lottie.value;

import android.graphics.ColorFilter;

import com.airbnb.lottie.animation.content.ColorFilterableContent;
import com.airbnb.lottie.animation.content.Content;

public class ColorFilterValue extends LottieValue<ColorFilter> {

  /**
   * Adds a color filter to an existing fill, stroke, solid, or image.
   *
   * For more information on ColorFilters, reference the Android documentation here:
   * https://developer.android.com/reference/android/graphics/PorterDuffColorFilter.html
   *
   * Lottie provides a {@link com.airbnb.lottie.SimpleColorFilter} that replaces the current
   * color with the ColorFilter color.
   *
   * This color filter will be set on the {@link android.graphics.Paint} object that draws
   * the fill, stroke, solid, or image.
   *
   * Because this is added to the paint and not a property of the original animation, there are
   * no keyframes. You may repeatedly call this in your own animation loop if you would like though.
   */
  public static ColorFilterValue forColor(ColorFilter colorFilter) {
    return new ColorFilterValue(colorFilter);
  }

  private ColorFilterValue(ColorFilter value) {
    super(value, 0, true);
  }

  @Override public void offsetValue(ColorFilter value) {
    throw new UnsupportedOperationException("You cannot offset color filters.");
  }

  @Override public void apply(Content content) {
    if (content instanceof ColorFilterableContent) {
      ((ColorFilterableContent) content).setColorFilter(getValue());
    }
  }
}
