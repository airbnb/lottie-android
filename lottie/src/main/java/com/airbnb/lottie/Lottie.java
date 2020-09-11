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

  private static volatile NetworkFetcher networkFetcher;
  private static volatile NetworkCache networkCache;

  public static void initialize(@NonNull final LottieConfig lottieConfig) {
    fetcher = lottieConfig.networkFetcher;
    cacheDirCacheProvider = lottieConfig.cacheDirCacheProvider;
  }

  @NonNull
  public static NetworkFetcher networkFetcher(@NonNull Context context) {
    NetworkFetcher local = networkFetcher;
    if (local == null) {
      synchronized (NetworkFetcher.class) {
        local = networkFetcher;
        if (local == null) {
          networkFetcher = local = new NetworkFetcher(networkCache(context), fetcher != null ? fetcher : new DefaultFetcher());
        }
      }
    }
    return local;
  }

  @NonNull
  public static NetworkCache networkCache(@NonNull final Context context) {
    NetworkCache local = networkCache;
    if (local == null) {
      synchronized (NetworkCache.class) {
        local = networkCache;
        if (local == null) {
          networkCache = local = new NetworkCache(cacheDirCacheProvider != null ? cacheDirCacheProvider : new CacheProvider() {
            @Override @NonNull public File getCacheDir() {
              return new File(context.getCacheDir(), "lottie_network_cache");
            }
          });
        }
      }
    }
    return local;
  }
}