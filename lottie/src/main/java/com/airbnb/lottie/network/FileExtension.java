package com.airbnb.lottie.network;

import androidx.annotation.RestrictTo;

/**
 * Helpers for known Lottie file types.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public enum FileExtension {
  JSON(".json"),
  ZIP(".zip"),
  GZIP(".gz");

  public final String extension;

  FileExtension(String extension) {
    this.extension = extension;
  }

  public String tempExtension() {
    return ".temp" + extension;
  }

  @Override public String toString() {
    return extension;
  }

}
