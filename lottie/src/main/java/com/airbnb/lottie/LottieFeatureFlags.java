package com.airbnb.lottie;

import android.annotation.SuppressLint;
import android.os.Build;

import com.airbnb.lottie.utils.Logger;

import java.util.HashSet;

public class LottieFeatureFlags {
  public enum FeatureFlag {
    /**
     * Merge paths currently don't work if the the operand shape is entirely contained within the
     * first shape. If you need to cut out one shape from another shape, use an even-odd fill type
     * instead of using merge paths.
     */
    MergePathsApi19(Build.VERSION_CODES.KITKAT);

    public final int minRequiredSdkVersion;

    FeatureFlag(int minRequiredSdkVersion) {
      this.minRequiredSdkVersion = minRequiredSdkVersion;
    }
  }

  private final HashSet<FeatureFlag> enabledFlags = new HashSet<>();

  /**
   * Returns true if the flag was changed.
   */
  @SuppressLint("DefaultLocale")
  public boolean enableFlag(FeatureFlag flag, boolean enable) {
    if (enable) {
      if (Build.VERSION.SDK_INT < flag.minRequiredSdkVersion) {
        Logger.warning(String.format("%s is not supported pre SDK %d", flag.name(), flag.minRequiredSdkVersion));
        return false;
      }
      return enabledFlags.add(flag);
    } else {
      return enabledFlags.remove(flag);
    }
  }

  public boolean isFlagEnabled(FeatureFlag flag) {
    return enabledFlags.contains(flag);
  }

}
