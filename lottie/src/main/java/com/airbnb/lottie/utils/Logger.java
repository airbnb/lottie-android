package com.airbnb.lottie.utils;

/**
 * Singleton object for logging. If you want to provide a custom logger implementation,
 * implements LottieLogger interface in a custom class and replace Logger.instance
 */
public class Logger {
  
  public static LottieLogger instance = new LogcatLogger();

  public static void debug(String message) {
    instance.debug(message);
  }

  public static void debug(String message, Throwable exception) {
    instance.debug(message, exception);
  }

  public static void warning(String message) {
    instance.warning(message);
  }

  public static void warning(String message, Throwable exception) {
    instance.warning(message, exception);
  }
}
