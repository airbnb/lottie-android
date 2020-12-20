package com.airbnb.lottie.utils;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class GammaEvaluatorTest {
  @Test
  public void testEvaluateForSameColorValues() {
    for (int color = 0x000000; color <= 0xffffff; color++) {
      int actual = GammaEvaluator.evaluate(0.3f, color, color);
      assertThat(actual, is(color));
    }
  }
}
