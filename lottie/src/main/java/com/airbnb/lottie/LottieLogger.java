package com.airbnb.lottie;

/**
 * Give ability to integrators to provide another logging mechanism.
 */
public interface LottieLogger {

  void debug(String message);

  void debug(String message, Throwable exception);

  void warning(String message);

  void warning(String message, Throwable exception);

  void error(String message, Throwable exception);
}
