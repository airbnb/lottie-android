package com.airbnb.lottie.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class LottieThreadFactory implements ThreadFactory {
  private static final AtomicInteger poolNumber = new AtomicInteger(1);
  private final ThreadGroup group;
  private final AtomicInteger threadNumber = new AtomicInteger(1);
  private final String namePrefix;

  public LottieThreadFactory() {
    SecurityManager s = System.getSecurityManager();
    group = (s == null) ? Thread.currentThread().getThreadGroup() : s.getThreadGroup();
    namePrefix = "lottie-" + poolNumber.getAndIncrement() + "-thread-";
  }

  public Thread newThread(Runnable r) {
    Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
    // Don't prevent this thread from letting Android kill the app process if it wants to.
    t.setDaemon(false);
    // This will block the main thread if it isn't high enough priority
    // so this thread should be as close to the main thread priority as possible.
    t.setPriority(Thread.MAX_PRIORITY);
    return t;
  }
}