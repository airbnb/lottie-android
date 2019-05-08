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

  public static FileExtension forFile(String filename) {
    for (FileExtension e : values()) {
      if (filename.endsWith(e.extension)) {
        return e;
      }
    }
    // Default to Json.
    Logger.warning("Unable to find correct extension for " + filename);
    return JSON;
  }
}
