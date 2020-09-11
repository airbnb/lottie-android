package com.airbnb.lottie;

import android.content.Context;

import com.airbnb.lottie.network.DefaultFetcher;
import com.airbnb.lottie.network.NetworkFetcher;
import com.airbnb.lottie.network.Fetcher;
import com.airbnb.lottie.network.NetworkCache;
import com.airbnb.lottie.network.Supplier;

import java.io.File;

import androidx.annotation.NonNull;

public class Lottie {

  private static Fetcher fetcher;
  private static Supplier<File> cacheDirSupplier;

  public static void initialize(@NonNull final LottieConfig lottieConfig) {
    fetcher = lottieConfig.networkFetcher;
    cacheDirSupplier = lottieConfig.cacheDirSupplier;
  }

  @NonNull
  public static NetworkFetcher networkFetcher(@NonNull Context context) {
    return new NetworkFetcher(networkCache(context), fetcher != null ? fetcher : new DefaultFetcher());
  }

  @NonNull
  public static NetworkCache networkCache(@NonNull final Context context) {
    return new NetworkCache(cacheDirSupplier != null ? cacheDirSupplier : new Supplier<File>() {
      @Override public File get() {
        return context.getCacheDir();
      }
    });
  }
}