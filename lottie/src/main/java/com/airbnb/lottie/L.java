package com.airbnb.lottie;

import android.support.annotation.RestrictTo;
import android.support.v4.os.TraceCompat;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class L {
  public static final String TAG = "LOTTIE";
  public static final boolean DBG = false;
  /**
   * This is a cheat to get the px -> dp scale from anywhere because it requires context to get
   * and shouldn't change.
   */
  private static float dpScale = -1;

  private static final int MAX_DEPTH = 20;
  private static boolean traceEnabled = false;
  private static String[] sections;
  private static long[] startTimeNs;
  private static int traceDepth = 0;
  private static int depthPastMaxDepth = 0;

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

  public static void setDpScale(float dpScale) {
    L.dpScale = dpScale;
  }

  public static float getDpScale() {
    if (L.dpScale == -1) {
      throw new IllegalStateException("dpScale has not been initialized yet.");
    }
    return L.dpScale;
  }
}
