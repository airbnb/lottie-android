package com.airbnb.lottie.network;


import androidx.annotation.NonNull;

import java.io.File;

/**
 * Interface for providing the custom cache directory where animations downloaded via url are saved.
 *
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