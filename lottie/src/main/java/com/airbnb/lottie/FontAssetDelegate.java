package com.airbnb.lottie;

import android.graphics.Typeface;

/**
 * Delegate to handle the loading of fonts that are not packaged in the assets of your app or don't
 * have the same file name.
 *
 * @see LottieDrawable#setFontAssetDelegate(FontAssetDelegate)
 */
@SuppressWarnings({"unused", "WeakerAccess"}) public class FontAssetDelegate {

  /**
   * Override this if you want to return a Typeface from a font family.
   */
  public Typeface fetchFont(String fontFamily) {
    return null;
  }

  /**
   * Override this if you want to specify the asset path for a given font family.
   */
  public String getFontPath(String fontFamily) {
    return null;
  }
}
