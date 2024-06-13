package com.airbnb.lottie;

import android.annotation.SuppressLint;
import android.os.Build;

import com.airbnb.lottie.utils.Logger;

import java.util.HashMap;
import java.util.Objects;

public class LottieFeatureFlags {
  public enum FeatureFlag {
    /**
     * Merge paths currently don't work if the the operand shape is entirely contained within the
     * first shape. If you need to cut out one shape from another shape, use an even-odd fill type
     * instead of using merge paths.
     */
    MergePath_19(Build.VERSION_CODES.KITKAT);

    public final int minRequiredSdkVersion;

    FeatureFlag(int minRequiredSdkVersion) {
      this.minRequiredSdkVersion = minRequiredSdkVersion;
    }
  }

  private HashMap<FeatureFlag, Boolean> flagValues = new HashMap<>();

  public LottieFeatureFlags() {
    for (FeatureFlag f : FeatureFlag.values()) {
      flagValues.put(f, Boolean.FALSE);
    }
  }

  @SuppressLint("DefaultLocale")
  public void enableFlag(FeatureFlag flag, boolean enable) {
    if (Objects.equals(flagValues.get(flag), enable)) {
      return;
    }
    if (Build.VERSION.SDK_INT < flag.minRequiredSdkVersion) {
      Logger.warning(String.format("%s is not supported pre SDK %d", flag.name(), flag.minRequiredSdkVersion));
      return;
    }
    flagValues.put(flag, enable);
  }

  public boolean isFlagEnabled(FeatureFlag flag) {
    return Boolean.TRUE.equals(flagValues.get(flag));
  }

}
