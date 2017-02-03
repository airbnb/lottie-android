package com.airbnb.lottie;

import java.io.IOException;
import java.io.InputStream;

public final class Utils {
  public static void closeQuietly(InputStream stream) {
    try {
      stream.close();
    } catch (IOException ignored) {
    }
  }
}
