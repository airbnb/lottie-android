package com.airbnb.lottie.animation;

import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.LocaleList;

import androidx.annotation.NonNull;

/**
 * Custom paint that doesn't set text locale.
 * It takes ~1ms on initialization and isn't needed so removing it speeds up
 * setComposition.
 */
public class LPaint extends Paint {
  public LPaint() {
    super();
  }

  public LPaint(int flags) {
    super(flags);
  }

  public LPaint(PorterDuff.Mode porterDuffMode) {
    super();
    setXfermode(new PorterDuffXfermode(porterDuffMode));
  }

  public LPaint(int flags, PorterDuff.Mode porterDuffMode) {
    super(flags);
    setXfermode(new PorterDuffXfermode(porterDuffMode));
  }

  @Override
  public void setTextLocales(@NonNull LocaleList locales) {
    // Do nothing.
  }
}
