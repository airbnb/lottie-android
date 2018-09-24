package com.airbnb.lottie;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import androidx.annotation.ColorInt;

/**
 * A color filter with a predefined transfer mode that applies the specified color on top of the
 * original color. As there are many other transfer modes, please take a look at the definition
 * of PorterDuff.Mode.SRC_ATOP to find one that suits your needs.
 * This site has a great explanation of Porter/Duff compositing algebra as well as a visual
 * representation of many of the transfer modes:
 * http://ssp.impulsetrain.com/porterduff.html
 */
@SuppressWarnings("WeakerAccess") public class SimpleColorFilter extends PorterDuffColorFilter {
  public SimpleColorFilter(@ColorInt int color) {
    super(color, PorterDuff.Mode.SRC_ATOP);
  }
}
