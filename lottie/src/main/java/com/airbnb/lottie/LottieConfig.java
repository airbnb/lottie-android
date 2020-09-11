package com.airbnb.lottie;

import android.content.Context;

import com.airbnb.lottie.network.Fetcher;
import com.airbnb.lottie.network.Supplier;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LottieConfig {

  @NonNull final Context applicationContext;
  @Nullable final Fetcher networkFetcher;
  @Nullable final Supplier<File> cacheDirSupplier;

  public LottieConfig(@NonNull Context applicationContext, @Nullable Fetcher networkFetcher, @Nullable Supplier<File> cacheDirSupplier) {
    this.applicationContext = applicationContext;
    this.networkFetcher = networkFetcher;
    this.cacheDirSupplier = cacheDirSupplier;
  }

  public static final class Builder {

    @NonNull
    private final Context context;
    @Nullable
    private Fetcher networkFetcher;
    @Nullable
    private Supplier<File> cacheDirSupplier;

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
      this.cacheDirSupplier = new Supplier<File>() {
        @Override public File get() {
          return file;
        }
      };
      return this;
    }

    @NonNull
    public Builder setCacheDirSupplier(@NonNull Supplier<File> fileSupplier) {
      this.cacheDirSupplier = fileSupplier;
      return this;
    }

    @NonNull
    public LottieConfig build() {
      return new LottieConfig(context, networkFetcher, cacheDirSupplier);
    }
  }
}