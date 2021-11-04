package com.airbnb.lottie.network;

import com.airbnb.lottie.utils.Logger;

/**
 * Helpers for known Lottie file types.
 */
public enum FileExtension {
  JSON(".json"),
  ZIP(".zip");

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
