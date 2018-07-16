package com.airbnb.lottie.model;

import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;

import com.airbnb.lottie.LottieAnimationView.CacheStrategy;
import com.airbnb.lottie.LottieComposition;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class LottieCompositionCache {

  private static final LottieCompositionCache INSTANCE = new LottieCompositionCache();

  public static LottieCompositionCache getInstance() {
    return INSTANCE;
  }

  private final Map<String, LottieComposition> strongRefCache = new HashMap<>();
  private final Map<String, WeakReference<LottieComposition>> weakRefCache = new HashMap<>();

  @VisibleForTesting
  LottieCompositionCache() {
  }

  @Nullable
  public LottieComposition getRawRes(@RawRes int rawRes) {
    return get(Integer.toString(rawRes));
  }

  @Nullable
  public LottieComposition get(String assetName) {
    if (strongRefCache.containsKey(assetName)) {
      return strongRefCache.get(assetName);
    } else if (weakRefCache.containsKey(assetName)) {
      WeakReference<LottieComposition> compRef = weakRefCache.get(assetName);
      return compRef.get();
    }
    return null;
  }

  public void put(@RawRes int rawRes, @Nullable LottieComposition composition, CacheStrategy cacheStrategy) {
    put(Integer.toString(rawRes), composition, cacheStrategy);
  }

  public void put(String cacheKey, @Nullable LottieComposition composition, CacheStrategy cacheStrategy) {
    if (cacheStrategy == CacheStrategy.Strong) {
      strongRefCache.put(cacheKey, composition);
    } else if (cacheStrategy == CacheStrategy.Weak) {
      weakRefCache.put(cacheKey, new WeakReference<LottieComposition>(composition));
    }
  }
}
