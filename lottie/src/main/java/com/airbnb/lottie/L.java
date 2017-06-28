package com.airbnb.lottie;

import android.support.v4.os.TraceCompat;

public class L {
  static final String TAG = "LOTTIE";
  public static final boolean DBG = false;

  private static final int MAX_DEPTH = 20;
  private static boolean traceEnabled = false;
  private static String[] sections;
  private static long[] startTimeMillis;
  private static int traceDepth = 0;

  public static void setTraceEnabled(boolean enabled) {
    if (traceEnabled == enabled) {
      return;
    }
    traceEnabled = enabled;
    if (traceEnabled) {
      sections = new String[MAX_DEPTH];
      startTimeMillis = new long[MAX_DEPTH];
    }
  }

  static void beginSection(String section) {
    if (!traceEnabled || traceDepth >= MAX_DEPTH) {
      return;
    }
    sections[traceDepth] = section;
    startTimeMillis[traceDepth] = System.currentTimeMillis();
    TraceCompat.beginSection(section);
    traceDepth++;
  }

  static long endSection(String section) {
    if (!traceEnabled) {
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
    TraceCompat.endSection();
    return System.currentTimeMillis() - startTimeMillis[traceDepth + 1];
  }
}
