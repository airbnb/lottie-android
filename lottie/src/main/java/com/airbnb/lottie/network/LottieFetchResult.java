package com.airbnb.lottie.network;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.Nullable;

/**
 * The result of the operation of obtaining a lotty animation
 */
public interface LottieFetchResult extends Closeable {
  /**
   * @return Is the operation successful
   */
  boolean isSuccessful();

  /**
   *
   * @return Operation result code
   */
  int resultCode() throws IOException;

  /**
   *
   * @return Received content stream
   */
  @Nullable
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
  String error() throws IOException;
}
