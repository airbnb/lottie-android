package com.airbnb.lottie.configurations.reducemotion;

import android.content.Context;
import androidx.annotation.Nullable;
import com.airbnb.lottie.utils.Utils;

/**
 * Lottie animations with a "reduced motion" marker will play that marker instead of any other animations.
 * This class uses {@link com.airbnb.lottie.utils.Utils#getAnimationScale(Context)} to determine if animations are disabled
 * and if it should play the reduced motion marker.
 *
 * If the animation is provided a "reduced motion"
 * marker name, they will be shown instead of the first or last frame. Supported marker names are case insensitive, and include:
 * - reduced motion
 * - reducedMotion
 * - reduced_motion
 * - reduced-motion
 */
public class SystemReducedMotionOption implements ReducedMotionOption {

  @Override
  public ReducedMotionMode getCurrentReducedMotionMode(@Nullable Context context) {
    if (context == null || Utils.getAnimationScale(context) != 0f) {
      return ReducedMotionMode.STANDARD_MOTION;
    } else {
      return ReducedMotionMode.REDUCED_MOTION;
    }
  }
}
