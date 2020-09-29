package com.airbnb.lottie.utils;

import com.airbnb.lottie.LottieLogger;

/**
 * Singleton object for logging. If you want to provide a custom logger implementation,
 * implements LottieLogger interface in a custom class and replace Logger.instance
 */
public class Logger {

  private static LottieLogger INSTANCE = new LogcatLogger();

  public static void setInstance(LottieLogger instance) {
    Logger.INSTANCE = instance;
  }

  public static void debug(String message) {
    INSTANCE.debug(message);
  }

  public static void debug(String message, Throwable exception) {
    INSTANCE.debug(message, exception);
  }

  public static void warning(String message) {
    INSTANCE.warning(message);
  }

  public static void warning(String message, Throwable exception) {
    INSTANCE.warning(message, exception);
  }

  public static void error(String message, Throwable exception) {
    INSTANCE.error(message, exception);
  }
}
