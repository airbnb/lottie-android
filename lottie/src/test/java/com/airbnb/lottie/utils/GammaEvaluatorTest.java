package com.airbnb.lottie.utils;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class GammaEvaluatorTest {
  @Test
  public void testEvaluateForSameColorValues() {
    for (int color = 0x000000; color <= 0xffffff; color++) {
      int actual = GammaEvaluator.evaluate(0.3f, color, color);
      assertThat(actual, is(color));
    }
  }
}
