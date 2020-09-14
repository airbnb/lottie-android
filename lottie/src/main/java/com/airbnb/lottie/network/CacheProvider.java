package com.airbnb.lottie.network;

import java.io.File;

import androidx.annotation.NonNull;

/**
 * Interface for providing the custom cache directory
 */
public interface CacheProvider {

  /**
   * Called during cache operations
   *
   * @return cache directory
   */
  @NonNull File getCacheDir();
}