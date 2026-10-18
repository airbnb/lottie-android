package com.airbnb.lottie.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import android.view.animation.Interpolator;

import androidx.core.view.animation.PathInterpolatorCompat;

import com.airbnb.lottie.BaseTest;
import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.parser.moshi.JsonReader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import okio.Buffer;

public class KeyframeParserTest extends BaseTest {
  private boolean cacheDisabled;

  @Before public void enableCache() {
    cacheDisabled = L.getDisablePathInterpolatorCache();
    L.setDisablePathInterpolatorCache(false);
  }

  @After public void restoreCacheSetting() {
    L.setDisablePathInterpolatorCache(cacheDisabled);
  }

  @Test public void differentCurvesWithCollidingHashesKeepTheirEasing() throws IOException {
    Interpolator linear = parseInterpolator(0f, 0f, 1f, 1f);
    Interpolator eased = parseInterpolator(1f, 0f, 0f, 1f);
    assertNotSame(linear, eased);
    Interpolator expected = PathInterpolatorCompat.create(1f, 0f, 0f, 1f);
    for (float progress : new float[]{0.1f, 0.25f, 0.75f, 0.9f}) {
      assertEquals(progress, linear.getInterpolation(progress), 0.001f);
      assertEquals(expected.getInterpolation(progress), eased.getInterpolation(progress), 0.001f);
    }
    assertSame(linear, parseInterpolator(0f, 0f, 1f, 1f));
    assertSame(eased, parseInterpolator(1f, 0f, 0f, 1f));
  }

  @Test public void identicalCurvesReuseTheCachedInterpolator() throws IOException {
    Interpolator first = parseInterpolator(0.42f, 0f, 0.58f, 1f);
    assertSame(first, parseInterpolator(0.42f, 0f, 0.58f, 1f));
  }

  @Test public void disabledCacheDoesNotReuseInterpolators() throws IOException {
    L.setDisablePathInterpolatorCache(true);
    Interpolator first = parseInterpolator(0.42f, 0f, 0.58f, 1f);
    assertNotSame(first, parseInterpolator(0.42f, 0f, 0.58f, 1f));
  }

  private static Interpolator parseInterpolator(float x1, float y1, float x2, float y2) throws IOException {
    String json = "{\"t\":0,\"s\":[0],\"e\":[1],\"o\":{\"x\":" + x1 + ",\"y\":" + y1
        + "},\"i\":{\"x\":" + x2 + ",\"y\":" + y2 + "}}";
    try (JsonReader reader = JsonReader.of(new Buffer().writeUtf8(json))) {
      return KeyframeParser.parse(reader, new LottieComposition(), 1f, FloatParser.INSTANCE, true, false).interpolator;
    }
  }
}
