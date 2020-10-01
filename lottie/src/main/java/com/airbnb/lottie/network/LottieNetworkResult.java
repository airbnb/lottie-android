package com.airbnb.lottie.network;

import com.airbnb.lottie.utils.Logger;

import java.io.Closeable;
import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class LottieNetworkResult implements Closeable {

  /**
   * The closeable will be called after the animation has loaded.
   *  You can close your connection here.
   */
  @Nullable
  private final Closeable closeable;

  LottieNetworkResult(@Nullable Closeable closeable) {
    this.closeable = closeable;
  }

  @Override public void close() {
    try {
      if (closeable != null) {
        closeable.close();
      }
    } catch (Exception exception) {
      Logger.error("LottieNetworkResult close: failed", exception);
    }
  }

  public static class Success extends LottieNetworkResult {
    @NonNull
    public final InputStream inputStream;
    @NonNull
    public final String contentType;

    public Success(@NonNull InputStream inputStream, @NonNull String contentType, @Nullable Closeable closeable) {
      super(closeable);
      this.inputStream = inputStream;
      this.contentType = contentType;
    }
  }

  public static class Error extends LottieNetworkResult {
    @NonNull
    public final String message;
    public final int responseCode;

    public Error(@NonNull String message, int responseCode, @Nullable Closeable closeable) {
      super(closeable);
      this.message = message;
      this.responseCode = responseCode;
    }
  }
}
