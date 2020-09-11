package com.airbnb.lottie;

import android.content.Context;

import com.airbnb.lottie.network.DefaultFetcher;
import com.airbnb.lottie.network.NetworkFetcher;
import com.airbnb.lottie.network.Fetcher;
import com.airbnb.lottie.network.NetworkCache;
import com.airbnb.lottie.network.CacheProvider;

import java.io.File;

import androidx.annotation.NonNull;

public class Lottie {

  private static Fetcher fetcher;
  private static CacheProvider cacheDirCacheProvider;

  public static void initialize(@NonNull final LottieConfig lottieConfig) {
    fetcher = lottieConfig.networkFetcher;
    cacheDirCacheProvider = lottieConfig.cacheDirCacheProvider;
  }

  @NonNull
  public static NetworkFetcher networkFetcher(@NonNull Context context) {
    return new NetworkFetcher(networkCache(context), fetcher != null ? fetcher : new DefaultFetcher());
  }

  @NonNull
  public static NetworkCache networkCache(@NonNull final Context context) {
    return new NetworkCache(cacheDirCacheProvider != null ? cacheDirCacheProvider : new CacheProvider() {
      @Override @NonNull public File getCacheDir() {
        return new File(context.getCacheDir(), "lottie_network_cache");
      }
    });
  }
}