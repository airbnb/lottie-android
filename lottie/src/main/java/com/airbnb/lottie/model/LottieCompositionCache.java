package com.airbnb.lottie.model;

import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;
import android.support.v4.util.LruCache;

import com.airbnb.lottie.LottieAnimationView.CacheStrategy;
import com.airbnb.lottie.LottieComposition;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class LottieCompositionCache {

  private static final int CACHE_SIZE_MB = 10;
  private static final LottieCompositionCache INSTANCE = new LottieCompositionCache();

  public static LottieCompositionCache getInstance() {
    return INSTANCE;
  }

  private final LruCache<String, LottieComposition> cache = new LruCache<>(1024 * 1024 * CACHE_SIZE_MB);

  @VisibleForTesting
  LottieCompositionCache() {
  }

  @Nullable
  public LottieComposition getRawRes(@RawRes int rawRes) {
    return get(Integer.toString(rawRes));
  }

  @Nullable
  public LottieComposition get(String assetName) {
    return cache.get(assetName);
  }

  public void put(@RawRes int rawRes, @Nullable LottieComposition composition) {
    put(Integer.toString(rawRes), composition);
  }

  public void put(@Nullable String cacheKey, @Nullable LottieComposition composition) {
    if (cacheKey == null) {
      return;

    }
    cache.put(cacheKey, composition);
  }
}
