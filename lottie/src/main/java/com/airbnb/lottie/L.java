package com.airbnb.lottie;

import com.airbnb.lottie.utils.LogcatLogger;
import com.airbnb.lottie.utils.LottieLogger;

import androidx.annotation.RestrictTo;
import androidx.core.os.TraceCompat;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class L {

  public static boolean DBG = false;
  public static final String TAG = "LOTTIE";

  private static final int MAX_DEPTH = 20;
  private static boolean traceEnabled = false;
  private static String[] sections;
  private static long[] startTimeNs;
  private static int traceDepth = 0;
  private static int depthPastMaxDepth = 0;

  public static LottieLogger logger = new LogcatLogger();

  public static void setTraceEnabled(boolean enabled) {
    if (traceEnabled == enabled) {
      return;
    }
    traceEnabled = enabled;
    if (traceEnabled) {
      sections = new String[MAX_DEPTH];
      startTimeNs = new long[MAX_DEPTH];
    }
  }

  public static void beginSection(String section) {
    if (!traceEnabled) {
      return;
    }
    if (traceDepth == MAX_DEPTH) {
      depthPastMaxDepth++;
      return;
    }
    sections[traceDepth] = section;
    startTimeNs[traceDepth] = System.nanoTime();
    TraceCompat.beginSection(section);
    traceDepth++;
  }

  public static float endSection(String section) {
    if (depthPastMaxDepth > 0) {
      depthPastMaxDepth--;
      return 0;
    }
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
    return (System.nanoTime() - startTimeNs[traceDepth]) / 1000000f;
  }
}
