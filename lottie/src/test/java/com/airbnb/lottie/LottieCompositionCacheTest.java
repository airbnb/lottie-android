package com.airbnb.lottie;

import com.airbnb.lottie.model.LottieCompositionCache;

import org.junit.Assert;
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
    cache.put(composition, "foo", LottieAnimationView.CacheStrategy.Strong);
    assertEquals(composition, cache.getAsset("foo"));
  }

  @Test
  public void testWeakAsset() {
    cache.put(composition, "foo", LottieAnimationView.CacheStrategy.Weak);
    assertEquals(composition, cache.getAsset("foo"));
  }

  @Test
  public void testStrongRawRes() {
    cache.put(composition, 123, LottieAnimationView.CacheStrategy.Strong);
    assertEquals(composition, cache.getRawRes(123));
  }

  @Test
  public void testWeakRawRes() {
    cache.put(composition, 123, LottieAnimationView.CacheStrategy.Weak);
    assertEquals(composition, cache.getRawRes(123));
  }
}
