package com.airbnb.lottie;

import android.content.Context;

import com.airbnb.lottie.network.LottieNetworkCacheProvider;
import com.airbnb.lottie.network.DefaultLottieNetworkFetcher;
import com.airbnb.lottie.network.LottieNetworkFetcher;
import com.airbnb.lottie.network.NetworkCache;
import com.airbnb.lottie.network.NetworkFetcher;

import java.io.File;

import androidx.annotation.NonNull;
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

  private static LottieNetworkFetcher fetcher;
  private static LottieNetworkCacheProvider cacheProvider;

  private static volatile NetworkFetcher networkFetcher;
  private static volatile NetworkCache networkCache;

  private L() {
  }

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

  public static void setFetcher(LottieNetworkFetcher customFetcher) {
    fetcher = customFetcher;
  }

  public static void setCacheProvider(LottieNetworkCacheProvider customProvider) {
    cacheProvider = customProvider;
  }

  @NonNull
  public static NetworkFetcher networkFetcher(@NonNull Context context) {
    NetworkFetcher local = networkFetcher;
    if (local == null) {
      synchronized (NetworkFetcher.class) {
        local = networkFetcher;
        if (local == null) {
          networkFetcher = local = new NetworkFetcher(networkCache(context), fetcher != null ? fetcher : new DefaultLottieNetworkFetcher());
        }
      }
    }
    return local;
  }

  @NonNull
  public static NetworkCache networkCache(@NonNull final Context context) {
    NetworkCache local = networkCache;
    if (local == null) {
      synchronized (NetworkCache.class) {
        local = networkCache;
        if (local == null) {
          networkCache = local = new NetworkCache(cacheProvider != null ? cacheProvider : new LottieNetworkCacheProvider() {
            @Override @NonNull public File getCacheDir() {
              return new File(context.getCacheDir(), "lottie_network_cache");
            }
          });
        }
      }
    }
    return local;
  }
}
