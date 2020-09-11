package com.airbnb.lottie;

import android.content.Context;

import com.airbnb.lottie.network.Fetcher;
import com.airbnb.lottie.network.CacheProvider;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LottieConfig {

  @NonNull final Context applicationContext;
  @Nullable final Fetcher networkFetcher;
  @Nullable final CacheProvider cacheDirCacheProvider;

  public LottieConfig(@NonNull Context applicationContext, @Nullable Fetcher networkFetcher, @Nullable CacheProvider cacheDirCacheProvider) {
    this.applicationContext = applicationContext;
    this.networkFetcher = networkFetcher;
    this.cacheDirCacheProvider = cacheDirCacheProvider;
  }

  public static final class Builder {

    @NonNull
    private final Context context;
    @Nullable
    private Fetcher networkFetcher;
    @Nullable
    private CacheProvider cacheDirCacheProvider;

    public Builder(@NonNull Context context) {
      this.context = context.getApplicationContext();
    }

    @NonNull
    public Builder setNetworkFetcher(@NonNull Fetcher fetcher) {
      this.networkFetcher = fetcher;
      return this;
    }

    @NonNull
    public Builder setCacheDir(@NonNull final File file) {
      this.cacheDirCacheProvider = new CacheProvider() {
        @Override @NonNull public File getCacheDir() {
          return file;
        }
      };
      return this;
    }

    @NonNull
    public Builder setCacheDirCacheProvider(@NonNull CacheProvider fileCacheProvider) {
      this.cacheDirCacheProvider = fileCacheProvider;
      return this;
    }

    @NonNull
    public LottieConfig build() {
      return new LottieConfig(context, networkFetcher, cacheDirCacheProvider);
    }
  }
}