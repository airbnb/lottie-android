package com.airbnb.lottie.model;

import com.airbnb.lottie.BaseTest;
import com.airbnb.lottie.BuildConfig;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LottieCompositionCacheTest extends BaseTest  {

  private LottieComposition composition;
  private LottieCompositionCache cache;

  @Before
  public void setup() {
    composition = Mockito.mock(LottieComposition.class);
    cache = new LottieCompositionCache();
  }

  @Test
  public void testEmpty() {
    assertNull(cache.get("foo"));
  }

  @Test
  public void testStrongAsset() {
    cache.put("foo", composition);
    assertEquals(composition, cache.get("foo"));
  }

  @Test
  public void testWeakAsset() {
    cache.put("foo", composition);
    assertEquals(composition, cache.get("foo"));
  }
}
