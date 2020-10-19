package com.airbnb.lottie;

import androidx.annotation.NonNull;

/**
 * Class for initializing the library with custom config
 */
public class Lottie {

  private Lottie() {
  }

  public static void initialize(@NonNull final LottieConfig lottieConfig) {
    L.setFetcher(lottieConfig.networkFetcher);
    L.setCacheProvider(lottieConfig.cacheProvider);
  }
}