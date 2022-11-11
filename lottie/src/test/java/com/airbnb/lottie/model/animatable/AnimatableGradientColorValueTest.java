package com.airbnb.lottie.model.animatable;

import static org.junit.Assert.*;

import org.junit.Test;

public class AnimatableGradientColorValueTest {
  @Test
  public void testMergeTheSame() {
    assertArrayEquals(new float[]{1, 2}, AnimatableGradientColorValue.mergePositions(new float[]{1, 2}, new float[]{1, 2}), 0f);
  }

  @Test
  public void testMergeDifferent() {
    assertArrayEquals(new float[]{1, 2, 3, 4}, AnimatableGradientColorValue.mergePositions(new float[]{1, 2}, new float[]{3, 4}), 0f);
  }

  @Test
  public void testMergeOneOverlap() {
    assertArrayEquals(new float[]{1, 2, 3}, AnimatableGradientColorValue.mergePositions(new float[]{1, 2}, new float[]{2, 3}), 0f);
  }
}