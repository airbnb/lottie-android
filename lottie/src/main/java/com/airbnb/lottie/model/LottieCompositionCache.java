package com.airbnb.lottie.model;

import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;
import android.util.SparseArray;

import com.airbnb.lottie.LottieAnimationView;
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

  private final SparseArray<LottieComposition> rawResStrongRefCache = new SparseArray<>();
  private final SparseArray<WeakReference<LottieComposition>> rawResWeakRefCache = new SparseArray<>();

  private final Map<String, LottieComposition> assetStringRefCache = new HashMap<>();
  private final Map<String, WeakReference<LottieComposition>> assetWeakRefCache = new HashMap<>();

  @VisibleForTesting
  LottieCompositionCache() {
  }

  @Nullable
  public LottieComposition getRawRes(@RawRes int rawRes) {
    if (rawResWeakRefCache.indexOfKey(rawRes) >= 0) {
      WeakReference<LottieComposition> compRef = rawResWeakRefCache.get(rawRes);
      return compRef.get();
    } else if (rawResStrongRefCache.indexOfKey(rawRes) >= 0) {
      return rawResStrongRefCache.get(rawRes);
    }
    return null;
  }

  @Nullable
  public LottieComposition getAsset(String assetName) {
    if (assetWeakRefCache.containsKey(assetName)) {
      WeakReference<LottieComposition> compRef = assetWeakRefCache.get(assetName);
      return compRef.get();
    } else if (assetStringRefCache.containsKey(assetName)) {
      return assetStringRefCache.get(assetName);
    }
    return null;
  }

  public void put(@RawRes int rawRes, @Nullable LottieComposition composition, CacheStrategy cacheStrategy) {
    if (cacheStrategy == CacheStrategy.Strong) {
      rawResStrongRefCache.put(rawRes, composition);
    } else if (cacheStrategy == CacheStrategy.Weak) {
      rawResWeakRefCache.put(rawRes, new WeakReference<LottieComposition>(composition));
    }
  }

  public void put(String assetName, @Nullable LottieComposition composition, CacheStrategy cacheStrategy) {
    if (cacheStrategy == CacheStrategy.Strong) {
      assetStringRefCache.put(assetName, composition);
    } else if (cacheStrategy == CacheStrategy.Weak) {
      assetWeakRefCache.put(assetName, new WeakReference<LottieComposition>(composition));
    }
  }
}
