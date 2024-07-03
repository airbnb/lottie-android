package com.airbnb.lottie;

import android.annotation.SuppressLint;
import android.os.Build;

import com.airbnb.lottie.utils.Logger;

import java.util.HashSet;

class LottieFeatureFlags {

  private final HashSet<LottieFeatureFlag> enabledFlags = new HashSet<>();

  /**
   * Returns true if the flag was changed.
   */
  @SuppressLint("DefaultLocale")
  public boolean enableFlag(LottieFeatureFlag flag, boolean enable) {
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

  public boolean isFlagEnabled(LottieFeatureFlag flag) {
    return enabledFlags.contains(flag);
  }

}
