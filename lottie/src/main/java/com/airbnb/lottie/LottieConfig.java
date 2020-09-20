package com.airbnb.lottie;

import android.content.Context;

import com.airbnb.lottie.network.LottieNetworkFetcher;
import com.airbnb.lottie.network.LottieNetworkCacheProvider;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Class for custom library configuration
 */
public class LottieConfig {

  @NonNull final Context applicationContext;
  @Nullable final LottieNetworkFetcher networkFetcher;
  @Nullable final LottieNetworkCacheProvider cacheProvider;

  private LottieConfig(@NonNull Context applicationContext, @Nullable LottieNetworkFetcher networkFetcher, @Nullable
      LottieNetworkCacheProvider cacheProvider) {
    this.applicationContext = applicationContext;
    this.networkFetcher = networkFetcher;
    this.cacheProvider = cacheProvider;
  }

  public static final class Builder {

    @NonNull
    private final Context context;
    @Nullable
    private LottieNetworkFetcher networkFetcher;
    @Nullable
    private LottieNetworkCacheProvider cacheProvider;

    public Builder(@NonNull Context context) {
      this.context = context.getApplicationContext();
    }

    @NonNull
    public Builder setNetworkFetcher(@NonNull LottieNetworkFetcher fetcher) {
      this.networkFetcher = fetcher;
      return this;
    }

    @NonNull
    public Builder setCacheDir(@NonNull final File file) {
      this.cacheProvider = new LottieNetworkCacheProvider() {
        @Override @NonNull public File getCacheDir() {
          return file;
        }
      };
      return this;
    }

    @NonNull
    public Builder setCacheProvider(@NonNull LottieNetworkCacheProvider fileCacheProvider) {
      this.cacheProvider = fileCacheProvider;
      return this;
    }

    @NonNull
    public LottieConfig build() {
      return new LottieConfig(context, networkFetcher, cacheProvider);
    }
  }
}