package com.airbnb.lottie.configurations.reducemotion;

import android.content.Context;

public interface ReducedMotionOption {

  /**
   * Returns the current reduced motion mode.
   */
  ReducedMotionMode getCurrentReducedMotionMode(Context context);
}
