package com.airbnb.lottie.network;

import java.io.InputStream;

import androidx.annotation.NonNull;

public class Result {

  public static class Success extends Result {
    @NonNull
    public final InputStream inputStream;
    @NonNull
    public final String contentType;

    public Success(@NonNull InputStream inputStream, @NonNull String contentType) {
      this.inputStream = inputStream;
      this.contentType = contentType;
    }
  }

  public static class Error extends Result {
    @NonNull
    public final String message;
    public final int responseCode;

    public Error(@NonNull String message, int responseCode) {
      this.message = message;
      this.responseCode = responseCode;
    }
  }
}
