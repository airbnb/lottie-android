package com.airbnb.lottie.model;

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

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class LottieCompositionCacheTest {

  private LottieComposition composition;
  private LottieCompositionCache cache;

  @Before
  public void setup() {
    composition = Mockito.mock(LottieComposition.class);
    cache = new LottieCompositionCache();
  }

  @Test
  public void testEmpty() {
    assertNull(cache.getAsset("foo"));
    assertNull(cache.getRawRes(123));
  }

  @Test
  public void testStrongAsset() {
    cache.put("foo", composition, LottieAnimationView.CacheStrategy.Strong);
    assertEquals(composition, cache.getAsset("foo"));
  }

  @Test
  public void testWeakAsset() {
    cache.put("foo", composition, LottieAnimationView.CacheStrategy.Weak);
    assertEquals(composition, cache.getAsset("foo"));
  }

  @Test
  public void testStrongRawRes() {
    cache.put(123, composition, LottieAnimationView.CacheStrategy.Strong);
    assertEquals(composition, cache.getRawRes(123));
  }

  @Test
  public void testWeakRawRes() {
    cache.put(123, composition, LottieAnimationView.CacheStrategy.Weak);
    assertEquals(composition, cache.getRawRes(123));
  }
}
