package com.airbnb.lottie.configurations.reducemotion;


public enum ReducedMotionMode {
  /**
   * The default behavior where Lottie animations play normally with no overrides.
   * By default this mode is used when {@link com.airbnb.lottie.utils.Utils#getAnimationScale(Context)} is not 0.
   */
  STANDARD_MOTION,

  /**
   * Lottie animations with a "reduced motion" marker will play that marker instead of any other animations.
   * By default this mode is used when {@link com.airbnb.lottie.utils.Utils#getAnimationScale(Context)} == 0.
   */
  REDUCED_MOTION
}
