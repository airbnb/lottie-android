package com.airbnb.lottie.model;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;
import androidx.collection.LruCache;

import com.airbnb.lottie.LottieComposition;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class LottieCompositionCache {

  private static final LottieCompositionCache INSTANCE = new LottieCompositionCache();

  public static LottieCompositionCache getInstance() {
    return INSTANCE;
  }

  private final LruCache<String, LottieComposition> cache = new LruCache<>(20);

  @VisibleForTesting LottieCompositionCache() {
  }

  @Nullable
  public LottieComposition get(@Nullable String cacheKey) {
    if (cacheKey == null) {
      return null;
    }
    return cache.get(cacheKey);
  }

  public void put(@Nullable String cacheKey, LottieComposition composition) {
    if (cacheKey == null) {
      return;
    }
    cache.put(cacheKey, composition);
  }

  public void clear() {
    cache.evictAll();
  }

  /**
   * Set the maximum number of compositions to keep cached in memory.
   * This must be {@literal >} 0.
   */
  public void resize(int size) {
    cache.resize(size);
  }
}
