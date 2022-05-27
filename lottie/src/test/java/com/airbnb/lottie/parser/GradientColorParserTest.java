package com.airbnb.lottie.parser;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

public class GradientColorParserTest {

  @Test public void testNoDistinctShort() {
    assertMerged(new float[]{1}, new float[]{2}, new float[]{1, 2});
  }

  @Test public void testNoDistinct() {
    assertMerged(new float[]{1, 2, 3}, new float[]{4, 5, 6}, new float[]{1, 2, 3, 4, 5, 6});
  }

  @Test public void testWithDistinct() {
    assertMerged(new float[]{1, 2, 3, 5}, new float[]{4, 5, 6}, new float[]{1, 2, 3, 4, 5, 6});
  }

  @Test public void testWithDistinctInterleavingValues() {
    assertMerged(new float[]{2, 4, 5, 6, 8, 10}, new float[]{1, 3, 4, 5, 7, 9}, new float[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
  }

  @Test public void testIdentical() {
    assertMerged(new float[]{2, 3}, new float[]{2, 3}, new float[]{2, 3});
  }

  private void assertMerged(float[] arrayA, float[] arrayB, float[] merged) {
    assertArrayEquals(merged, GradientColorParser.mergeUniqueElements(arrayA, arrayB), 0f);
  }
}