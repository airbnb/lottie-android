package com.airbnb.lottie;

import android.os.Build;

public enum LottieFeatureFlag {
  /**
   * Merge paths currently don't work if the the operand shape is entirely contained within the
   * first shape. If you need to cut out one shape from another shape, use an even-odd fill type
   * instead of using merge paths.
   */
  MergePathsApi19(Build.VERSION_CODES.KITKAT);

  public final int minRequiredSdkVersion;

  LottieFeatureFlag(int minRequiredSdkVersion) {
    this.minRequiredSdkVersion = minRequiredSdkVersion;
  }
}
