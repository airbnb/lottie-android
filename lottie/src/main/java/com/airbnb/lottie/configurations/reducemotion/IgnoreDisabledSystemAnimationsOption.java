package com.airbnb.lottie.configurations.reducemotion;

import android.content.Context;

/**
 * Allows ignoring system animations settings, therefore allowing animations to run even if they are disabled.
 */
public class IgnoreDisabledSystemAnimationsOption implements ReducedMotionOption {

  @Override
  public ReducedMotionMode getCurrentReducedMotionMode(Context context) {
    return ReducedMotionMode.STANDARD_MOTION;
  }
}
