package com.airbnb.lottie.network;


import java.io.File;

import androidx.annotation.NonNull;

/**
 * Interface for providing the custom cache directory where animations downloaded via url are saved.
 * @see com.airbnb.lottie.Lottie#initialize
 */
public interface LottieNetworkCacheProvider {

  /**
   * Called during cache operations
   *
   * @return cache directory
   */
  @NonNull File getCacheDir();
}