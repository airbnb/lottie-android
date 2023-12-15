package com.airbnb.lottie.model.content;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.Arrays;

public class GradientColorTest extends TestCase {

  private final GradientColor start = new GradientColor(new float[]{0f, 1f}, new int[]{0xFF000000, 0xFF020202});

  private final GradientColor end = new GradientColor(new float[]{0f, 1f}, new int[]{0xFF020202, 0xFF040404});

  private final GradientColor gradient = new GradientColor(new float[2], new int[2]);

  @Test
  public void testLerpWithOutOfBoundsNegativeProgress() {
    gradient.lerp(start, end, -42f);
    assertEquals(start, gradient);
  }

  @Test
  public void testLerpWithZeroProgress() {
    gradient.lerp(start, end, 0f);
    assertEquals(start, gradient);
  }

  @Test
  public void testLerpWithHalfProgress() {
    gradient.lerp(start, end, 0.5f);
    GradientColor half = new GradientColor(new float[]{0f, 1f}, new int[]{0xFF010101, 0xFF030303});
    assertEquals(half, gradient);
  }

  @Test
  public void testLerpWithOneProgress() {
    gradient.lerp(start, end, 1f);
    assertEquals(end, gradient);
  }

  @Test
  public void testLerpWithOutOfBoundsPositiveProgress() {
    gradient.lerp(start, end, 42f);
    assertEquals(end, gradient);
  }
}
