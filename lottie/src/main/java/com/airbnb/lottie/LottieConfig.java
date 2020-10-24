package com.airbnb.lottie;

import com.airbnb.lottie.network.LottieNetworkFetcher;
import com.airbnb.lottie.network.LottieNetworkCacheProvider;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Class for custom library configuration.
 *
 * This should be constructed with {@link LottieConfig.Builder}
 */
public class LottieConfig {

  @Nullable final LottieNetworkFetcher networkFetcher;
  @Nullable final LottieNetworkCacheProvider cacheProvider;
  final boolean enableSystraceMarkers;

  private LottieConfig(@Nullable LottieNetworkFetcher networkFetcher, @Nullable LottieNetworkCacheProvider cacheProvider,
      boolean enableSystraceMarkers) {
    this.networkFetcher = networkFetcher;
    this.cacheProvider = cacheProvider;
    this.enableSystraceMarkers = enableSystraceMarkers;
  }

  public static final class Builder {

    @Nullable
    private LottieNetworkFetcher networkFetcher;
    @Nullable
    private LottieNetworkCacheProvider cacheProvider;
    private boolean enableSystraceMarkers = false;

    /**
     * Lottie has a default network fetching stack built on {@link java.net.HttpURLConnection}. However, if you would like to hook into your own
     * network stack for performance, caching, or analytics, you may replace the internal stack with your own.
     */
    @NonNull
    public Builder setNetworkFetcher(@NonNull LottieNetworkFetcher fetcher) {
      this.networkFetcher = fetcher;
      return this;
    }

    /**
     * Provide your own network cache directory. By default, animations will be saved in your application's cacheDir/lottie_network_cache.
     *
     * @see #setNetworkCacheProvider(LottieNetworkCacheProvider)
     */
    @NonNull
    public Builder setNetworkCacheDir(@NonNull final File file) {
      if (cacheProvider != null) {
        throw new IllegalStateException("There is already a cache provider!");
      }
      cacheProvider = new LottieNetworkCacheProvider() {
        @Override @NonNull public File getCacheDir() {
          if (!file.isDirectory()) {
            throw new IllegalArgumentException("cache file must be a directory");
          }
          return file;
        }
      };
      return this;
    }

    /**
     * Provide your own network cache provider. By default, animations will be saved in your application's cacheDir/lottie_network_cache.
     */
    @NonNull
    public Builder setNetworkCacheProvider(@NonNull final LottieNetworkCacheProvider fileCacheProvider) {
      if (cacheProvider != null) {
        throw new IllegalStateException("There is already a cache provider!");
      }
      cacheProvider = new LottieNetworkCacheProvider() {
        @NonNull @Override public File getCacheDir() {
          File file = fileCacheProvider.getCacheDir();
          if (!file.isDirectory()) {
            throw new IllegalArgumentException("cache file must be a directory");
          }
          return file;
        }
      };
      return this;
    }

    /**
     * Enable this if you want to run systrace to debug the performance of animations.
     * <p/>
     * DO NOT leave this enabled in production. The overhead is low but non-zero.
     *
     * @see <a href="https://developer.android.com/topic/performance/tracing/command-line">Systrace Docs</a>
     */
    @NonNull
    public Builder setEnableSystraceMarkers(boolean enable) {
      enableSystraceMarkers = enable;
      return this;
    }

    @NonNull
    public LottieConfig build() {
      return new LottieConfig(networkFetcher, cacheProvider, enableSystraceMarkers);
    }
  }
}