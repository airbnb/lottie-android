package com.airbnb.lottie;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.airbnb.lottie.configurations.reducemotion.ReducedMotionOption;
import com.airbnb.lottie.configurations.reducemotion.IgnoreDisabledSystemAnimationsOption;
import com.airbnb.lottie.configurations.reducemotion.SystemReducedMotionOption;
import com.airbnb.lottie.network.LottieNetworkCacheProvider;
import com.airbnb.lottie.network.LottieNetworkFetcher;

import java.io.File;

/**
 * Class for custom library configuration.
 * <p>
 * This should be constructed with {@link LottieConfig.Builder}
 */
public class LottieConfig {

  @Nullable final LottieNetworkFetcher networkFetcher;
  @Nullable final LottieNetworkCacheProvider cacheProvider;
  final boolean enableSystraceMarkers;
  final boolean enableNetworkCache;
  final boolean disablePathInterpolatorCache;
  final AsyncUpdates defaultAsyncUpdates;
  final ReducedMotionOption reducedMotionOption;

  private LottieConfig(@Nullable LottieNetworkFetcher networkFetcher, @Nullable LottieNetworkCacheProvider cacheProvider,
      boolean enableSystraceMarkers, boolean enableNetworkCache, boolean disablePathInterpolatorCache,
      AsyncUpdates defaultAsyncUpdates, ReducedMotionOption reducedMotionOption) {
    this.networkFetcher = networkFetcher;
    this.cacheProvider = cacheProvider;
    this.enableSystraceMarkers = enableSystraceMarkers;
    this.enableNetworkCache = enableNetworkCache;
    this.disablePathInterpolatorCache = disablePathInterpolatorCache;
    this.defaultAsyncUpdates = defaultAsyncUpdates;
    this.reducedMotionOption = reducedMotionOption;
  }

  public static final class Builder {

    @Nullable
    private LottieNetworkFetcher networkFetcher;
    @Nullable
    private LottieNetworkCacheProvider cacheProvider;
    private boolean enableSystraceMarkers = false;
    private boolean enableNetworkCache = true;
    private boolean disablePathInterpolatorCache = true;
    private AsyncUpdates defaultAsyncUpdates = AsyncUpdates.AUTOMATIC;
    private ReducedMotionOption reducedMotionOption = new SystemReducedMotionOption();

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

    /**
     * Disable this if you want to completely disable internal Lottie cache for retrieving network animations.
     * Internal network cache is enabled by default.
     */
    @NonNull
    public Builder setEnableNetworkCache(boolean enable) {
      enableNetworkCache = enable;
      return this;
    }

    /**
     * When parsing animations, Lottie has a path interpolator cache. This cache allows Lottie to reuse PathInterpolators
     * across an animation. This is desirable in most cases. However, when shared across screenshot tests, it can cause slight
     * deviations in the rendering due to underlying approximations in the PathInterpolator.
     * <p>
     * The cache is enabled by default and should probably only be disabled for screenshot tests.
     */
    @NonNull
    public Builder setDisablePathInterpolatorCache(boolean disable) {
      disablePathInterpolatorCache = disable;
      return this;
    }

    /**
     * Sets the default value for async updates.
     *
     * @see LottieDrawable#setAsyncUpdates(AsyncUpdates)
     */
    @NonNull
    public Builder setDefaultAsyncUpdates(AsyncUpdates asyncUpdates) {
      defaultAsyncUpdates = asyncUpdates;
      return this;
    }

    /**
     * Provide your own reduce motion option. By default it uses
     * {@link SystemReducedMotionOption},
     * which returns ReducedMotionMode.REDUCED_MOTION when the system AnimationScale is set to 0.
     * <p>
     * You can override this behavior by providing your own custom {@link ReducedMotionOption}.
     * You can also use {@link IgnoreDisabledSystemAnimationsOption}
     * if you want to ignore the system settings and always play the full animation.
     */
    @NonNull
    public Builder setReducedMotionOption(ReducedMotionOption reducedMotionOption) {
      this.reducedMotionOption = reducedMotionOption;
      return this;
    }

    @NonNull
    public LottieConfig build() {
      return new LottieConfig(networkFetcher, cacheProvider, enableSystraceMarkers, enableNetworkCache, disablePathInterpolatorCache,
          defaultAsyncUpdates, reducedMotionOption);
    }
  }
}
