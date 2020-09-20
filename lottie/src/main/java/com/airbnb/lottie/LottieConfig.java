package com.airbnb.lottie;

import com.airbnb.lottie.network.LottieNetworkFetcher;
import com.airbnb.lottie.network.LottieNetworkCacheProvider;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Class for custom library configuration
 */
public class LottieConfig {

  @Nullable final LottieNetworkFetcher networkFetcher;
  @Nullable final LottieNetworkCacheProvider cacheProvider;

  private LottieConfig(@Nullable LottieNetworkFetcher networkFetcher, @Nullable LottieNetworkCacheProvider cacheProvider) {
    this.networkFetcher = networkFetcher;
    this.cacheProvider = cacheProvider;
  }

  public static final class Builder {

    @Nullable
    private LottieNetworkFetcher networkFetcher;
    @Nullable
    private LottieNetworkCacheProvider cacheProvider;

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
      return new LottieConfig(networkFetcher, cacheProvider);
    }
  }
}