package com.airbnb.lottie.network;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.Nullable;

public interface LottieFetchResult extends Closeable {
  boolean isSuccessful() throws IOException;
  int resultCode() throws IOException;
  @Nullable
  String message() throws IOException;
  @Nullable
  InputStream bodyByteStream() throws IOException;
  @Nullable
  String contentType();
  @Nullable
  String error() throws IOException;
}
