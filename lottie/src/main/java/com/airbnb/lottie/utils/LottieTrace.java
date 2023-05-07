package com.airbnb.lottie.utils;

import androidx.core.os.TraceCompat;

public class LottieTrace {
  private static final int MAX_DEPTH = 5;

  private final String[] sections = new String[MAX_DEPTH];
  private final long[] startTimeNs = new long[MAX_DEPTH];
  private int traceDepth = 0;
  private int depthPastMaxDepth = 0;

  public void beginSection(String section) {
    if (traceDepth == MAX_DEPTH) {
      depthPastMaxDepth++;
      return;
    }
    sections[traceDepth] = section;
    startTimeNs[traceDepth] = System.nanoTime();
    //noinspection deprecation
    TraceCompat.beginSection(section);
    traceDepth++;
  }

  public float endSection(String section) {
    if (depthPastMaxDepth > 0) {
      depthPastMaxDepth--;
      return 0;
    }
    traceDepth--;
    if (traceDepth == -1) {
      throw new IllegalStateException("Can't end trace section. There are none.");
    }
    if (!section.equals(sections[traceDepth])) {
      throw new IllegalStateException("Unbalanced trace call " + section +
          ". Expected " + sections[traceDepth] + ".");
    }
    //noinspection deprecation
    TraceCompat.endSection();
    return (System.nanoTime() - startTimeNs[traceDepth]) / 1000000f;
  }
}
