package com.airbnb.lottie.network;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * The result of the operation of obtaining a Lottie animation
 */
public interface LottieFetchResult extends Closeable {
  /**
   * @return Is the operation successful
   */
  boolean isSuccessful();

  /**
   *
   * @return Received content stream
   */
  @NonNull
  InputStream bodyByteStream() throws IOException;

  /**
   *
   * @return Type of content received
   */
  @Nullable
  String contentType();

  /**
   *
   * @return Operation error
   */
  @Nullable
  String error();
}
